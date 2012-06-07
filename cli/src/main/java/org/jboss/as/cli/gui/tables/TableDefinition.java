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

import java.util.ArrayList;
import java.util.List;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
public class TableDefinition {

    private ModelNode compositeOperation;
    private List<ModelNode> baseAddress;
    private List<AttributeType> attributes;

    private ModelNode definition;

    public TableDefinition(ModelNode compositeOperation, List<ModelNode> baseAddress, List<AttributeType> attributes) {
        this.compositeOperation = compositeOperation;
        this.baseAddress = baseAddress;
        this.attributes = attributes;
        this.definition = makeDefinition();
    }

    public TableDefinition(ModelNode definition) {
        this.definition = definition;
        setMembers();
    }

    public ModelNode getCompositeOperation() {
        return this.compositeOperation;
    }

    public List<ModelNode> getBaseAddress() {
        return this.baseAddress;
    }

    public List<AttributeType> getAttributes() {
        return this.attributes;
    }

    public ModelNode getDefinition() {
        return this.definition;
    }

    // parse the definition to set baseAddress, attributes, and definition
    private void setMembers() {
        this.compositeOperation = definition.get("operation");
        this.baseAddress = definition.get("baseAddress").asList();

        this.attributes = new ArrayList<AttributeType>();
        for (Property attrib : definition.get("attributes").asPropertyList()) {
            this.attributes.add(new SimpleAttributeType(attrib.getName(), attrib.getValue().asList()));
        }
    }

    private class SimpleAttributeType implements AttributeType {

        private String name;
        private List<ModelNode> address;

        SimpleAttributeType(String name, List<ModelNode> address) {
            this.name = name;
            this.address = address;
        }

        public List<ModelNode> getAddress() {
            return this.address;
        }

        public String getName() {
            return this.name;
        }

    }

    // make definition from compositeOperation, baseAddress, and attributes
    private ModelNode makeDefinition() {
        ModelNode def = new ModelNode();
        def.get("type").set("table");
        def.get("operation").set(compositeOperation);

        for (ModelNode address : baseAddress) def.get("baseAddress").add(address);

        ModelNode attribs = new ModelNode();

        for (AttributeType attrType : this.attributes) {
            attribs.get(attrType.getName()).setEmptyList();
            for (ModelNode address : attrType.getAddress()) {
                attribs.get(attrType.getName()).add(address);
            }
        }

        def.get("attributes").set(attribs);

        return def;
    }
}
