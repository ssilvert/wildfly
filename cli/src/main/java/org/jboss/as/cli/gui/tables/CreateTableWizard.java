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

    private SelectFirstColumnPanel panelOne;
    private SelectAttributesPanel panelTwo;

    private JPanel currentPanel;


    public CreateTableWizard(CliGuiContext cliGuiCtx) {
        super(cliGuiCtx.getMainWindow(), "Create Table", Dialog.ModalityType.APPLICATION_MODAL);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        contentPane.setLayout(new BorderLayout());

        panelOne = new SelectFirstColumnPanel(cliGuiCtx, this);
        panelTwo = new SelectAttributesPanel(cliGuiCtx, this);
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
            helpButton.setText("Help");

            panelTwo.setSelectedNode(panelOne.getSelectedNode());

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
                previousButton.setEnabled(false);
                helpButton.setEnabled(false);
                finishButton.setEnabled(false);

                contentPane.remove(panelTwo);
                contentPane.add(panelOne, BorderLayout.CENTER);
                contentPane.paintAll(contentPane.getGraphics());

                currentPanel = panelOne;
            }
        });

        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                nextButton.setEnabled(false);
                previousButton.setEnabled(true);
                helpButton.setEnabled(true);
                finishButton.setEnabled(!panelTwo.noAttributesSelected());

                contentPane.remove(panelOne);
                contentPane.add(panelTwo, BorderLayout.CENTER);
                contentPane.paintAll(contentPane.getGraphics());

                currentPanel = panelTwo;
            }
        });

        helpButton.setEnabled(false);
        helpButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (helpButton.getText().equals("Help")) {
                    helpButton.setText("Hide Help");
                } else {
                    helpButton.setText("Help");
                }

                panelTwo.toggleHelpText();
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
