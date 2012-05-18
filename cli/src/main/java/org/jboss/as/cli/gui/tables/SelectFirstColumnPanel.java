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

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import org.jboss.as.cli.gui.CliGuiContext;
import org.jboss.as.cli.gui.ManagementModel;
import org.jboss.as.cli.gui.ManagementModelNode;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
public class SelectFirstColumnPanel extends JPanel {

    private static final String SELECT_FIRST_COLUMN_HELP ="<html><font size='4'>Select a resource to be the first column of your table.<br/>" +
                                             "Typically you should choose a queryable resource (=*) as your first column.</font></html>";

    private final TreeSelectionListener SELECTION_LISTENER = new ExcludeLeavesSelectionListener();

    private ManagementModel mgtModel;
    private ItemSelectionListener wizard;

    public SelectFirstColumnPanel(CliGuiContext cliGuiCtx, ItemSelectionListener wizard) {
        setLayout(new BorderLayout());
        this.wizard = wizard;
        mgtModel = new ManagementModel(cliGuiCtx, SELECT_FIRST_COLUMN_HELP, ManagementModelNode.DEFAULT_ACCEPTOR, SELECTION_LISTENER, null);
        add(mgtModel, BorderLayout.CENTER);
    }

    public ManagementModelNode getSelectedNode() {
        JTree tree = mgtModel.getTree();
        if (tree.getSelectionPath() == null) return null;
        return (ManagementModelNode)tree.getSelectionPath().getLastPathComponent();
    }

    private class ExcludeLeavesSelectionListener implements TreeSelectionListener {
        public void valueChanged(TreeSelectionEvent e) {
            ManagementModelNode node = (ManagementModelNode)e.getPath().getLastPathComponent();
            if (node.isLeaf()) {
                mgtModel.getTree().setSelectionPath(null);
                wizard.noItemSelected();
                return;
            }

            wizard.itemSelected();
        }
    }
}
