package de.embl.cba.bdp2.ui;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class ImageMenu extends JMenu {

    BdvMenus bdvMenus;

    public ImageMenu( BdvMenus bdvMenus ) {
        this.bdvMenus = bdvMenus;
        setText(UIDisplayConstants.IMAGE_MENU_DISPLAY_TEXT );

        addMenuItem( UIDisplayConstants.CALIBRATE_MENU_ITEM );
        addMenuItem( UIDisplayConstants.SAVE_AS_MENU_ITEM );
    }

    private JMenuItem addMenuItem( String name )
    {
        JMenuItem jMenuItem = new JMenuItem( name );
        jMenuItem.addActionListener( bdvMenus );
        this.add( jMenuItem );
        return jMenuItem;
    }
}
