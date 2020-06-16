package de.embl.cba.bdp2.ui;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.bin.BinCommand;
import de.embl.cba.bdp2.bin.BinDialog;
import de.embl.cba.bdp2.calibrate.CalibrateCommand;
import de.embl.cba.bdp2.calibrate.CalibrationDialog;
import de.embl.cba.bdp2.convert.ConvertToUnsignedByteTypeCommand;
import de.embl.cba.bdp2.convert.UnsignedByteTypeConversionDialog;
import de.embl.cba.bdp2.crop.CropCommand;
import de.embl.cba.bdp2.crop.CropDialog;
import de.embl.cba.bdp2.dialog.MiscMenu;
import de.embl.cba.bdp2.dialog.Utils;
import de.embl.cba.bdp2.image.ImageRenameCommand;
import de.embl.cba.bdp2.image.ImageRenameDialog;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.open.*;
import de.embl.cba.bdp2.save.SaveDialog;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.scijava.Services;
import de.embl.cba.bdp2.align.AlignChannelsCommand;
import de.embl.cba.bdp2.align.splitchip.AlignChannelsSplitChipCommand;
import de.embl.cba.bdp2.align.splitchip.SplitViewMergeDialog;
import de.embl.cba.bdp2.align.ChromaticShiftDialog;
import de.embl.cba.bdp2.register.RegisteredViews;
import de.embl.cba.bdp2.register.Registration;
import de.embl.cba.bdp2.shear.ShearMenuDialog;
import de.embl.cba.bdp2.track.ApplyTrackDialog;
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

    public MenuActions()
    {
        menu = new Menu(this);
        miscMenu = new MiscMenu(this);
    }

    public void setViewer( BdvImageViewer viewer ){
        this.viewer = viewer;
    }

    public List< JMenu > getMenus(){ //Add new menu items here.
        List<JMenu> jMenuList = new ArrayList<>();
        jMenuList.add( menu );
        jMenuList.add( miscMenu );
        return jMenuList;
    }

    @Override
    public synchronized void actionPerformed(ActionEvent e)
    {
        if (e.getActionCommand().equalsIgnoreCase( Menu.SAVE_AS_IMARIS_VOLUMES_MENU_ITEM ))
        {
            BigDataProcessor2.generalThreadPool.submit(() -> {
                SaveDialog saveDialog = new SaveDialog( viewer, SavingSettings.FileType.IMARIS_VOLUMES );
                saveDialog.setVisible(true);
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase( Menu.SAVE_AS_TIFF_VOLUMES_MENU_ITEM ))
        {
            BigDataProcessor2.generalThreadPool.submit(() -> {
                SaveDialog saveDialog = new SaveDialog( viewer, SavingSettings.FileType.TIFF_VOLUMES );
                saveDialog.setVisible(true);
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase( Menu.SAVE_AS_TIFF_PLANES_MENU_ITEM ))
        {
            BigDataProcessor2.generalThreadPool.submit(() -> {
                SaveDialog saveDialog = new SaveDialog( viewer, SavingSettings.FileType.TIFF_PLANES );
                saveDialog.setVisible(true);
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase( CalibrateCommand.COMMAND_NAME ))
        {
            BigDataProcessor2.generalThreadPool.submit(() -> {
                new CalibrationDialog< >( viewer );
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase( Menu.OBLIQUE_MENU_ITEM ))
        {
            BigDataProcessor2.generalThreadPool.submit(() -> {
                ShearMenuDialog shearMenuDialog = new ShearMenuDialog( viewer );
                shearMenuDialog.setVisible(true);
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase( Menu.APPLY_TRACK_MENU_ITEM ))
        {
            BigDataProcessor2.generalThreadPool.submit(() -> {
                new ApplyTrackDialog( viewer );
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase( Menu.REGISTER_VOLUME_SIFT_MENU_ITEM ))
        {
            BigDataProcessor2.generalThreadPool.submit(() ->
			{
                Integer channel = Utils.getChannel( viewer );
                if ( channel == null ) return;
                RegisteredViews.showSIFTVolumeAlignedBdvView( viewer );
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase( Menu.REGISTER_MOVIE_SIFT_MENU_ITEM ))
        {
            BigDataProcessor2.generalThreadPool.submit(() -> {
                Integer channel = Utils.getChannel( viewer );
                if ( channel == null ) return;
                RegisteredViews.createAlignedMovieView( viewer, Registration.SIFT_CORRESPONDENCES, channel );
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase( Menu.REGISTER_MOVIE_PHASE_CORRELATION_MENU_ITEM ))
        {
            BigDataProcessor2.generalThreadPool.submit(() -> {
                Integer channel = Utils.getChannel( viewer );
                if ( channel == null ) return;
                RegisteredViews.createAlignedMovieView( viewer, Registration.PHASE_CORRELATION, 0 );
            });
        }
        else if(e.getActionCommand().equalsIgnoreCase( CropCommand.COMMAND_NAME ))
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
                ImageJFunctions.show( permuted, BigDataProcessor2.generalThreadPool );
            }).start();
        }
        else if(e.getActionCommand().equalsIgnoreCase( ConvertToUnsignedByteTypeCommand.COMMAND_NAME ))
        {
            BigDataProcessor2.generalThreadPool.submit(() ->
			{
                new UnsignedByteTypeConversionDialog( viewer );
            });
        }
        else if(e.getActionCommand().equalsIgnoreCase( BinCommand.COMMAND_NAME ))
        {
            BigDataProcessor2.generalThreadPool.submit(() ->
            {
                new BinDialog<>( viewer );
            });
        }
        else if( e.getActionCommand().equalsIgnoreCase( AlignChannelsCommand.COMMAND_NAME ) )
        {
            BigDataProcessor2.generalThreadPool.submit(() ->
            {
                new ChromaticShiftDialog<>( viewer );
            });
        }
        else if( e.getActionCommand().equalsIgnoreCase( AlignChannelsSplitChipCommand.COMMAND_NAME ) )
        {
            BigDataProcessor2.generalThreadPool.submit(() ->
            {
                new SplitViewMergeDialog( viewer );
            });
        }
        else if( e.getActionCommand().equalsIgnoreCase( Menu.CONFIGURE_LOGGING_MENU_ITEM ))
        {
            BigDataProcessor2.generalThreadPool.submit(() ->
            {
                Logger.showLoggingLevelDialog();
            });
        }
        else if( e.getActionCommand().equalsIgnoreCase( ImageRenameCommand.COMMAND_NAME ) )
        {
            BigDataProcessor2.generalThreadPool.submit(() ->
            {
                new ImageRenameDialog<>( viewer );
            });
        }
        else if( e.getActionCommand().equalsIgnoreCase( RegExpHelpCommand.COMMAND_NAME ) )
        {
            BigDataProcessor2.generalThreadPool.submit(() ->
            {
                Services.commandService.run( RegExpHelpCommand.class, true );
            });
        }
        else if( e.getActionCommand().equalsIgnoreCase( OpenCommand.COMMAND_NAME ) )
        {
            BigDataProcessor2.generalThreadPool.submit(() ->
            {
                AbstractOpenCommand.parentBdvImageViewer = viewer;
                Services.commandService.run( OpenCommand.class, true );
            });
        }
        else if( e.getActionCommand().equalsIgnoreCase( OpenLeicaDSLTiffPlanesCommand.COMMAND_NAME ) )
        {
            BigDataProcessor2.generalThreadPool.submit(() ->
            {
                AbstractOpenCommand.parentBdvImageViewer = viewer;
                Services.commandService.run( OpenLeicaDSLTiffPlanesCommand.class, true );
            });
        }
        else if( e.getActionCommand().equalsIgnoreCase( OpenLuxendoMuViCommand.COMMAND_NAME ) )
        {
            BigDataProcessor2.generalThreadPool.submit(() ->
            {
                AbstractOpenCommand.parentBdvImageViewer = viewer;
                Services.commandService.run( OpenLuxendoMuViCommand.class, true );
            });
        }
        else if( e.getActionCommand().equalsIgnoreCase( OpenLuxendoInViCommand.COMMAND_NAME ) )
        {
            BigDataProcessor2.generalThreadPool.submit(() ->
            {
                AbstractOpenCommand.parentBdvImageViewer = viewer;
                Services.commandService.run( OpenLuxendoInViCommand.class, true );
            });
        }
    }
}
