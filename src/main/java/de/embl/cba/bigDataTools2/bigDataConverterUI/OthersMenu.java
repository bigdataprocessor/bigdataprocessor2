package de.embl.cba.bigDataTools2.bigDataProcessorUI;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class OthersMenu extends JMenu {

    private final JMenuItem imageJViewerItem;
    private final JMenuItem bigDataTrackerItem;


    public OthersMenu(BdvMenus actionListener) {
        setText(UIDisplayConstants.OTHERS_MENU_DISPLAY_TEXT);
        imageJViewerItem = new JMenuItem(UIDisplayConstants.IMAGEJ_VIEW_MENU_DISPLAY_TEXT);
        add(imageJViewerItem);
        bigDataTrackerItem = new JMenuItem(UIDisplayConstants.BIG_DATA_TRACKER_MENU_DISPLAY_TEXT);
        add(bigDataTrackerItem);
        bigDataTrackerItem.addActionListener(actionListener);
        imageJViewerItem.addActionListener(actionListener);
    }
}
