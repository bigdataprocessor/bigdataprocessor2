package de.embl.cba.bdp2.ui;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.bin.BinCommand;
import de.embl.cba.bdp2.bin.BinDialog;
import de.embl.cba.bdp2.calibrate.CalibrateCommand;
import de.embl.cba.bdp2.calibrate.CalibrationDialog;
import de.embl.cba.bdp2.convert.ConvertToUnsignedByteTypeCommand;
import de.embl.cba.bdp2.convert.UnsignedByteTypeConversionDialog;
import de.embl.cba.bdp2.crop.CropDialog;
import de.embl.cba.bdp2.data.OpenSampleDataCommand;
import de.embl.cba.bdp2.dialog.MiscMenu;
import de.embl.cba.bdp2.dialog.Utils;
import de.embl.cba.bdp2.drift.track.ApplyTrackCommand;
import de.embl.cba.bdp2.drift.track.ApplyTrackDialog;
import de.embl.cba.bdp2.drift.track.TrackCreator;
import de.embl.cba.bdp2.image.ImageRenameCommand;
import de.embl.cba.bdp2.image.ImageRenameDialog;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.open.ui.*;
import de.embl.cba.bdp2.record.MacroRecordingDialog;
import de.embl.cba.bdp2.save.SaveDialog;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.scijava.Services;
import de.embl.cba.bdp2.align.AlignChannelsCommand;
import de.embl.cba.bdp2.align.splitchip.AlignChannelsSplitChipCommand;
import de.embl.cba.bdp2.align.splitchip.SplitViewMergeDialog;
import de.embl.cba.bdp2.align.ChromaticShiftDialog;
import de.embl.cba.bdp2.register.RegisteredViews;
import de.embl.cba.bdp2.register.Registration;
import de.embl.cba.bdp2.service.BdvService;
import de.embl.cba.bdp2.shear.ShearMenuDialog;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.view.Views;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class MenuActions implements ActionListener {

    //private final ImageMenu imageMenu;
    private final MiscMenu miscMenu;
    private final Menu menu;
    private BdvImageViewer viewer;
    private final ArrayList< JMenu > menus;

    public MenuActions()
    {
        menu = new Menu(this);
        menus = menu.getMenus();
        miscMenu = new MiscMenu(this);
        menus.add( miscMenu );
    }

    public void setViewer( BdvImageViewer viewer ){
        this.viewer = viewer;
    }

    public List< JMenu > getMenus()
    {
        return menus;
    }

    public List< JMenu > getMainMenus()
    {
        List<JMenu> jMenuList = new ArrayList<>();
        jMenuList.add( menu );
        jMenuList.add( miscMenu );
        return jMenuList;
    }

    @Override
    public synchronized void actionPerformed(ActionEvent e)
    {
        final BdvImageViewer activeViewer = BdvService.getActiveViewer();

        this.viewer = activeViewer;

        if (e.getActionCommand().equalsIgnoreCase( Menu.SAVE_AS_IMARIS_VOLUMES_MENU_ITEM ))
        {
            BigDataProcessor2.threadPool.submit(() -> {
                SaveDialog saveDialog = new SaveDialog( viewer, SavingSettings.SaveFileType.IMARIS_VOLUMES );
                saveDialog.setVisible(true);
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase( Menu.SAVE_AS_TIFF_VOLUMES_MENU_ITEM ))
        {
            BigDataProcessor2.threadPool.submit(() -> {
                SaveDialog saveDialog = new SaveDialog( viewer, SavingSettings.SaveFileType.TIFF_VOLUMES );
                saveDialog.setVisible(true);
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase( Menu.SAVE_AS_TIFF_PLANES_MENU_ITEM ))
        {
            BigDataProcessor2.threadPool.submit(() -> {
                SaveDialog saveDialog = new SaveDialog( viewer, SavingSettings.SaveFileType.TIFF_PLANES );
                saveDialog.setVisible(true);
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase( CalibrateCommand.COMMAND_NAME ))
        {
            BigDataProcessor2.threadPool.submit(() -> {
                new CalibrationDialog< >( viewer );
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase( Menu.MACRO_RECORDING ))
        {
            BigDataProcessor2.threadPool.submit(() -> {
                new MacroRecordingDialog();
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase( Menu.OBLIQUE_MENU_ITEM ))
        {
            BigDataProcessor2.threadPool.submit(() -> {
                ShearMenuDialog shearMenuDialog = new ShearMenuDialog( viewer );
                shearMenuDialog.setVisible( true );
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase( Menu.CREATE_TRACK ))
        {
            BigDataProcessor2.threadPool.submit(() -> {
                new TrackCreator( viewer, "track" );
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase( ApplyTrackCommand.COMMAND_NAME ))
        {
            BigDataProcessor2.threadPool.submit(() -> {
                new ApplyTrackDialog( viewer );
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase( Menu.REGISTER_VOLUME_SIFT_MENU_ITEM ))
        {
            BigDataProcessor2.threadPool.submit(() ->
			{
                Integer channel = Utils.getChannel( viewer );
                if ( channel == null ) return;
                RegisteredViews.showSIFTVolumeAlignedBdvView( viewer );
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase( Menu.REGISTER_MOVIE_SIFT_MENU_ITEM ))
        {
            BigDataProcessor2.threadPool.submit(() -> {
                Integer channel = Utils.getChannel( viewer );
                if ( channel == null ) return;
                RegisteredViews.createAlignedMovieView( viewer, Registration.SIFT_CORRESPONDENCES, channel );
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase( Menu.REGISTER_MOVIE_PHASE_CORRELATION_MENU_ITEM ))
        {
            BigDataProcessor2.threadPool.submit(() -> {
                Integer channel = Utils.getChannel( viewer );
                if ( channel == null ) return;
                RegisteredViews.createAlignedMovieView( viewer, Registration.PHASE_CORRELATION, 0 );
            });
        }
        else if(e.getActionCommand().equalsIgnoreCase( Menu.CROP ))
        {
        	new Thread( () ->  {
        	    new CropDialog<>( viewer );
            }).start();
        }
        else if(e.getActionCommand().equalsIgnoreCase( Menu.IMAGEJ_VIEW_MENU_ITEM ))
        {
            new Thread( () -> {
                // TODO:
                // - make own class
                // - add calibration
                RandomAccessibleInterval permuted =
                        Views.permute( viewer.getImage().getRai(),
                                DimensionOrder.Z, DimensionOrder.C );
                ImageJFunctions.show( permuted, BigDataProcessor2.threadPool );
            }).start();
        }
        else if(e.getActionCommand().equalsIgnoreCase( ConvertToUnsignedByteTypeCommand.COMMAND_NAME ))
        {
            BigDataProcessor2.threadPool.submit(() ->
			{
                new UnsignedByteTypeConversionDialog( viewer );
            });
        }
        else if(e.getActionCommand().equalsIgnoreCase( BinCommand.COMMAND_NAME ))
        {
            BigDataProcessor2.threadPool.submit(() ->
            {
                new BinDialog<>( viewer );
            });
        }
        else if( e.getActionCommand().equalsIgnoreCase( AlignChannelsCommand.COMMAND_NAME ) )
        {
            BigDataProcessor2.threadPool.submit(() ->
            {
                new ChromaticShiftDialog<>( viewer );
            });
        }
        else if( e.getActionCommand().equalsIgnoreCase( AlignChannelsSplitChipCommand.COMMAND_NAME ) )
        {
            BigDataProcessor2.threadPool.submit(() ->
            {
                new SplitViewMergeDialog( viewer );
            });
        }
        else if( e.getActionCommand().equalsIgnoreCase( Menu.CONFIGURE_LOGGING_MENU_ITEM ))
        {
            BigDataProcessor2.threadPool.submit(() ->
            {
                Logger.showLoggingLevelDialog();
            });
        }
        else if( e.getActionCommand().equalsIgnoreCase( ImageRenameCommand.COMMAND_NAME ) )
        {
            BigDataProcessor2.threadPool.submit(() ->
            {
                new ImageRenameDialog<>( viewer );
            });
        }
        else if( e.getActionCommand().equalsIgnoreCase( OpenCustomHelpCommand.COMMAND_NAME ) )
        {
            BigDataProcessor2.threadPool.submit(() ->
            {
                Services.commandService.run( OpenCustomHelpCommand.class, true );
            });
        }
        else if( e.getActionCommand().equalsIgnoreCase( OpenSampleDataCommand.COMMAND_NAME ) )
        {
            BigDataProcessor2.threadPool.submit(() ->
            {
                OpenSampleDataCommand.parentBdvImageViewer = viewer;
                Services.commandService.run( OpenSampleDataCommand.class, true );
            });
        }
        else if( e.getActionCommand().equalsIgnoreCase( OpenCustomCommand.COMMAND_NAME ) )
        {
            BigDataProcessor2.threadPool.submit(() ->
            {
                AbstractOpenCommand.parentBdvImageViewer = viewer;
                Services.commandService.run( OpenCustomCommand.class, true );
            });
        }
        else if( e.getActionCommand().equalsIgnoreCase( OpenLeicaDSLTiffPlanesCommand.COMMAND_NAME ) )
        {
            BigDataProcessor2.threadPool.submit(() ->
            {
                AbstractOpenCommand.parentBdvImageViewer = viewer;
                Services.commandService.run( OpenLeicaDSLTiffPlanesCommand.class, true );
            });
        }
        else if( e.getActionCommand().equalsIgnoreCase( OpenLuxendoCommand.COMMAND_NAME ) )
        {
            BigDataProcessor2.threadPool.submit(() ->
            {
                AbstractOpenCommand.parentBdvImageViewer = viewer;
                Services.commandService.run( OpenLuxendoCommand.class, true );
            });
        }

    }
}
