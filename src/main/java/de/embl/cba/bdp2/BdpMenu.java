package de.embl.cba.bdp2;

import de.embl.cba.bdp2.image.ImageRenameCommand;

import javax.swing.*;

public class BdpMenu extends JMenu {

    public static final String EIGHT_BIT_CONVERSION_MENU_ITEM = "8-bit...";
    public static final String BINNING_MENU_ITEM = "Bin...";
    public static final String CHROMATIC_SHIFT_CORRECTION_MENU_ITEM = "Correct Chromatic Shift... ";
    public static final String SPLIT_VIEW_MENU_ITEM = "Merge Split View...";
    public static final String CONFIGURE_LOGGING_MENU_ITEM = "Configure Logging...";
    public static final String VIEW_IN_3D_VIEWER = "3D Viewer..."; // TODO
    // Development (alpha stage)
    public static final String IMAGEJ_VIEW_MENU_ITEM = "Show in ImageJ Viewer";
    public static final String CORRECT_MOTION_MENU_ITEM = "Correct Motion... (Under Development)";
    public static final String APPLY_TRACK_MENU_ITEM = "Apply Track...";
    public static final String REGISTER_VOLUME_SIFT_MENU_ITEM = "Correct Lateral Slice Drift in Volume (SIFT)...";
    public static final String REGISTER_MOVIE_SIFT_MENU_ITEM = "Correct Lateral Frame Drift in Time-lapse (SIFT)...";
    public static final String REGISTER_MOVIE_PHASE_CORRELATION_MENU_ITEM = "Correct Lateral Frame Drift in Time-lapse (X-Corr)...";
    // Menu items
    public static final String SAVE_AS_IMARIS_VOLUMES_MENU_ITEM = "Save as Imaris Volumes...";
    public static final String SAVE_AS_TIFF_VOLUMES_MENU_ITEM = "Save as Tiff Volumes...";
    public static final String SAVE_AS_TIFF_PLANES_MENU_ITEM = "Save as Tiff Planes...";
    public static final String CALIBRATE_MENU_ITEM = "Calibrate...";

    public static final String OBLIQUE_MENU_ITEM = "Shear..."; // TODO: ?
    public static final String CROP_MENU_ITEM = "Crop...";
    // Menus
    public static final String MISC_MENU = "Misc";
    public static final String DEVELOPMENT_MENU_DISPLAY_TEXT = "Development";

    private final BdpMenuActions bdpMenuActions;

    public BdpMenu( BdpMenuActions bdpMenuActions )
    {
        this.bdpMenuActions = bdpMenuActions;
        setText( "BigDataProcessor" );

        final JMenu imageMenu = addMenu( "Image" );
        addMenuItem( imageMenu, ImageRenameCommand.BDP_MENU_NAME );
        addMenuItem( imageMenu, CALIBRATE_MENU_ITEM );
        addMenuItem( imageMenu, CROP_MENU_ITEM );

        final JMenu processMenu = addMenu( "Process" );
        addMenuItem( processMenu, BINNING_MENU_ITEM );
        addMenuItem( processMenu, EIGHT_BIT_CONVERSION_MENU_ITEM );
        addMenuItem( processMenu, CHROMATIC_SHIFT_CORRECTION_MENU_ITEM );
        addMenuItem( processMenu, SPLIT_VIEW_MENU_ITEM );
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
        jMenuItem.addActionListener( bdpMenuActions );
        this.add( jMenuItem );
        return jMenuItem;
    }

    private JMenuItem addMenuItem( JMenu jMenu, String name )
    {
        JMenuItem jMenuItem = new JMenuItem( name );
        jMenuItem.addActionListener( bdpMenuActions );
        jMenu.add( jMenuItem );
        return jMenuItem;
    }


}
