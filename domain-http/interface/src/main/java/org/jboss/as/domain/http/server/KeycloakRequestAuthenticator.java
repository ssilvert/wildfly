/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

package org.jboss.as.domain.http.server;

import io.undertow.security.api.SecurityContext;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.Session;
import io.undertow.util.Sessions;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.HttpFacade;
import org.keycloak.adapters.KeycloakAccount;
import org.keycloak.adapters.KeycloakDeployment;
import org.keycloak.adapters.RefreshableKeycloakSecurityContext;
import org.keycloak.adapters.undertow.KeycloakUndertowAccount;
import org.keycloak.adapters.undertow.UndertowRequestAuthenticator;
import org.keycloak.adapters.undertow.UndertowUserSessionManagement;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2014 Red Hat Inc.
 */
public class KeycloakRequestAuthenticator extends UndertowRequestAuthenticator {
    private final UndertowUserSessionManagement userSessionManagement;

    public KeycloakRequestAuthenticator(HttpFacade facade, KeycloakDeployment deployment, int sslRedirectPort,
                                       SecurityContext securityContext, HttpServerExchange exchange,
                                       UndertowUserSessionManagement userSessionManagement) {
        super(facade, deployment, sslRedirectPort, securityContext, exchange);
        this.userSessionManagement = userSessionManagement;
    }

    @Override
    protected boolean isCached() {
        //final ServletRequestContext servletRequestContext = exchange.getAttachment(ServletRequestContext.ATTACHMENT_KEY);
        //HttpServletRequest req = (HttpServletRequest) servletRequestContext.getServletRequest();
        Session session = Sessions.getSession(exchange);
        if (session == null) {
            log.info("session was null, returning null");
            return false;
        }
        KeycloakUndertowAccount account = (KeycloakUndertowAccount)session.getAttribute(KeycloakUndertowAccount.class.getName());
        if (account == null) {
            log.info("Account was not in session, returning null");
            return false;
        }
        account.setDeployment(deployment);
        if (account.isActive()) {
            log.info("Cached account found");
            securityContext.authenticationComplete(account, "KEYCLOAK", false);
            propagateKeycloakContext( account);
            return true;
        }
        log.info("Account was not active, returning null");
        session.setAttribute(KeycloakUndertowAccount.class.getName(), null);
        return false;
    }

    @Override
    protected void completeOAuthAuthentication(KeycloakPrincipal principal, RefreshableKeycloakSecurityContext session) {
        KeycloakUndertowAccount account = new KeycloakUndertowSubjectAccount(principal, session, deployment);
        securityContext.authenticationComplete(account, "KEYCLOAK", false);
        propagateKeycloakContext(account);
        login(account);
    }

    @Override
    protected void completeBearerAuthentication(KeycloakPrincipal principal, RefreshableKeycloakSecurityContext session) {
        KeycloakUndertowAccount account = new KeycloakUndertowSubjectAccount(principal, session, deployment);
        securityContext.authenticationComplete(account, "KEYCLOAK", false);
        propagateKeycloakContext(account);
    }

    @Override
    protected void login(KeycloakAccount account) {
        Session session = Sessions.getOrCreateSession(exchange);
        session.setAttribute(KeycloakUndertowAccount.class.getName(), account);
        String username = account.getPrincipal().getName();
        String keycloakSessionId = account.getKeycloakSecurityContext().getToken().getSessionState();
        userSessionManagement.login(session.getSessionManager(), session.getId(), username, keycloakSessionId);
    }

}
