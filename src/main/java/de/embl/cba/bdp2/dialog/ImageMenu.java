package de.embl.cba.bdp2.dialog;

import de.embl.cba.bdp2.ui.Menu;
import de.embl.cba.bdp2.ui.MenuActions;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class ImageMenu extends JMenu {

    MenuActions menuActions;

    public ImageMenu( MenuActions menuActions ) {
        this.menuActions = menuActions;
        setText( Menu.DEVELOPMENT_MENU_DISPLAY_TEXT );

        addMenuItem( Menu.CALIBRATE_MENU_ITEM );
        addMenuItem( Menu.SAVE_AS_IMARIS_VOLUMES_MENU_ITEM );
    }

    private JMenuItem addMenuItem( String name )
    {
        JMenuItem jMenuItem = new JMenuItem( name );
        jMenuItem.addActionListener( menuActions );
        this.add( jMenuItem );
        return jMenuItem;
    }
}
