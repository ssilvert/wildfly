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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.jboss.as.cli.CommandFormatException;
import org.jboss.as.cli.gui.ManagementModelNode;
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

    private List<AttributeType> attributes;

    private LinkedHashSet<String> uniquePaths = new LinkedHashSet<String>();

    private List<String> addrColumnNames;

    private List<String> columnNames;

    private Map<String, Integer> columnNumbers = new HashMap<String, Integer>();

    private List<ModelNode> rows = new ArrayList<ModelNode>();

    public TableCalculator(ManagementModelNode node, List<AttributeType> attributes) throws CommandFormatException {
        this.baseAddress = node.getParentAddress();
        this.attributes = attributes;

        for (AttributeType attrType : attributes) {
            uniquePaths.add(attrType.getPath());
        }

        // linkedColNames = unique paths + attribute names
        LinkedHashSet<String> linkedColNames = new LinkedHashSet<String>();
        for (String path : uniquePaths) {
            for (String pathElement : path.split("/")) { // TODO: need to check for escaped '/' in path?
                if (!pathElement.equals("")) linkedColNames.add(pathElement);
            }
        }

        addrColumnNames = new ArrayList<String>(linkedColNames);

        for (AttributeType attrType : attributes) {
            linkedColNames.add(attrType.getName());
        }

        columnNames = new ArrayList<String>(linkedColNames);

        for (int i=0; i < columnNames.size(); i++) {
            columnNumbers.put(columnNames.get(i), i);
        }

        System.out.println("empty address =");
        for (ModelNode addr : makeEmptyAddress()) {
            System.out.println(addr.toString());
        }
    }

    public int getColumnCount() {
        if (columnNames == null) return 0;
        return columnNames.size();
    }

    public int getRowCount() {
        if (rows == null) return 0;
        return rows.size();
    }

    public String getColumnName(int column) {
        return columnNames.get(column);
    }

    public int getColumnNumber(String columnName) {
        return columnNumbers.get(columnName);
    }

    public ModelNode getValueAt(int row, int column) {
        ModelNode rowNode = rows.get(row);
        String colName = getColumnName(column);

        if (isAddressColumn(column)) {
            colName = colName.substring(0, colName.indexOf('='));
            ModelNode addrMember = findAddressMember(colName, rowNode.get("address").asList());
            return addrMember.get(colName);
        }

        return rowNode.get(colName);
    }

    private boolean isAddressColumn(int columnNumber) {
        return columnNumber < addrColumnNames.size();
    }

    void parseRows(ModelNode result) {
        int attrIndex = 0;
        System.out.println("empty address=");
        System.out.println(makeEmptyAddress());
        for (Property step : result.get("result").asPropertyList()) {
            if (step.getValue().get("result").getType() != ModelType.LIST) {
                processRow(attrIndex, step.getValue());
            } else {
                for (ModelNode readAttrResult : step.getValue().get("result").asList()) {
                    processRow(attrIndex, readAttrResult);
                }
            }

            attrIndex++;
        }

    }

    private void processRow(int attrIndex, ModelNode readAttrResult) {
        AttributeType attr = attributes.get(attrIndex);
        ModelNode row;
        if (readAttrResult.get("address").isDefined()) {
            List<ModelNode> compAddr = makeComparableAddress(readAttrResult.get("address").asList());
            row = findRowForAddress(compAddr);
        } else {
            row = findRowForAddress(makeEmptyAddress());
        }

        row.get(attr.getName()).set(readAttrResult.get("result"));
    }

    void dumpRows() {
        System.out.println("Row Dump:");
        for (int i=0; i < rows.size(); i++) {
            System.out.println("row " + i);
            System.out.println(rows.get(i).toString());
        }
    }

    // Use the addrList to fill in the values of the empty address
    // This makes an address that can be compared for findRowForAddress()
    private List<ModelNode> makeComparableAddress(List<ModelNode> addrList) {
        List<ModelNode> comparableAddress = makeEmptyAddress();
        for (ModelNode addr : comparableAddress) {
            String address = addr.asProperty().getName();
            ModelNode valueFromResponse = findAddressMember(address, addrList);
            if (valueFromResponse != null) {
                findAddressMember(address, comparableAddress).get(address).set(valueFromResponse.get(address));
            }
        }

        return comparableAddress;
    }

    private ModelNode findAddressMember(String address, List<ModelNode> addrList) {
        for (ModelNode addr : addrList) {
            if (addr.has(address)) return addr;
        }

        return null;
    }

    // creates an address list with empty Strings for all unknown address values
    private List<ModelNode> makeEmptyAddress() {
        ModelNode address = new ModelNode();

        for (String colName : addrColumnNames) {
            ModelNode colAddress = new ModelNode();
            if (colName.endsWith("=*")) {
                colAddress.get(colName.substring(0, colName.indexOf("=*"))); // set as undefined
            } else {
                colAddress.get(colName.substring(0, colName.indexOf('='))).set(colName.substring(colName.indexOf('=') + 1));
            }
            address.add(colAddress);
        }

        return address.asList();
    }

    private ModelNode findRowForAddress(List<ModelNode> address) {
        for (ModelNode row : rows) {
            List<ModelNode> rowAddress = row.get("address").asList();
            if (isAddressEqual(rowAddress, address)) return row;
        }

        ModelNode newRow = new ModelNode();
        newRow.get("address").set(address);
        rows.add(newRow);
        return newRow;
    }

    private boolean isAddressEqual(List<ModelNode> addr1, List<ModelNode> addr2) {
        if (addr1.size() != addr2.size()) throw new IllegalArgumentException("addr1 and addr2 must be the same size.");

        for (int i=0; i < addr1.size(); i++) {
            Property addrElement1 = addr1.get(i).asProperty();
            Property addrElement2 = addr2.get(i).asProperty();
            if (!(addrElement1.getName().equals(addrElement2.getName()) &&
                  addrElement1.getValue().asString().equals(addrElement2.getValue().asString()))) {
                return false;
            }
        }

        return true;
    }

}
