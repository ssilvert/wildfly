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
import org.jboss.dmr.ModelType;
import org.jboss.dmr.Property;

/**
 * Manages the list of TableCalculator.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
class TableCalculator {

    // The address of everything before the AttributeType's path
    private List<ModelNode> baseAddress;

    private List<Column> addressColumns;

    // attribute columns
    private List<AttributeType> attributes;

    private List<ModelNode> rows;

    public TableCalculator(List<ModelNode> baseAddress, List<AttributeType> attributes) {
        this.baseAddress = baseAddress;
        this.attributes = attributes;
    }

    public int getColumnCount() {
        if (this.addressColumns == null) return 0;
        if (this.attributes == null) return 0;
        return this.addressColumns.size() + this.attributes.size();
    }

    public int getRowCount() {
        if (this.rows == null) return 0;
        return this.rows.size();
    }

    public String getColumnName(int column) {
        if (column < addressColumns.size()) {
            return addressColumns.get(column).getName();
        } else {
            return attributes.get(column - addressColumns.size()).getName();
        }
    }

    public ModelNode getValueAt(int rowNum, int column) {
        ModelNode row = rows.get(rowNum);
        if (column < addressColumns.size()) {
            return getAddressColumnValue(column, row);
        } else {
            return row.get(getColumnName(column));
        }
    }

    private ModelNode getAddressColumnValue(int column, ModelNode row) {
        Column addrColumn = this.addressColumns.get(column);
        List<ModelNode> rowAddress = row.get("address").asList();
        int addrElementPosition = addrColumn.getResourceCount() - 1;

        // account for values that should be empty
        if (column > rowAddress.size() - 1) return new ModelNode();
        Property addrElement = rowAddress.get(addrElementPosition).asProperty();
        if (!addrElement.getName().equals(addrColumn.getName())) return new ModelNode();

        return rowAddress.get(addrElementPosition).asProperty().getValue();
    }

    void parseRows(ModelNode result) {
        this.rows = new ArrayList<ModelNode>();
        this.addressColumns = new ArrayList<Column>();

        List<Property> steps = result.get("result").asPropertyList();
        for (int i = 0; i < steps.size(); i++) {
            Property step = steps.get(i);
            AttributeType attrType = attributes.get(i);
            if (step.getValue().get("result").getType() != ModelType.LIST) {
                ModelNode row = step.getValue();
                row.get("address").set(attrType.getAddress());
                parseRow(attrType, row);
            } else {
                for (ModelNode row : step.getValue().get("result").asList()) {
                    parseRow(attrType, row);
                }
            }
        }

      //  this.dumpRows();
      //  dumpAddressColumns();
    }

    private void parseRow(AttributeType attrType, ModelNode stepRow) {
        if (isAddressUnique(stepRow.get("address"))) {
            addRow(stepRow);
        }

        ModelNode permanentRow = getPermanentRow(stepRow);
        permanentRow.get(attrType.getName()).set(stepRow.get("result"));
    }

    private boolean isAddressUnique(ModelNode address) {
        for (ModelNode row : rows) {
            if (row.get("address").equals(address)) return false;
        }

        return true;
    }

    private void addRow(ModelNode row) {
        ModelNode newRow = new ModelNode();
        newRow.get("address").set(row.get("address").clone());
        rows.add(newRow);
        addAddressColumns(newRow);
    }

    // If the new row also has a new address path, add it
    private void addAddressColumns(ModelNode newRow) {
        String resourcePath = "/";
        List<ModelNode> addressElements = newRow.get("address").asList();
        for (int i=0; i < addressElements.size(); i++ ) {
            String resource = addressElements.get(i).asProperty().getName();
            resourcePath += resource + "/";

            if (i < baseAddress.size() - 1) continue;

            Column newColumn = new Column(resourcePath, resource, i + 1);
            addAddressColumn(newColumn);
        }
    }

    // if the column does not already exist, add it
    private void addAddressColumn(Column newColumn) {
        for (Column column : this.addressColumns) {
            if (column.equals(newColumn)) return;
        }

        this.addressColumns.add(newColumn);
    }

    // finds the permanent permanentRow with the same address as the stepRow
    private ModelNode getPermanentRow(ModelNode stepRow) {
        for (ModelNode permanentRow : rows) {
            if (permanentRow.get("address").equals(stepRow.get("address"))) {
                return permanentRow;
            }
        }

        throw new IllegalArgumentException("Row not found for stepRow address=" + stepRow.get("address"));
    }

    private class Column {
        private String resourcePath;
        private String name;
        private int resourceCount; // the number of resources in the resourcePath

        public Column(String resourcePath, String name, int resourceCount) {
            this.resourcePath = resourcePath;
            this.name = name;
            this.resourceCount = resourceCount;
        }

        public String getName() {
            return this.name;
        }

        public String getResourcePath() {
            return this.resourcePath;
        }

        public int getResourceCount() {
            return this.resourceCount;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Column)) return false;
            Column comparedColumn = (Column)obj;
            return this.resourcePath.equals(comparedColumn.resourcePath);
        }

        @Override
        public int hashCode() {
            return this.resourcePath.hashCode();
        }

    }

    void dumpRows() {
        System.out.println("Row Dump:");
        for (int i=0; i < rows.size(); i++) {
            System.out.println("row " + i);
            System.out.println(rows.get(i).toString());
        }
    }

    void dumpAddressColumns() {
        System.out.println("Column Dump:");
        for (int i=0; i < addressColumns.size(); i++) {
            System.out.println("addr column " + i);
            System.out.println(addressColumns.get(i).getName());
            System.out.println(addressColumns.get(i).getResourcePath());
            System.out.println("resource count=" + addressColumns.get(i).getResourceCount());
        }
    }

}
