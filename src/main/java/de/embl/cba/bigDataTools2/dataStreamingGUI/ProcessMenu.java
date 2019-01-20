package de.embl.cba.bigDataTools2.dataStreamingGUI;

import javax.swing.*;

public class ProcessMenu extends JMenu {

    private final JMenuItem obliqueView;
    private final JMenuItem unsignedByteConverter;
    private final JMenuItem cropSelectMenu;

    public ProcessMenu(BdvMenus actionListener) {
        setText(UIDisplayConstants.PROCESS_MENU_DISPLAY_TEXT);
        cropSelectMenu = new JMenuItem(UIDisplayConstants.CROP_MENU_DISPLAY_TEXT);
        add(cropSelectMenu);
        cropSelectMenu.addActionListener(actionListener);
        obliqueView = new JMenuItem(UIDisplayConstants.OBLIQUE_MENU_DISPLAY_TEXT);
        add(obliqueView);
        obliqueView.addActionListener(actionListener);
        unsignedByteConverter = new JMenuItem(UIDisplayConstants.EIGHT_BIT_MENU_DISPLAY_TEXT);
        add(unsignedByteConverter);
        unsignedByteConverter.addActionListener(actionListener);

    }


}
