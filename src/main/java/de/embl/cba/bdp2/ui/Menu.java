package de.embl.cba.bdp2.ui;

import de.embl.cba.bdp2.bin.BinCommand;
import de.embl.cba.bdp2.calibrate.CalibrateCommand;
import de.embl.cba.bdp2.convert.ConvertToUnsignedByteTypeCommand;
import de.embl.cba.bdp2.crop.CropCommand;
import de.embl.cba.bdp2.data.OpenSampleDataCommand;
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
    public static final String APPLY_TRACK_MENU_ITEM = "Apply Track...";
    public static final String REGISTER_VOLUME_SIFT_MENU_ITEM = "Correct Lateral Slice Drift in Volume (SIFT)...";
    public static final String REGISTER_MOVIE_SIFT_MENU_ITEM = "Correct Lateral Frame Drift in Time-lapse (SIFT)...";
    public static final String REGISTER_MOVIE_PHASE_CORRELATION_MENU_ITEM = "Correct Lateral Frame Drift in Time-lapse (X-Corr)...";
    // Menu items
    public static final String SAVE_AS_IMARIS_VOLUMES_MENU_ITEM = "Save as Imaris Volumes...";
    public static final String SAVE_AS_TIFF_VOLUMES_MENU_ITEM = "Save as Tiff Volumes...";
    public static final String SAVE_AS_TIFF_PLANES_MENU_ITEM = "Save as Tiff Planes...";

    public static final String CROP_CALIBRATED = "Crop (calibrated units)...";
    public static final String CROP_VOXEL = "Crop (voxel units)...";

    public static final String OBLIQUE_MENU_ITEM = "Shear...";

    // Menus
    public static final String MISC_MENU = "Misc";
    public static final String DEVELOPMENT_MENU_DISPLAY_TEXT = "Development";

    private final MenuActions menuActions;

    public Menu( MenuActions menuActions )
    {
        this.menuActions = menuActions;
        setText( "BigDataProcessor2" );

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
        addMenuItem( processMenu, CROP_VOXEL );
        addMenuItem( processMenu, CROP_CALIBRATED );

        addMenuItem( processMenu, CropCommand.COMMAND_NAME );
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
        final JMenu correctMotionMenu = new JMenu( name );
        this.add( correctMotionMenu );
        return correctMotionMenu;
    }

    private JMenuItem addMenuItem( String name )
    {
        JMenuItem jMenuItem = new JMenuItem( name );
        jMenuItem.addActionListener( menuActions );
        this.add( jMenuItem );
        return jMenuItem;
    }

    private JMenuItem addMenuItem( JMenu jMenu, String name )
    {
        JMenuItem jMenuItem = new JMenuItem( name );
        jMenuItem.addActionListener( menuActions );
        jMenu.add( jMenuItem );
        return jMenuItem;
    }


}
