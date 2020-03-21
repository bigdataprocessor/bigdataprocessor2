package de.embl.cba.bdp2.dialog;

import de.embl.cba.bdp2.BdpMenu;
import de.embl.cba.bdp2.BdpMenuActions;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class MiscMenu extends JMenu {


    public MiscMenu( BdpMenuActions actionListener ) {
        setText( BdpMenu.MISC_MENU );

        final JMenuItem imageJViewerItem
                = new JMenuItem( BdpMenu.IMAGEJ_VIEW_MENU_ITEM );
        add( imageJViewerItem );
        imageJViewerItem.addActionListener(actionListener);

        final JMenuItem configureLogging =
                new JMenuItem( BdpMenu.CONFIGURE_LOGGING_MENU_ITEM );
        configureLogging.addActionListener( actionListener );
        add( configureLogging );

    }
}
