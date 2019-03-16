package de.embl.cba.bdp2.ui;

import javax.swing.*;

public class ProcessMenu extends JMenu {

    private final BdvMenus bdvMenus;

    public ProcessMenu( BdvMenus bdvMenus ) {

        setText(UIDisplayConstants.PROCESS_MENU_DISPLAY_TEXT);
        this.bdvMenus = bdvMenus;

        addMenuItem( UIDisplayConstants.INTERACTIVE_BINNING );
        addMenuItem( UIDisplayConstants.OBLIQUE_MENU_DISPLAY_TEXT );
        addMenuItem( UIDisplayConstants.CROP_MENU_DISPLAY_TEXT );
        addMenuItem( UIDisplayConstants.INTERACTIVE_EIGHT_BIT_MENU_DISPLAY_TEXT );
        addMenuItem( UIDisplayConstants.CHROMATIC_SHIFT_CORRECTION_MENU_DISPLAY_TEXT );
    }

    private void addMenuItem( String name )
    {
        JMenuItem jMenuItem = new JMenuItem( name );
        add( jMenuItem );
        jMenuItem.addActionListener( bdvMenus );
    }


}
