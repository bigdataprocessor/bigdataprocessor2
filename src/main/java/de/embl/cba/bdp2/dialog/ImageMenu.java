package de.embl.cba.bdp2.dialog;

import de.embl.cba.bdp2.BdpMenu;
import de.embl.cba.bdp2.BdpMenuActions;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class ImageMenu extends JMenu {

    BdpMenuActions bdpMenuActions;

    public ImageMenu( BdpMenuActions bdpMenuActions ) {
        this.bdpMenuActions = bdpMenuActions;
        setText( BdpMenu.DEVELOPMENT_MENU_DISPLAY_TEXT );

        addMenuItem( BdpMenu.CALIBRATE_MENU_ITEM );
        addMenuItem( BdpMenu.SAVE_AS_IMARIS_VOLUMES_MENU_ITEM );
    }

    private JMenuItem addMenuItem( String name )
    {
        JMenuItem jMenuItem = new JMenuItem( name );
        jMenuItem.addActionListener( bdpMenuActions );
        this.add( jMenuItem );
        return jMenuItem;
    }
}
