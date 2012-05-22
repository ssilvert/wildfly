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
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jboss.as.cli.gui.CliGuiContext;
import org.jboss.as.cli.gui.ViewManager;

/**
 * This dialog presents a pared-down version of the management tree filtering out
 * nodes that can not contain real time attributes.  Once the user has selected a
 * real time attribute it creates a graph for it.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
public class CreateTableWizard extends JDialog implements ItemSelectionListener {

    private Container contentPane = getContentPane();

    private JButton previousButton = new JButton("< Previous");
    private JButton nextButton = new JButton("Next >");
    private JButton finishButton = new JButton("Finish");
    private JButton helpButton = new JButton("Help");

    private SelectViewNamePanel panelOne;
    private SelectFirstColumnPanel panelTwo;
    private SelectAttributesPanel panelThree;

    private JPanel currentPanel;

    private CliGuiContext cliGuiCtx;


    public CreateTableWizard(CliGuiContext cliGuiCtx) {
        super(cliGuiCtx.getMainWindow(), "Create Table", Dialog.ModalityType.APPLICATION_MODAL);
        this.cliGuiCtx = cliGuiCtx;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        contentPane.setLayout(new BorderLayout());

        panelOne = new SelectViewNamePanel(this);
        panelTwo = new SelectFirstColumnPanel(cliGuiCtx, this);
        panelThree = new SelectAttributesPanel(cliGuiCtx, this);

        currentPanel = panelOne;

        contentPane.add(panelOne, BorderLayout.CENTER);

        noItemSelected();
        contentPane.add(makeButtonPanel(), BorderLayout.SOUTH);
        pack();
        setSize((int)(cliGuiCtx.getMainWindow().getWidth() * 0.75), cliGuiCtx.getMainWindow().getHeight());
        setResizable(true);
    }

    @Override
    public final void itemSelected() {
        if (currentPanel == panelOne) {
            previousButton.setEnabled(false);
            nextButton.setEnabled(true);
            finishButton.setEnabled(false);
            return;
        }

        if (currentPanel == panelTwo) {
            previousButton.setEnabled(true);
            nextButton.setEnabled(true);
            finishButton.setEnabled(false);
            helpButton.setText("Help");

            panelThree.setSelectedNode(panelTwo.getSelectedNode());

            return;
        }

        finishButton.setEnabled(true);
    }

    @Override
    public final void noItemSelected() {
        finishButton.setEnabled(false);

        if (currentPanel == panelOne) {
            previousButton.setEnabled(false);
            nextButton.setEnabled(false);
            return;
        }

        if (currentPanel == panelTwo) {
            previousButton.setEnabled(true);
            nextButton.setEnabled(false);
            return;
        }

    }

    private JPanel makeButtonPanel() {
        JPanel buttonPanel = new JPanel();

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                CreateTableWizard.this.dispose();
            }
        });

        previousButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                nextButton.setEnabled(true);
                finishButton.setEnabled(false);

                if (currentPanel == panelTwo) {
                    currentPanel = panelOne;
                    previousButton.setEnabled(false);
                    helpButton.setVisible(false);
                    contentPane.remove(panelTwo);
                    contentPane.add(panelOne, BorderLayout.CENTER);
                }

                if (currentPanel == panelThree) {
                    currentPanel = panelTwo;
                    helpButton.setVisible(false);
                    contentPane.remove(panelThree);
                    contentPane.add(panelTwo, BorderLayout.CENTER);
                }

                contentPane.paintAll(contentPane.getGraphics());
            }
        });

        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (currentPanel == panelTwo) {
                    currentPanel = panelThree;
                    nextButton.setEnabled(false);
                    previousButton.setEnabled(true);
                    helpButton.setVisible(true);
                    finishButton.setEnabled(!panelThree.noAttributesSelected());

                    contentPane.remove(panelTwo);
                    contentPane.add(panelThree, BorderLayout.CENTER);
                }

                if (currentPanel == panelOne) {
                    currentPanel = panelTwo;
                    nextButton.setEnabled(panelTwo.getSelectedNode() != null);
                    previousButton.setEnabled(true);
                    helpButton.setVisible(false);
                    finishButton.setEnabled(false);

                    contentPane.remove(panelOne);
                    contentPane.add(panelTwo, BorderLayout.CENTER);
                }

                contentPane.paintAll(contentPane.getGraphics());
            }
        });

        helpButton.setVisible(false);
        helpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (helpButton.getText().equals("Help")) {
                    helpButton.setText("Hide Help");
                } else {
                    helpButton.setText("Help");
                }

                panelThree.toggleHelpText();
            }
        });

        finishButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ViewManager viewMgr = cliGuiCtx.getViewManager();

                try {
                    TableView tableView = new TableView(cliGuiCtx,
                                                        panelOne.getName(),
                                                        panelTwo.getSelectedNode(),
                                                        panelThree.getSelectedAttributes());
                    viewMgr.addView(panelOne.getViewName(), tableView);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                CreateTableWizard.this.dispose();
            }
        });

        buttonPanel.add(previousButton);
        buttonPanel.add(nextButton);
        buttonPanel.add(finishButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(helpButton);
        return buttonPanel;
    }
}
