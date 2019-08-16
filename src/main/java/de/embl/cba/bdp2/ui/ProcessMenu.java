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

        final JMenu correctMotionMenu = addMenu( UIDisplayConstants.CORRECT_MOTION_MENU_ITEM );
        addMenuItem( correctMotionMenu, UIDisplayConstants.APPLY_TRACK_MENU_ITEM );
        addMenuItem( correctMotionMenu, UIDisplayConstants.REGISTER_VOLUME_SIFT_MENU_ITEM );
        addMenuItem( correctMotionMenu, UIDisplayConstants.REGISTER_MOVIE_SIFT_MENU_ITEM );
        addMenuItem( correctMotionMenu, UIDisplayConstants.REGISTER_MOVIE_PHASE_CORRELATION_MENU_ITEM );
    }

    private JMenu addMenu( String name )
    {
        final JMenu correctMotionMenu = new JMenu( name );
        this.add( correctMotionMenu );
        return correctMotionMenu;
    }

    private JMenuItem addMenuItem( String name )
    {
        JMenuItem jMenuItem = new JMenuItem( name );
        jMenuItem.addActionListener( bdvMenus );
        this.add( jMenuItem );
        return jMenuItem;
    }

    private JMenuItem addMenuItem( JMenu jMenu, String name )
    {
        JMenuItem jMenuItem = new JMenuItem( name );
        jMenuItem.addActionListener( bdvMenus );
        jMenu.add( jMenuItem );
        return jMenuItem;
    }


}
