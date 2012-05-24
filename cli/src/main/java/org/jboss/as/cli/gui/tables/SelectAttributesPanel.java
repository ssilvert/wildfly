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
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jboss.as.cli.gui.CliGuiContext;
import org.jboss.as.cli.gui.ManagementModelNode;
import org.jboss.as.cli.gui.component.WordWrapLabel;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
public class SelectAttributesPanel extends JPanel {

    private static final String SELECT_ATTRS_HELP ="Select the column attributes for ";

    private CliGuiContext cliGuiCtx;
    private ManagementModelNode selectedNode;
    private ItemSelectionListener wizard;

    private WordWrapLabel selectedNodeLabel = new WordWrapLabel("", 500);

    private List<AttributeCheckBox> attributeCheckBoxes;
    private List<WordWrapLabel> helpTextLabels;

    private JScrollPane scroller = new JScrollPane();

    private boolean helpShowing = false;

    public SelectAttributesPanel(CliGuiContext cliGuiCtx, ItemSelectionListener wizard) {
        setLayout(new BorderLayout());
        this.cliGuiCtx = cliGuiCtx;
        this.wizard = wizard;

        add(selectedNodeLabel, BorderLayout.NORTH);

        add(scroller, BorderLayout.CENTER);
    }

    public List<AttributeType> getSelectedAttributes() {
        List<AttributeType> attrList = new ArrayList<AttributeType>();
        for (AttributeCheckBox attrCheckBox : attributeCheckBoxes) {
            if (attrCheckBox.isSelected()) attrList.add(attrCheckBox.getAttrType());
        }
        return attrList;
    }

    public void setSelectedNode(ManagementModelNode selectedNode) {
        this.selectedNode = selectedNode;
        this.helpShowing = false;

        //TODO: not sure why I need <html> since WordWrapLabel already embeds it
        this.selectedNodeLabel.setText("<html><font size='4'>" + SELECT_ATTRS_HELP + "<b>" + selectedNode.addressPath() + "</b><br/>Press Help button for attribute descriptions.</font></html>");

        attributeCheckBoxes = new ArrayList<AttributeCheckBox>();
        helpTextLabels = new ArrayList<WordWrapLabel>();
        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
        for (AttributeType attrType : findAttributesForSelection()) {
            AttributeCheckBox attrCheckBox = new AttributeCheckBox(attrType);
            attrCheckBox.setToolTipText(attrType.getHelpText());
            attributeCheckBoxes.add(attrCheckBox);

            WordWrapLabel helpText = new WordWrapLabel("<b>" + attrType.getHelpText() + "</b>", 500);
            helpTextLabels.add(helpText);
            helpText.setVisible(false);

            checkBoxPanel.add(Box.createVerticalStrut(10));
            checkBoxPanel.add(helpText);
            checkBoxPanel.add(attrCheckBox);
        }
        scroller.setViewportView(checkBoxPanel);

    }

    public void toggleHelpText() {
        helpShowing = !helpShowing;

        for (WordWrapLabel helpText : helpTextLabels) {
            helpText.setVisible(helpShowing);
        }
    }

    public class AttributeCheckBox extends JCheckBox implements ChangeListener {

        private AttributeType attrType;

        public AttributeCheckBox(AttributeType attrType) {
            super(attrType.toString());
            this.attrType = attrType;
            addChangeListener(this);
        }

        public AttributeType getAttrType() {
            return this.attrType;
        }

        public void stateChanged(ChangeEvent e) {
            if (this.isSelected()) {
                wizard.itemSelected();
                return;
            }

            if (noAttributesSelected()) {
                wizard.noItemSelected();
            }
        }
    }

    public boolean noAttributesSelected() {
        for (AttributeCheckBox checkBox : attributeCheckBoxes) {
            if (checkBox.isSelected()) return false;
        }

        return true;
    }

    private List<AttributeType> findAttributesForSelection() {
        ModelNode rscDesc = new ModelNode();
        try {
            rscDesc = cliGuiCtx.getExecutor().doCommand(selectedNode.addressPath() + ":read-resource-description(recursive=true)");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        List<AttributeType> attribList = new ArrayList<AttributeType>();

        if (selectedNode.isGeneric()) {
            for (ModelNode results : rscDesc.get("result").asList()) {
                findAttributes(attribList, selectedNode.addressPath(), results.get("result"));
            }
        } else {
            findAttributes(attribList, selectedNode.addressPath(), rscDesc.get("result"));
        }

        return attribList;
    }

    private void findAttributes(List<AttributeType> attribList, String basePath, ModelNode results) {
        readAttributes(attribList, basePath, results);
        readChildren(attribList, basePath, results);
    }

    private void readAttributes(List<AttributeType> attribList, String basePath, ModelNode results) {
        if (!results.get("attributes").isDefined()) return;
        for (Property attribute : results.get("attributes").asPropertyList()) {
            attribList.add(new AttributeType(cliGuiCtx, basePath, attribute));
        }
    }

    private void readChildren(List<AttributeType> attribList, String basePath, ModelNode results) {
        if (!results.get("children").isDefined()) return;
        for (Property child : results.get("children").asPropertyList()) {
            if (!child.getValue().get("model-description").isDefined()) continue;

            // example is /extension=*
            if (child.getValue().get("model-description", "*").isDefined()) {
                findAttributes(attribList, basePath + child.getName() + "=*/", child.getValue().get("model-description", "*"));
                continue;
            }

            // example is /core-service=platform-mbean
            for (Property attrib : child.getValue().get("model-description").asPropertyList()) {
                findAttributes(attribList, basePath + child.getName() + "=" + attrib.getName() + "/", attrib.getValue());
            }
        }
    }
}
