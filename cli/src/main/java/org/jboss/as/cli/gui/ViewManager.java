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
package org.jboss.as.cli.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import org.jboss.as.cli.gui.component.DisplayTextDialogMenuItem;
import org.jboss.as.cli.gui.component.MsgDialog;
import org.jboss.as.cli.gui.component.RefreshableViewPanel;
import org.jboss.as.cli.gui.component.ViewPanel;
import org.jboss.dmr.ModelNode;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2012 Red Hat Inc.
 */
public class ViewManager {
    private static final int VIEW_MENU_POSITION = 1;

    private CliGuiContext cliGuiCtx;

    private List<JPanel> views = new ArrayList<JPanel>();

    public ViewManager(CliGuiContext cliGuiCtx) {
        this.cliGuiCtx = cliGuiCtx;
    }

    public synchronized void addView(String name, JPanel view) {
        JTabbedPane tabs = cliGuiCtx.getTabs();
        int tabCount = tabs.getComponentCount();

        tabs.add(name, view);
        tabs.setSelectedIndex(tabCount);
        this.views.add(view);

        addToViewMenu(view, name, tabCount);
    }

    private void addToViewMenu(JPanel view, String name, int tabIndex) {
        JMenu viewMenu = cliGuiCtx.getMenuBar().getMenu(VIEW_MENU_POSITION);

        if (views.size() == 1) viewMenu.addSeparator();

        JMenu subMenu = new JMenu(name);

        if (view instanceof ViewPanel) {
            JMenuItem shareItem = new JMenuItem("Share view with other clients");
            shareItem.addActionListener(new ShareViewActionListener((ViewPanel)view, name));
            subMenu.add(shareItem);
        }

        JMenuItem editItem = new JMenuItem("Edit");
        editItem.addActionListener(new ComingSoonActionListener());
        subMenu.add(editItem);

        if (view instanceof RefreshableViewPanel) {
            JMenuItem refreshItem = new JMenuItem("Refresh");
            refreshItem.addActionListener(new RefreshActionListener((RefreshableViewPanel)view));
            subMenu.add(refreshItem);
        }

        if (view instanceof ViewPanel) {
            JMenuItem showDefItem = new DisplayTextDialogMenuItem("View Definition", "Show definition", ((ViewPanel)view).getDefinition().toString());
            subMenu.add(showDefItem);
        }

        JMenuItem closeItem = new JMenuItem("Close");
        closeItem.addActionListener(new CloseViewActionListener(view, viewMenu, subMenu));
        subMenu.add(closeItem);

        viewMenu.add(subMenu);
    }

    private class ComingSoonActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            comingSoon(cliGuiCtx);
        }
    }

    public static void comingSoon(CliGuiContext cliGuiCtx) {
        String msg = "Feature coming soon.";
        JOptionPane.showMessageDialog(cliGuiCtx.getMainWindow(), msg, msg, JOptionPane.INFORMATION_MESSAGE);
    }

    private class ShareViewActionListener implements ActionListener {

        private ViewPanel view;
        private String name;

        public ShareViewActionListener(ViewPanel view, String name) {
            this.view = view;
            this.name = name;
        }

        public void actionPerformed(ActionEvent e) {
            //TODO: decide how to handle domains.  Should we just upload view to all instances of
            //      the views subsystem?
            try {
                ModelNode response = cliGuiCtx.getExecutor().doCommand("/subsystem=views/view=" + name + "/:add(definition=" + view.getDefinition().toString() + ")");
                if (response.get("outcome").asString().equals("success")) {
                    MsgDialog.showInfoMessage(cliGuiCtx, "View Upload Success", "Successfully uploaded to view subsystem.");
                } else {
                    MsgDialog.showDMRFailure(cliGuiCtx, response, "View Upload Failed");
                }
            } catch (Exception ex) {
                MsgDialog.showError(cliGuiCtx, "CLI command failed", ex.getLocalizedMessage());
                ex.printStackTrace();
            }
        }

    }

    private class RefreshActionListener implements ActionListener {
        private RefreshableViewPanel view;

        public RefreshActionListener(RefreshableViewPanel view) {
            this.view = view;
        }

        public void actionPerformed(ActionEvent e) {
            view.refresh();
        }
    }

    private class CloseViewActionListener implements ActionListener {

        private JPanel view;
        private JMenu viewMenu;
        private JMenu subMenu;

        public CloseViewActionListener(JPanel view, JMenu viewMenu, JMenu subMenu) {
            this.view = view;
            this.viewMenu = viewMenu;
            this.subMenu = subMenu;
        }

        public void actionPerformed(ActionEvent e) {
            cliGuiCtx.getTabs().remove(view);
            viewMenu.remove(subMenu);
        }
    }
}
