package de.embl.cba.bdp2.ui;

import javax.swing.*;

public class ProcessMenu extends JMenu {

    private final JMenuItem obliqueView;
    private final JMenuItem cropSelectMenu;
    private final JMenuItem interactiveUnsignedByteConverter;

    public ProcessMenu(BdvMenus actionListener) {

        setText(UIDisplayConstants.PROCESS_MENU_DISPLAY_TEXT);

        cropSelectMenu = new JMenuItem(UIDisplayConstants.CROP_MENU_DISPLAY_TEXT);
        add(cropSelectMenu);
        cropSelectMenu.addActionListener(actionListener);

        obliqueView = new JMenuItem(UIDisplayConstants.OBLIQUE_MENU_DISPLAY_TEXT);
        add(obliqueView);
        obliqueView.addActionListener(actionListener);

        interactiveUnsignedByteConverter =
                new JMenuItem(UIDisplayConstants.INTERACTIVE_EIGHT_BIT_MENU_DISPLAY_TEXT);
        add(interactiveUnsignedByteConverter);
        interactiveUnsignedByteConverter.addActionListener( actionListener );
    }


}
