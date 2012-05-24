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
package org.jboss.as.cli.gui.component;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
public class AuthorInfoPanel extends JPanel {

    private JTextField firstName = new JTextField(30);
    private JTextField lastName = new JTextField(30);
    private JTextField email = new JTextField(30);

    public AuthorInfoPanel() {
        setLayout(new GridBagLayout());
        setBorder(new TitledBorder("Author Information"));

        GridBagConstraints gbConst = new GridBagConstraints();
        gbConst.anchor = GridBagConstraints.WEST;
        gbConst.insets = new Insets(5,5,5,5);

        JLabel firstNameLabel = new JLabel("First Name:");
        gbConst.gridwidth = 1;
        add(firstNameLabel, gbConst);

        addStrut();
        gbConst.gridwidth = GridBagConstraints.REMAINDER;
        add(firstName, gbConst);

        JLabel lastNameLabel = new JLabel("Last Name:");
        gbConst.gridwidth = 1;
        add(lastNameLabel, gbConst);

        addStrut();
        gbConst.gridwidth = GridBagConstraints.REMAINDER;
        add(lastName, gbConst);

        JLabel emailLabel = new JLabel("Email Address:");
        gbConst.gridwidth = 1;
        add(emailLabel, gbConst);

        addStrut();
        gbConst.gridwidth = GridBagConstraints.REMAINDER;
        add(email, gbConst);
    }

    private void addStrut() {
        add(Box.createHorizontalStrut(5));
    }

    public String getEmail() {
        return email.getText();
    }

    public String getFirstName() {
        return firstName.getText();
    }

    public String getLastName() {
        return lastName.getText();
    }

}
