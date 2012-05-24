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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.jboss.as.cli.gui.component.AuthorInfoPanel;
import org.jboss.as.cli.gui.component.WordWrapLabel;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
public class SelectViewNamePanel extends JPanel {
    //TODO: not sure why I need <html> since WordWrapLabel already embeds it
    private static final String SELECT_NAME_HELP = "<html><font size='4'>Select a table name.  This will also be the tab name.</font></html>";
    private static final String AUTHOR_INFO_HELP = "<html><font size='4'>Author info is optional, but recommended if you will be sharing your table view.</font></html>";

    private WordWrapLabel selectNameHelp = new WordWrapLabel(SELECT_NAME_HELP, 500);
    private WordWrapLabel authorInfoHelp = new WordWrapLabel(AUTHOR_INFO_HELP, 500);

    private ItemSelectionListener wizard;

    private JTextField nameField = new JTextField(30);

    public SelectViewNamePanel(ItemSelectionListener wizard) {
        setLayout(new GridBagLayout());
        this.wizard = wizard;
        addNameFieldListener();

        GridBagConstraints gbConst = new GridBagConstraints();
        gbConst.anchor = GridBagConstraints.WEST;
        gbConst.insets = new Insets(5,5,5,5);
        gbConst.gridwidth = GridBagConstraints.REMAINDER;
        add(selectNameHelp, gbConst);

        gbConst.gridwidth = 1;
        JLabel tableNameLabel = new JLabel("Table Name:");
        add(tableNameLabel, gbConst);

        gbConst.gridwidth = GridBagConstraints.REMAINDER;
        add(nameField, gbConst);

        add(Box.createHorizontalStrut(100), gbConst);

        add(authorInfoHelp, gbConst);

        add(new AuthorInfoPanel(), gbConst);
    }

    private void addNameFieldListener() {
        nameField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {

            }

            public void insertUpdate(DocumentEvent e) {
                wizard.itemSelected();
            }

            public void removeUpdate(DocumentEvent e) {
                if (e.getDocument().getLength() == 0) wizard.noItemSelected();
            }
        });
    }

    public String getViewName() {
        return this.nameField.getText();
    }
}
