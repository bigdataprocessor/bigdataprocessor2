package de.embl.cba.bdp2.dialog;

import de.embl.cba.bdp2.BigDataProcessor2Menu;
import de.embl.cba.bdp2.BigDataProcessor2MenuActions;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

public class MiscMenu extends JMenu
{
    public MiscMenu( BigDataProcessor2MenuActions actionListener )
    {
        setText( BigDataProcessor2Menu.MISC );

        final JMenuItem imageJViewerItem
                = new JMenuItem( BigDataProcessor2Menu.IMAGEJ_VIEW_MENU_ITEM );
        add( imageJViewerItem );
        imageJViewerItem.addActionListener(actionListener);

        final JMenuItem configureLogging =
                new JMenuItem( BigDataProcessor2Menu.LOG );
        configureLogging.addActionListener( actionListener );
        add( configureLogging );
    }
}
