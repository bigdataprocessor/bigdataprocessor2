package de.embl.cba.bdp2.dialog;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class MiscMenu extends JMenu {


    public MiscMenu( BdvMenus actionListener ) {
        setText( UIDisplayConstants.MISC_MENU );

        final JMenuItem imageJViewerItem
                = new JMenuItem( UIDisplayConstants.IMAGEJ_VIEW_MENU_ITEM );
        add( imageJViewerItem );
        imageJViewerItem.addActionListener(actionListener);

        final JMenuItem configureLogging =
                new JMenuItem( UIDisplayConstants.CONFIGURE_LOGGING_MENU_ITEM );
        configureLogging.addActionListener( actionListener );
        add( configureLogging );

    }
}
