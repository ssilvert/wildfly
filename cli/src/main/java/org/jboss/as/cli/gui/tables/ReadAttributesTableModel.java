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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import org.jboss.as.cli.CommandFormatException;
import org.jboss.as.cli.gui.CliGuiContext;
import org.jboss.as.cli.gui.ManagementModelNode;
import org.jboss.as.cli.gui.ManagementModelNode.UserObject;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
public class ReadAttributesTableModel extends DefaultTableModel {

    private CliGuiContext cliGuiCtx;
    private ManagementModelNode node;
    private List<AttributeType> attributes;

    private ModelNode result;

    private TableCalculator tableCalc;

    public ReadAttributesTableModel(CliGuiContext cliGuiCtx, ManagementModelNode node, List<AttributeType> attributes) throws CommandFormatException, IOException {
        this.cliGuiCtx = cliGuiCtx;
        this.node = node;
        this.attributes = attributes;
        tableCalc = new TableCalculator(node, attributes);

        refresh();

//        tableCalc.dumpRows();
    }
/*
    private void testAttrTypes() throws CommandFormatException {
        TableCalculator attrTypes = new TableCalculator(node, attributes);
        System.out.println("column count = " + attrTypes.getColumnCount());
        for (int i=0; i < attrTypes.getColumnCount(); i++) {
            System.out.println("|" + attrTypes.getColumnName(i) + "|");
        }
    }
*/
    public final synchronized void refresh() throws CommandFormatException, IOException {
        ModelNode attempt = cliGuiCtx.getExecutor().doCompositeCommand(makeReadAttrCommands(), false);
        boolean success = attempt.get("outcome").asString().equals("success");
        if (success) {
            result = attempt;
            tableCalc.parseRows(result);
        }

        System.out.println("result=");
        System.out.println(attempt.toString());

    }

    private List<String> makeReadAttrCommands() {
        List<String> commands = new ArrayList<String>();
        for (AttributeType attr : attributes) {
            commands.add(attr.makeReadCommand());
        }
        return commands;
    }

    @Override
    public Object getValueAt(int row, int column) {
        ModelNode value = tableCalc.getValueAt(row, column);

        if (value.isDefined()) return value.asString();

        return "";
    }

    private List<ModelNode> getColumn(int column) {
        return result.get("result", "step-" + column, "result").asList();
    }

    private String getColumnZeroValue(int row) {
        List<ModelNode> col1 = getColumn(1);
        ModelNode rowNode = col1.get(row);
        for (ModelNode address : rowNode.get("address").asList()) {
            Property addrProp = address.asProperty();
            if (addrProp.getName().equals(getColumnName(0))) {
                return addrProp.getValue().asString();
            }
        }

        throw new IllegalStateException("Can't find column 0 value for row " + row);
    }

    @Override
    public int getColumnCount() {
        if (tableCalc == null) return 0;
        return tableCalc.getColumnCount();
    }

    @Override
    public String getColumnName(int column) {
        return tableCalc.getColumnName(column);
    }

    @Override
    public int getRowCount() {
        if (tableCalc == null) return 0;
        return tableCalc.getRowCount();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    private UserObject getUserObject() {
        return (UserObject)node.getUserObject();
    }

}
