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
import org.jboss.as.cli.gui.CommandExecutor.Response;
import org.jboss.as.cli.gui.ManagementModelNode;
import org.jboss.as.cli.gui.component.MsgDialog;
import org.jboss.dmr.ModelNode;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
public class ReadAttributesTableModel extends DefaultTableModel {

    private CliGuiContext cliGuiCtx;
    private List<AttributeTypeImpl> attributes;

    // version of attributes using the interface
    private List<AttributeType> attrList;

    private ModelNode request;
    private ModelNode result;

    private TableCalculator tableCalc;

    private List<ModelNode> baseAddress;

    public ReadAttributesTableModel(CliGuiContext cliGuiCtx, ManagementModelNode node, List<AttributeTypeImpl> attributes) throws CommandFormatException, IOException {
        this.cliGuiCtx = cliGuiCtx;
        this.baseAddress = node.getAddress();
        this.attributes = attributes;
        this.attrList = new ArrayList<AttributeType>(attributes);
        tableCalc = new TableCalculator();

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
            tableCalc.parseRows(baseAddress, attrList, result);
        } else {
            MsgDialog.showDMRFailure(cliGuiCtx, attempt, "Table creation failure");
        }

   //     System.out.println("result=");
   //     System.out.println(attempt.toString());

    }

    ModelNode getCompositeCommand() {
        return request;
    }

    List<AttributeType> getAttributeList() {
        return this.attrList;
    }

    List<ModelNode> getBaseAddress() {
        return this.baseAddress;
    }

    private List<String> makeReadAttrCommands() {
        List<String> commands = new ArrayList<String>();
        for (AttributeTypeImpl attr : attributes) {
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
