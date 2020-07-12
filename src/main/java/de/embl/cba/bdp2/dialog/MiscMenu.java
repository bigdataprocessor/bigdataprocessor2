package de.embl.cba.bdp2.dialog;

import de.embl.cba.bdp2.ui.Menu;
import de.embl.cba.bdp2.ui.MenuActions;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class MiscMenu extends JMenu
{
    public MiscMenu( MenuActions actionListener )
    {
        setText( Menu.MISC );

        final JMenuItem imageJViewerItem
                = new JMenuItem( Menu.IMAGEJ_VIEW_MENU_ITEM );
        add( imageJViewerItem );
        imageJViewerItem.addActionListener(actionListener);

        final JMenuItem configureLogging =
                new JMenuItem( Menu.CONFIGURE_LOGGING_MENU_ITEM );
        configureLogging.addActionListener( actionListener );
        add( configureLogging );
    }
}
