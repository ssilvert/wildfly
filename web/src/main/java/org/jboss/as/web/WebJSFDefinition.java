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
package org.jboss.as.web;

import static org.jboss.as.web.WebMessages.MESSAGES;

import java.util.ArrayList;
import java.util.List;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.ReloadRequiredWriteAttributeHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.operations.validation.AllowedValuesValidator;
import org.jboss.as.controller.operations.validation.ModelTypeValidator;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.web.deployment.JsfVersionMarker;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
public class WebJSFDefinition extends SimpleResourceDefinition {
    public static final WebJSFDefinition INSTANCE = new WebJSFDefinition();

    protected static final SimpleAttributeDefinition JSF_IMPL =
            new SimpleAttributeDefinitionBuilder(Constants.JSF_IMPL, ModelType.STRING, true)
                    .setXmlName(Constants.JSF_IMPL)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setValidator(new JSFImplNameValidator())
                    .setDefaultValue(new ModelNode(JsfVersionMarker.Mojarra_2_x))
                    .build();

    protected static class JSFImplNameValidator extends ModelTypeValidator implements AllowedValuesValidator {
        private List<ModelNode> allowedValues = new ArrayList<ModelNode>();

        protected JSFImplNameValidator() {
            super(ModelType.STRING, false, true);
            allowedValues.add(new ModelNode(JsfVersionMarker.Mojarra_1_2));
            allowedValues.add(new ModelNode(JsfVersionMarker.Mojarra_2_x));
            allowedValues.add(new ModelNode(JsfVersionMarker.MyFaces_2_x));
            allowedValues.add(new ModelNode(JsfVersionMarker.WAR_BUNDLES_JSF_IMPL));
        }

        @Override
        public void validateParameter(String parameterName, ModelNode value) throws OperationFailedException {
            super.validateParameter(parameterName, value);

            for (ModelNode allowedValue : allowedValues) {
                if (allowedValue.equals(value)) return;
            }

            throw new OperationFailedException(new ModelNode().set(MESSAGES.unknownJSFImplException(value.asString())));
        }

        public List<ModelNode> getAllowedValues() {
            return allowedValues;
        }
    }

    protected static final SimpleAttributeDefinition[] JSF_ATTRIBUTES = {
            // IMPORTANT -- keep these in xsd order as this order controls marshalling
            JSF_IMPL
    };

    private WebJSFDefinition() {
        super(WebExtension.JSF_CONFIGURATION_PATH,
                WebExtension.getResourceDescriptionResolver("configuration.jsf"),
                WebJSFConfigurationAdd.INSTANCE,
                new ReloadRequiredRemoveStepHandler());
    }


    @Override
    public void registerAttributes(ManagementResourceRegistration jsf) {
        for (SimpleAttributeDefinition def : JSF_ATTRIBUTES) {
            jsf.registerReadWriteAttribute(def, null, new ReloadRequiredWriteAttributeHandler(def));
        }
    }
}
