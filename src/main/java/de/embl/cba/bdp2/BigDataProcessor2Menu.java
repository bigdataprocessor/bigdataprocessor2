package de.embl.cba.bdp2;

import de.embl.cba.bdp2.process.AbstractImageProcessingCommand;
import de.embl.cba.bdp2.open.ui.DownloadAndOpenSampleDataCommand;
import de.embl.cba.bdp2.track.ApplyTrackCommand;
import de.embl.cba.bdp2.open.ui.*;
import de.embl.cba.bdp2.scijava.Services;
import de.embl.cba.bdp2.service.ImageViewerService;
import de.embl.cba.bdp2.utils.PluginProvider;
import de.embl.cba.bdp2.viewers.ImageViewer;
import ij.IJ;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BigDataProcessor2Menu extends JMenu
{
    public static final String CONFIGURE_LOGGING_MENU_ITEM = "Configure Logging...";
    // Development (alpha stage)
    public static final String IMAGEJ_VIEW_MENU_ITEM = "Show in Hyperstack Viewer";
    public static final String REGISTER_VOLUME_SIFT_MENU_ITEM = "Correct Lateral Slice Drift in Volume (SIFT)...";
    public static final String REGISTER_MOVIE_SIFT_MENU_ITEM = "Correct Lateral Frame Drift in Time-lapse (SIFT)...";
    public static final String REGISTER_MOVIE_PHASE_CORRELATION_MENU_ITEM = "Correct Lateral Frame Drift in Time-lapse (X-Corr)...";
    // Menu items
    public static final String SAVE_AS_IMARIS_VOLUMES_MENU_ITEM = "Save as Imaris Volumes...";
    public static final String SAVE_AS_TIFF_VOLUMES_MENU_ITEM = "Save as Tiff Volumes...";
    public static final String SAVE_AS_TIFF_PLANES_MENU_ITEM = "Save as Tiff Planes...";

    public static final String CROP = "Crop...";

    public static final String CREATE_TRACK = "Create Track...";

    public static final String OBLIQUE_MENU_ITEM = "Shear...";
    
    // Menus
    public static final String MISC = "Misc";
    public static final String DEVELOPMENT_MENU_DISPLAY_TEXT = "Development";
    public static final String MACRO_RECORDING = "Record Macro...";
    public static final String ABOUT = "About";
    public static final String HELP = "Help";
    public static final String ISSUE = "Report an issue";
    public static final String CITE = "Cite";
    public static final String DEBUG_MENU_ITEM = "Debug";

    private final BigDataProcessor2MenuActions menuActions;
    private final ArrayList< JMenu > menus;

    public BigDataProcessor2Menu( BigDataProcessor2MenuActions menuActions )
    {
        this.menuActions = menuActions;
        setText( "BigDataProcessor2" );

        menus = new ArrayList<>();

        final JMenu mainMenu = addMenu( "BDP2" );
        menus.add( mainMenu );
        addMenuItem( mainMenu, ABOUT );
        addMenuItem( mainMenu, HELP );
        addMenuItem( mainMenu, ISSUE );
        addMenuItem( mainMenu, CITE );

        final JMenu recordMenu = addMenu( "Record" );
        menus.add( recordMenu );
        addMenuItem( recordMenu, MACRO_RECORDING );

        final JMenu openMenu = addMenu( "Open" );
        menus.add( openMenu );
        addMenuItem( openMenu, OpenCustomCommand.COMMAND_NAME );
        addMenuItem( openMenu, OpenCustomHelpCommand.COMMAND_NAME );
        addMenuItem( openMenu, OpenEMTiffPlanesCommand.COMMAND_NAME );
        addMenuItem( openMenu, DownloadAndOpenSampleDataCommand.COMMAND_NAME );
        addMenuItem( openMenu, OpenLuxendoCommand.COMMAND_NAME );
        addMenuItem( openMenu, OpenLeicaDSLTiffPlanesCommand.COMMAND_NAME );

        final JMenu processMenu = addMenu( "Process" );
        menus.add( processMenu );

        populateProcessMenu( processMenu );

        final JMenu correctDriftMenu = new JMenu( "Correct Drift" );
        processMenu.add( correctDriftMenu );
        addMenuItem( correctDriftMenu, CREATE_TRACK );
        addMenuItem( correctDriftMenu, ApplyTrackCommand.COMMAND_NAME );

//        addMenuItem( OBLIQUE_MENU_ITEM );

        final JMenu saveMenu = addMenu( "Save" );
        menus.add( saveMenu );
        addMenuItem( saveMenu, SAVE_AS_IMARIS_VOLUMES_MENU_ITEM );
        addMenuItem( saveMenu, SAVE_AS_TIFF_VOLUMES_MENU_ITEM );
        addMenuItem( saveMenu, SAVE_AS_TIFF_PLANES_MENU_ITEM );

        final JMenu miscMenu = addMenu( MISC );
        menus.add( miscMenu );
        addMenuItem( miscMenu, IMAGEJ_VIEW_MENU_ITEM );
        addMenuItem( miscMenu, DEBUG_MENU_ITEM );
    }

    public void populateProcessMenu( JMenu processMenu )
    {
        PluginProvider< AbstractImageProcessingCommand > pluginProvider = new PluginProvider<>( AbstractImageProcessingCommand.class );
        pluginProvider.setContext( Services.getContext() );
        List< String > names = new ArrayList<>( pluginProvider.getNames() );
        Collections.sort( names );

        for ( String name : names )
        {
            addMenuItemAndProcessingAction( processMenu, name, pluginProvider.getInstance( name ) );
        }

        // TODO: auto-populate as much as possible
//        addMenuItem( processMenu, ImageRenameCommand.COMMAND_NAME );
//        addMenuItem( processMenu, CalibrateCommand.COMMAND_NAME );
//        addMenuItem( processMenu, CropCommand.COMMAND_NAME );
//        addMenuItem( processMenu, TransformCommand.COMMAND_NAME );
//        addMenuItem( processMenu, BinCommand.COMMAND_NAME );
//        addMenuItem( processMenu, MultiChannelConvertToUnsignedByteTypeCommand.COMMAND_NAME );
//        addMenuItem( processMenu, AlignChannelsCommand.COMMAND_NAME );
//        addMenuItem( processMenu, SplitChipCommand.COMMAND_NAME );
    }

    public ArrayList< JMenu > getMenus()
    {
        return menus;
    }

    private JMenu addMenu( String name )
    {
        final JMenu menu = new JMenu( name );
        this.add( menu );
        return menu;
    }

    private JMenuItem addMenuItem( String name )
    {
        JMenuItem menuItem = new JMenuItem( name );
        menuItem.addActionListener( menuActions );
        this.add( menuItem );
        return menuItem;
    }

    private JMenuItem addMenuItem( JMenu jMenu, String name )
    {
        JMenuItem jMenuItem = new JMenuItem( name );
        jMenuItem.addActionListener( menuActions );
        jMenu.add( jMenuItem );
        return jMenuItem;
    }

    private JMenuItem addMenuItemAndProcessingAction( JMenu jMenu, String name, AbstractImageProcessingCommand< ? > processingCommand )
    {
        JMenuItem jMenuItem = new JMenuItem( name );
        jMenuItem.addActionListener( e -> {
            new Thread( () ->
            {
                ImageViewer activeViewer = ImageViewerService.getActiveViewer();

                if ( activeViewer == null )
                {
                    IJ.showMessage( "No image selected.\n\nPlease select an image by either\n- clicking on an existing BigDataViewer window, or\n- open a new image using the [ BigDataProcessor2 > Open ] menu." );
                    return;
                }

                processingCommand.showDialog( activeViewer );
            }).start();
        } );
        jMenu.add( jMenuItem );
        return jMenuItem;
    }
}
