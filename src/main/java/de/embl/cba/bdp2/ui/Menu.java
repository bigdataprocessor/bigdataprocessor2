package de.embl.cba.bdp2.ui;

import de.embl.cba.bdp2.bin.BinCommand;
import de.embl.cba.bdp2.calibrate.CalibrateCommand;
import de.embl.cba.bdp2.convert.ConvertToUnsignedByteTypeCommand;
import de.embl.cba.bdp2.data.OpenSampleDataCommand;
import de.embl.cba.bdp2.drift.track.ApplyTrackCommand;
import de.embl.cba.bdp2.image.ImageRenameCommand;
import de.embl.cba.bdp2.open.*;
import de.embl.cba.bdp2.align.AlignChannelsCommand;
import de.embl.cba.bdp2.align.splitchip.AlignChannelsSplitChipCommand;

import javax.swing.*;

public class Menu extends JMenu
{
    public static final String CONFIGURE_LOGGING_MENU_ITEM = "Configure Logging...";
    // Development (alpha stage)
    public static final String IMAGEJ_VIEW_MENU_ITEM = "Show in ImageJ Viewer";
    public static final String REGISTER_VOLUME_SIFT_MENU_ITEM = "Correct Lateral Slice Drift in Volume (SIFT)...";
    public static final String REGISTER_MOVIE_SIFT_MENU_ITEM = "Correct Lateral Frame Drift in Time-lapse (SIFT)...";
    public static final String REGISTER_MOVIE_PHASE_CORRELATION_MENU_ITEM = "Correct Lateral Frame Drift in Time-lapse (X-Corr)...";
    // Menu items
    public static final String SAVE_AS_IMARIS_VOLUMES_MENU_ITEM = "Save as Imaris Volumes...";
    public static final String SAVE_AS_TIFF_VOLUMES_MENU_ITEM = "Save as Tiff Volumes...";
    public static final String SAVE_AS_TIFF_PLANES_MENU_ITEM = "Save as Tiff Planes...";

    public static final String CROP_CALIBRATED = "Crop in Calibrated Units...";
    public static final String CROP_VOXEL = "Crop in Voxel Units...";

    public static final String CREATE_MANUAL_TRACK = "Create Manual Track...";

    public static final String OBLIQUE_MENU_ITEM = "Shear...";

    // Menus
    public static final String MISC_MENU = "Misc";
    public static final String DEVELOPMENT_MENU_DISPLAY_TEXT = "Development";
    public static final String MACRO_RECORDING = "Macro Recording...";

    private final MenuActions menuActions;

    public Menu( MenuActions menuActions )
    {
        this.menuActions = menuActions;
        setText( "BigDataProcessor2" );

        final JMenu recordMenu = addMenu( "Record" );
        addMenuItem( recordMenu, MACRO_RECORDING );

        final JMenu openMenu = addMenu( "Open" );
        addMenuItem( openMenu, RegExpHelpCommand.COMMAND_NAME );
        addMenuItem( openMenu, OpenSampleDataCommand.COMMAND_NAME );

        addMenuItem( openMenu, OpenCommand.COMMAND_NAME );
        addMenuItem( openMenu, OpenEMTiffPlanesCommand.COMMAND_NAME );
        addMenuItem( openMenu, OpenLuxendoMuViCommand.COMMAND_NAME );
        addMenuItem( openMenu, OpenLuxendoInViCommand.COMMAND_NAME );
        addMenuItem( openMenu, OpenLeicaDSLTiffPlanesCommand.COMMAND_NAME );

        final JMenu processMenu = addMenu( "Process" );
        addMenuItem( processMenu, ImageRenameCommand.COMMAND_NAME );
        addMenuItem( processMenu, CalibrateCommand.COMMAND_NAME );

        final JMenu correctDriftMenu = new JMenu( "Correct Drift" );
        addMenuItem( correctDriftMenu, CREATE_MANUAL_TRACK );
        addMenuItem( correctDriftMenu, ApplyTrackCommand.COMMAND_NAME );
        processMenu.add( correctDriftMenu );

        final JMenu cropMenu = new JMenu( "Crop" );
        addMenuItem( cropMenu, CROP_VOXEL );
        addMenuItem( cropMenu, CROP_CALIBRATED );
        processMenu.add( cropMenu );

        addMenuItem( processMenu, BinCommand.COMMAND_NAME );
        addMenuItem( processMenu, ConvertToUnsignedByteTypeCommand.COMMAND_NAME );
        addMenuItem( processMenu, AlignChannelsCommand.COMMAND_NAME );
        addMenuItem( processMenu, AlignChannelsSplitChipCommand.COMMAND_NAME );
//        addMenuItem( OBLIQUE_MENU_ITEM );

        final JMenu saveMenu = addMenu( "Save" );
        addMenuItem( saveMenu, SAVE_AS_IMARIS_VOLUMES_MENU_ITEM );
        addMenuItem( saveMenu, SAVE_AS_TIFF_VOLUMES_MENU_ITEM );
        addMenuItem( saveMenu, SAVE_AS_TIFF_PLANES_MENU_ITEM );


//        final JMenu correctMotionMenu = addMenu( UIDisplayConstants.CORRECT_MOTION_MENU_ITEM );
//        addMenuItem( correctMotionMenu, UIDisplayConstants.APPLY_TRACK_MENU_ITEM );
//        addMenuItem( correctMotionMenu, UIDisplayConstants.REGISTER_VOLUME_SIFT_MENU_ITEM );
//        addMenuItem( correctMotionMenu, UIDisplayConstants.REGISTER_MOVIE_SIFT_MENU_ITEM );
//        addMenuItem( correctMotionMenu, UIDisplayConstants.REGISTER_MOVIE_PHASE_CORRELATION_MENU_ITEM );
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
}
