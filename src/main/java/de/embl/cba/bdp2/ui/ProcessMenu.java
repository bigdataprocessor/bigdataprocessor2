package de.embl.cba.bdp2.ui;

import javax.swing.*;

public class ProcessMenu extends JMenu {

    private final BdvMenus bdvMenus;

    public ProcessMenu( BdvMenus bdvMenus ) {

        setText(UIDisplayConstants.PROCESS_MENU_DISPLAY_TEXT);
        this.bdvMenus = bdvMenus;

        addMenuItem( UIDisplayConstants.BINNING_MENU_ITEM );
        addMenuItem( UIDisplayConstants.OBLIQUE_MENU_ITEM );
        addMenuItem( UIDisplayConstants.CROP_MENU_ITEM );
        addMenuItem( UIDisplayConstants.EIGHT_BIT_CONVERSION_MENU_ITEM );
        addMenuItem( UIDisplayConstants.CHROMATIC_SHIFT_CORRECTION_MENU_ITEM );
        addMenuItem( UIDisplayConstants.SPLIT_VIEW_MENU_ITEM );
        addMenuItem( UIDisplayConstants.CORRECT_MOTION_MENU_ITEM );
    }

    private void addMenuItem( String name )
    {
        JMenuItem jMenuItem = new JMenuItem( name );
        add( jMenuItem );
        jMenuItem.addActionListener( bdvMenus );
    }


}
