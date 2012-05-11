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
package org.jboss.as.cli.gui.views;

import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import org.jboss.as.cli.gui.CliGuiContext;
import org.jboss.as.cli.gui.ManagementModelNode;
import org.jboss.as.cli.gui.ManagementModelNode.UserObject;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
public class TableView extends JPanel {

    private CliGuiContext cliGuiCtx;
    private ManagementModelNode root;

    private DefaultTableModel tableModel;
    private JTable table = new JTable();
    private ModelNode readRsc;

    public TableView(CliGuiContext cliGuiCtx, ManagementModelNode root) throws Exception {
        this.cliGuiCtx = cliGuiCtx;
        this.root = root;

        JScrollPane scroller = new JScrollPane(table);
        add(scroller);
        refreshTable();
    }

    public final void refreshTable() throws Exception {
        this.tableModel = new DefaultTableModel();
        this.table.setModel(tableModel);
        readRsc = cliGuiCtx.getExecutor().doCommand(root.addressPath() + ":read-resource(recursive=true)");
        readRsc = readRsc.get("result");
        setMainColumn();
    }

    public void addColumn(String path) {

    }

    private void setMainColumn() {
        UserObject usrObj = (UserObject)root.getUserObject();
        String columnName = usrObj.getName();
        tableModel.addColumn(columnName);
        for (ModelNode resource : readRsc.asList()) {
            tableModel.addRow(new String[] {columnValueFromAddress(columnName, resource.get("address"))});
        }
    }

    private String columnValueFromAddress(String columnName, ModelNode address) {
        // the last value in the address will be the column value
        List<ModelNode> elements = address.asList();
        return elements.get(elements.size() - 1).get(columnName).asString();
    }


}
