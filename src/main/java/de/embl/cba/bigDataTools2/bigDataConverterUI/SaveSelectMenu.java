package de.embl.cba.bigDataTools2.bigDataConverterUI;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class SaveSelectMenu extends JMenu {
    private final JMenuItem item;

    public SaveSelectMenu(BdvMenus actionListener) {
        setText(UIDisplayConstants.SAVE_MENU_DISPLAY_TEXT);
        item = new JMenuItem(UIDisplayConstants.SAVE_AS_MENU_DISPLAY_TEXT);
        item.addActionListener(actionListener);
        add(item);
    }
}
