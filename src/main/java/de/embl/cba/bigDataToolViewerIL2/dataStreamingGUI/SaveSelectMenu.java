package de.embl.cba.bigDataToolViewerIL2.dataStreamingGUI;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class SaveSelectMenu extends JMenu {
    public JMenuItem item;

    public SaveSelectMenu(BdvMenus actionListener) {
        setText("Save View");
        item = new JMenuItem("Save As");
        item.addActionListener(actionListener);
        add(item);
    }


}
