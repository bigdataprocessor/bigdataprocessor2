package de.embl.cba.bdp2.dialog;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class ImageMenu extends JMenu {

    BdpMenus bdpMenus;

    public ImageMenu( BdpMenus bdpMenus ) {
        this.bdpMenus = bdpMenus;
        setText(UIDisplayConstants.DEVELOPMENT_MENU_DISPLAY_TEXT );

        addMenuItem( UIDisplayConstants.CALIBRATE_MENU_ITEM );
        addMenuItem( UIDisplayConstants.SAVE_AS_MENU_ITEM );
    }

    private JMenuItem addMenuItem( String name )
    {
        JMenuItem jMenuItem = new JMenuItem( name );
        jMenuItem.addActionListener( bdpMenus );
        this.add( jMenuItem );
        return jMenuItem;
    }
}
