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
import java.awt.Container;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import org.jboss.as.cli.gui.CliGuiContext;
import org.jboss.as.cli.gui.ManagementModelNode;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
public class CreateViewDialog extends JDialog {

    private CliGuiContext cliGuiCtx;
    private ManagementModelNode node;

    public CreateViewDialog(CliGuiContext cliGuiCtx, ManagementModelNode node) throws Exception {
        super(cliGuiCtx.getMainWindow(), "Create View", Dialog.ModalityType.APPLICATION_MODAL);
        this.cliGuiCtx = cliGuiCtx;
        this.node = node;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout(10, 10));


       // contentPane.add(opDescription, BorderLayout.NORTH);

        JComponent tree = makeTree();
        TableView tableView = new TableView(cliGuiCtx, node);
        JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tree, tableView);

        contentPane.add(splitter, BorderLayout.CENTER);

        contentPane.add(makeButtonPanel(), BorderLayout.SOUTH);
        pack();
        setResizable(true);
    }

    private JComponent makeTree() throws Exception {
        String command = node.addressPath() + ":read-resource-description(recursive=true)";
        ModelNode result = cliGuiCtx.getExecutor().doCommand(command);
        result = result.get("result").asList().get(0).get("result");

        DefaultMutableTreeNode root = new DefaultMutableTreeNode(node.getUserObject().toString());
        addChildren(root, result);

        JTree tree = new JTree(new DefaultTreeModel(root));
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        JScrollPane pane = new JScrollPane(tree);
        return pane;
    }

    private void addChildren(DefaultMutableTreeNode node, ModelNode result) {

        if (result.get("attributes").isDefined()) {
            for (Property attrib : result.get("attributes").asPropertyList()) {
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(attrib.getName());
                node.add(newNode);
            }
        }

        if (result.get("children").isDefined()) {
            for (Property child : result.get("children").asPropertyList()) {
                if (child.getValue().get("model-description").isDefined()) {
                    for (Property modelDescChild : child.getValue().get("model-description").asPropertyList()) {
                        DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(child.getName() + "=" + modelDescChild.getName());
                        node.add(childNode);
                        addChildren(childNode, modelDescChild.getValue());
                    }
                }
            }
        }


    }

    private JPanel makeButtonPanel() {
        JPanel buttonPanel = new JPanel();

        JButton ok = new JButton("OK");
        //ok.addActionListener(new SetOperationActionListener());

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                CreateViewDialog.this.dispose();
            }
        });

        buttonPanel.add(ok);
        buttonPanel.add(cancel);
        return buttonPanel;
    }
}
