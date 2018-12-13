package de.embl.cba.bigDataTools2.dataStreamingGUI;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class OthersMenu extends JMenu {

    public JMenuItem imageJViewerItem;
    private JMenuItem bigDataTrackerItem;
    private JMenuItem obliqueView;

    public OthersMenu(BdvMenus actionListener) {
        setText("Others");
        imageJViewerItem = new JMenuItem("Show in ImageJ Viewer");
        add(imageJViewerItem);
        bigDataTrackerItem = new JMenuItem("Big Data Tracker");
        add(bigDataTrackerItem);
        obliqueView = new JMenuItem("Oblique View");
        add(obliqueView);
        bigDataTrackerItem.addActionListener(actionListener);
        imageJViewerItem.addActionListener(actionListener);
        obliqueView.addActionListener(actionListener);
    }
}
