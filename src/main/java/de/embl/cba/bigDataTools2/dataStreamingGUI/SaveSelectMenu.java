package de.embl.cba.bigDataTools2.dataStreamingGUI;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class SaveSelectMenu extends JMenu {
    private final JMenuItem item;

    public SaveSelectMenu(BdvMenus actionListener) {
        setText("Save");
        item = new JMenuItem("Save As");
        item.addActionListener(actionListener);
        add(item);
    }
}
