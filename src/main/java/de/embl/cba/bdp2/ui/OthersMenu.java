package de.embl.cba.bdp2.ui;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class OthersMenu extends JMenu {

    private final JMenuItem imageJViewerItem;

    public OthersMenu(BdvMenus actionListener) {
        setText(UIDisplayConstants.MISC_MENU );
        imageJViewerItem = new JMenuItem(UIDisplayConstants.IMAGEJ_VIEW_MENU_ITEM );
        add(imageJViewerItem);
        imageJViewerItem.addActionListener(actionListener);
    }
}
