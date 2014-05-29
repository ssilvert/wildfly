/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.webservices.service;

import static org.jboss.as.webservices.WSLogger.ROOT_LOGGER;

import java.security.Provider;
import java.security.Security;
import java.util.List;

import javax.management.MBeanServer;

import org.jboss.as.server.ServerEnvironment;
import org.jboss.as.server.ServerEnvironmentService;
import org.jboss.as.webservices.config.ServerConfigImpl;
import org.jboss.as.webservices.util.WSServices;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.ServiceBuilder;
import org.jboss.msc.service.ServiceBuilder.DependencyType;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceController.Mode;
import org.jboss.msc.service.ServiceListener;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.ServiceTarget;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.ImmediateValue;
import org.jboss.ws.common.management.AbstractServerConfig;
import org.jboss.wsf.spi.management.ServerConfig;

/**
 * WS server config service.
 *
 * @author <a href="alessio.soldano@jboss.com">Alessio Soldano</a>
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class ServerConfigService implements Service<ServerConfig> {

    private static final ServiceName MBEAN_SERVER_NAME = ServiceName.JBOSS.append("mbean", "server");
    private final AbstractServerConfig serverConfig;

    private ServerConfigService(final AbstractServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    @Override
    public ServerConfig getValue() {
        return serverConfig;
    }

    @Override
    public void start(final StartContext context) throws StartException {
        try {
            serverConfig.create();
            //we fix newly added BouncyCastle security provider to remove DH KeyPairGenerator
            fixSecurityProvider();
        } catch (final Exception e) {
            ROOT_LOGGER.configServiceCreationFailed();
            throw new StartException(e);
        }
    }

    @Override
    public void stop(final StopContext context) {
        try {
            serverConfig.destroy();
        } catch (final Exception e) {
            ROOT_LOGGER.configServiceDestroyFailed();
        }
    }

    public static ServiceController<?> install(final ServiceTarget serviceTarget, final ServerConfigImpl serverConfig,
            final ServiceListener<Object> listener, final List<ServiceName> dependencies, final boolean jmxSubsystemAvailable) {
        final ServiceBuilder<ServerConfig> builder = serviceTarget.addService(WSServices.CONFIG_SERVICE, new ServerConfigService(serverConfig));
        if (jmxSubsystemAvailable) {
            builder.addDependency(DependencyType.REQUIRED, MBEAN_SERVER_NAME, MBeanServer.class, serverConfig.getMBeanServerInjector());
        } else {
            serverConfig.getMBeanServerInjector().setValue(new ImmediateValue<MBeanServer>(null));
        }
        builder.addDependency(ServerEnvironmentService.SERVICE_NAME, ServerEnvironment.class, serverConfig.getServerEnvironmentInjector());
        for (ServiceName dep : dependencies) {
            builder.addDependency(dep);
        }
        builder.addListener(listener);
        builder.setInitialMode(Mode.ACTIVE);
        ServiceController<?> sc = builder.install();
        return sc;
    }

    /*
    Fixes WFLY-3252 First HTTPS / SSL request after startup of Wildfly 8.0.0.Final is blocked for many seconds
    This is a big hack, which removes BouncyCastle DH KeyPairGenerator algorithm as it can hang / eat lots of CPU
    see https://issues.apache.org/jira/browse/HARMONY-3789 & http://bouncycastle.org/jira/browse/BJA-19 for more
    */
    private static void fixSecurityProvider(){
        Provider bc = Security.getProvider("BC");
        if (bc!=null) {
            bc.remove("KeyPairGenerator.DH");
        }
    }
}
