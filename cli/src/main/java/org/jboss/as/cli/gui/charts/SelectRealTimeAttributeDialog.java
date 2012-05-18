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
package org.jboss.as.cli.gui.charts;
import javax.swing.JScrollPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import org.jboss.as.cli.gui.ManagementModel;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jboss.as.cli.gui.CliGuiContext;
import org.jboss.as.cli.gui.ManagementModelNode;
import org.jboss.as.cli.gui.ManagementModelNode.ChildAcceptor;
import org.jboss.as.cli.gui.ManagementModelNode.UserObject;
import org.jboss.dmr.ModelNode;

/**
 * This dialog presents a pared-down version of the management tree filtering out
 * nodes that can not contain real time attributes.  Once the user has selected a
 * real time attribute it creates a graph for it.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
public class SelectRealTimeAttributeDialog extends JDialog {

    private static final String CHART_HELP ="<html><font size='4'>A real-time graphable attribute is deonted with the \u2245 symbol.<br>" +
                                             "Select a graphable attribute and press OK.</font></html>";

    private final ChildAcceptor GRAPHABLES_ACCEPTOR = new GraphablesChildAcceptor();
    private final TreeSelectionListener SELECTION_LISTENER = new LeavesOnlySelectionListener();

    private CliGuiContext cliGuiCtx;
    private ManagementModel mgtModel;

    public SelectRealTimeAttributeDialog(CliGuiContext cliGuiCtx) {
        super(cliGuiCtx.getMainWindow(), "Create Chart", Dialog.ModalityType.APPLICATION_MODAL);
        this.cliGuiCtx = cliGuiCtx;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(10, 10));

        JComponent tree = makeTree();

        contentPane.add(tree, BorderLayout.CENTER);

        contentPane.add(makeButtonPanel(), BorderLayout.SOUTH);
        pack();
        setSize((int)(cliGuiCtx.getMainWindow().getWidth() * 0.75), cliGuiCtx.getMainWindow().getHeight());
        setResizable(true);
    }

    private class GraphablesChildAcceptor implements ChildAcceptor {

        public boolean accept(ManagementModelNode node) {
            UserObject usrObj = (UserObject)node.getUserObject();
            if (usrObj.isGeneric()) return false;

            // we know extensions and paths don't have graphable attributes
            if (usrObj.getName().equals("extension")) return false;
            if (usrObj.getName().equals("path")) return false;

            if (!usrObj.isLeaf()) return hasRuntimeAttributes(node);
            if (usrObj.getAttributeProps().isGraphable()) return true;
            return false;
        }

        // Make best effort to determine if there are graphable attributes below this node.
        private boolean hasRuntimeAttributes(ManagementModelNode node) {
            try {
                ModelNode rscDesc = cliGuiCtx.getExecutor().doCommand(node.addressPath() + ":read-resource-description(recursive=true)");
                if (rscDesc.get("outcome").asString().equals("failed")) return true; // might still be true
                return rscDesc.toString().contains("\"storage\" => \"runtime\"");
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

    }

    private class LeavesOnlySelectionListener implements TreeSelectionListener {
        public void valueChanged(TreeSelectionEvent e) {
            ManagementModelNode node = (ManagementModelNode)e.getPath().getLastPathComponent();
            if (!node.isLeaf()) {
                mgtModel.getTree().setSelectionPath(null);
            }
        }
    }

    private JComponent makeTree() {
        mgtModel = new ManagementModel(cliGuiCtx, CHART_HELP, GRAPHABLES_ACCEPTOR, SELECTION_LISTENER, null);
        JScrollPane pane = new JScrollPane(mgtModel);
        return pane;
    }

    private JPanel makeButtonPanel() {
        JPanel buttonPanel = new JPanel();

        JButton ok = new JButton("OK");
        ok.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                ManagementModelNode selectedNode = (ManagementModelNode)mgtModel.getTree().getSelectionPath().getLastPathComponent();
                if (selectedNode == null) return;
                if (!selectedNode.isLeaf()) return;

                CreateAChartDialog dialog = new CreateAChartDialog(cliGuiCtx, selectedNode);
                SelectRealTimeAttributeDialog.this.dispose();
                dialog.setLocationRelativeTo(cliGuiCtx.getMainWindow());
                dialog.setVisible(true);
            }

        });

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                SelectRealTimeAttributeDialog.this.dispose();
            }
        });

        buttonPanel.add(ok);
        buttonPanel.add(cancel);
        return buttonPanel;
    }
}
