/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012 Red Hat Inc. and/or its affiliates and other contributors
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
package org.jboss.as.cli.gui.tables;

import java.util.List;
import org.jboss.as.cli.CommandFormatException;
import org.jboss.as.cli.gui.CliGuiContext;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
public class AttributeType {
    private CliGuiContext cliGuiCtx;
    private String path;
    private Property attrib;

    public AttributeType(CliGuiContext cliGuiCtx, String path, Property attrib) {
        this.cliGuiCtx = cliGuiCtx;
        this.path = path;
        this.attrib = attrib;
    }

    public String getHelpText() {
        ModelNode value = attrib.getValue();
        if (!value.get("description").isDefined()) return "";
        return value.get("description").asString();
    }

    public String getName() {
        return attrib.getName();
    }

    public String getPath() {
        return this.path;
    }

    public List<ModelNode> getAddress() {
        try {
            // use :read-operation-names just to get the properly-formatted address
            ModelNode request = cliGuiCtx.getExecutor().buildRequest(path + ":read-operation-names");
            return request.get("address").asList();
        } catch (CommandFormatException e) {
            throw new IllegalStateException(e);
        }
    }

    public String makeReadCommand() {
        String name = getName();
        String command = toString();
        command = command.substring(0, command.lastIndexOf(name));
        command += ":read-attribute(name=" + name + ")";
        return command;
    }

    @Override
    public String toString() {
        return path + attrib.getName();
    }
}
