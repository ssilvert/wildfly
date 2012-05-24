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
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import org.jboss.as.cli.CommandFormatException;
import org.jboss.as.cli.gui.CliGuiContext;
import org.jboss.as.cli.gui.CommandExecutor.Response;
import org.jboss.as.cli.gui.ManagementModelNode;
import org.jboss.as.cli.gui.component.WordWrapLabel;
import org.jboss.dmr.ModelNode;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
public class ReadAttributesTableModel extends DefaultTableModel {

    private CliGuiContext cliGuiCtx;
    private List<AttributeType> attributes;

    private ModelNode request;
    private ModelNode result;

    private TableCalculator tableCalc;

    public ReadAttributesTableModel(CliGuiContext cliGuiCtx, ManagementModelNode node, List<AttributeType> attributes) throws CommandFormatException, IOException {
        this.cliGuiCtx = cliGuiCtx;
        this.attributes = attributes;
        tableCalc = new TableCalculator(node.getAddress(), attributes);

        refresh();

//        tableCalc.dumpRows();
    }

    public final synchronized void refresh() throws CommandFormatException, IOException {
        Response response = cliGuiCtx.getExecutor().doCompositeCommand(makeReadAttrCommands(), false);
        request = response.getDmrRequest();

        ModelNode attempt = response.getDmrResponse();
        boolean success = attempt.get("outcome").asString().equals("success");
        if (success) {
            result = attempt;
            tableCalc.parseRows(result);
        } else {
            String failureDesc = attempt.get("failure-description").asString();
            WordWrapLabel failureLabel = new WordWrapLabel(failureDesc, 300);
            JOptionPane.showMessageDialog(cliGuiCtx.getMainWindow(), failureLabel, "Table creation failure", JOptionPane.ERROR_MESSAGE);
        }

   //     System.out.println("result=");
   //     System.out.println(attempt.toString());

    }

    ModelNode getCompositeCommand() {
        return request;
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

}
