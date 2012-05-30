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
package org.jboss.as.views;

import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.operations.validation.StringLengthValidator;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelType;

/**
 * SubModel definition for views.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
public class ViewDefinition extends SimpleResourceDefinition {

    ViewDefinition() {
        super(PathElement.pathElement("view"),
                SubsystemExtension.getResourceDescriptionResolver("view"),
                new AddViewHandler(), new RemoveViewHandler()
        );
    }

    protected static final SimpleAttributeDefinition DEFINITION_ATTRIBUTE =
            new SimpleAttributeDefinitionBuilder("definition", ModelType.STRING, false)
                    .setXmlName("definition")
                    .setAllowExpression(false)
                    .setValidator(new StringLengthValidator(1))
                    .build();

    // TODO: figure out why StringLengthValidator isn't working

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerReadWriteAttribute(DEFINITION_ATTRIBUTE, null, new SimpleWriteAttributeHandler());
    }

}
