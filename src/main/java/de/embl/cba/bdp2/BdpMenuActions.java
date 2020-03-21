package de.embl.cba.bdp2;

import de.embl.cba.bdp2.bin.BinDialog;
import de.embl.cba.bdp2.calibrate.CalibrationDialog;
import de.embl.cba.bdp2.convert.UnsignedByteTypeConversionDialog;
import de.embl.cba.bdp2.crop.CropDialog;
import de.embl.cba.bdp2.dialog.MiscMenu;
import de.embl.cba.bdp2.dialog.Utils;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.save.SaveDialog;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.splitviewmerge.SplitViewMergeDialog;
import de.embl.cba.bdp2.shift.ChromaticShiftDialog;
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

public class BdpMenuActions extends JMenu implements ActionListener {

    //private final ImageMenu imageMenu;
    private final MiscMenu miscMenu;
    private final BdpMenu bdpMenu;
    private BdvImageViewer viewer;

    public BdpMenuActions()
    {
        bdpMenu = new BdpMenu(this);
        miscMenu = new MiscMenu(this);
    }

    public void setViewer( BdvImageViewer viewer ){
        this.viewer = viewer;
    }

    public List< JMenu > getMenus(){ //Add new menu items here.
        List<JMenu> jMenuList = new ArrayList<>();
        jMenuList.add( bdpMenu );
        jMenuList.add( miscMenu );
        return jMenuList;
    }

    @Override
    public synchronized void actionPerformed(ActionEvent e)
    {
        if (e.getActionCommand().equalsIgnoreCase(
                BdpMenu.SAVE_AS_IMARIS_VOLUMES_MENU_ITEM ))
        {
            BigDataProcessor2.generalThreadPool.submit(() -> {
                SaveDialog saveDialog = new SaveDialog( viewer, SavingSettings.FileType.IMARIS_VOLUMES );
                saveDialog.setVisible(true);
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase(
                BdpMenu.SAVE_AS_TIFF_VOLUMES_MENU_ITEM ))
        {
            BigDataProcessor2.generalThreadPool.submit(() -> {
                SaveDialog saveDialog = new SaveDialog( viewer, SavingSettings.FileType.TIFF_VOLUMES );
                saveDialog.setVisible(true);
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase(
                BdpMenu.SAVE_AS_TIFF_PLANES_MENU_ITEM ))
        {
            BigDataProcessor2.generalThreadPool.submit(() -> {
                SaveDialog saveDialog = new SaveDialog( viewer, SavingSettings.FileType.TIFF_PLANES );
                saveDialog.setVisible(true);
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase(
                BdpMenu.CALIBRATE_MENU_ITEM ))
        {
            BigDataProcessor2.generalThreadPool.submit(() -> {
                new CalibrationDialog< >( viewer );
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase(
                BdpMenu.OBLIQUE_MENU_ITEM ))
        {
            BigDataProcessor2.generalThreadPool.submit(() -> {
                ShearMenuDialog shearMenuDialog = new ShearMenuDialog( viewer );
                shearMenuDialog.setVisible(true);
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase(
        		BdpMenu.APPLY_TRACK_MENU_ITEM ))
        {
            BigDataProcessor2.generalThreadPool.submit(() -> {
                new ApplyTrackDialog( viewer );
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase(
        		BdpMenu.REGISTER_VOLUME_SIFT_MENU_ITEM ))
        {
            BigDataProcessor2.generalThreadPool.submit(() ->
			{
                Integer channel = Utils.getChannel( viewer );
                if ( channel == null ) return;
                RegisteredViews.showSIFTVolumeAlignedBdvView( viewer );
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase(
        		BdpMenu.REGISTER_MOVIE_SIFT_MENU_ITEM ))
        {
            BigDataProcessor2.generalThreadPool.submit(() -> {
                Integer channel = Utils.getChannel( viewer );
                if ( channel == null ) return;
                RegisteredViews.createAlignedMovieView( viewer, Registration.SIFT_CORRESPONDENCES, channel );
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase(
        		BdpMenu.REGISTER_MOVIE_PHASE_CORRELATION_MENU_ITEM ))
        {
            BigDataProcessor2.generalThreadPool.submit(() -> {
                Integer channel = Utils.getChannel( viewer );
                if ( channel == null ) return;
                RegisteredViews.createAlignedMovieView( viewer, Registration.PHASE_CORRELATION, 0 );
            });
        }
        else if(e.getActionCommand().equalsIgnoreCase(
        		BdpMenu.CROP_MENU_ITEM ))
        {
        	new Thread( () ->  {
        	    new CropDialog<>( viewer );
            }).start();
        }
        else if(e.getActionCommand().equalsIgnoreCase(
        		BdpMenu.IMAGEJ_VIEW_MENU_ITEM ))
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
        else if(e.getActionCommand().equalsIgnoreCase(
                BdpMenu.EIGHT_BIT_CONVERSION_MENU_ITEM ))
        {
            BigDataProcessor2.generalThreadPool.submit(() ->
			{
                new UnsignedByteTypeConversionDialog( viewer );
            });
        }
        else if(e.getActionCommand().equalsIgnoreCase(
                BdpMenu.BINNING_MENU_ITEM ))
        {
            BigDataProcessor2.generalThreadPool.submit(() ->
            {
                new BinDialog<>( viewer );
            });
        }
        else if(e.getActionCommand().equalsIgnoreCase(
            BdpMenu.CHROMATIC_SHIFT_CORRECTION_MENU_ITEM ))
        {
            BigDataProcessor2.generalThreadPool.submit(() ->
            {
                new ChromaticShiftDialog<>( viewer );
            });
        }
        else if(e.getActionCommand().equalsIgnoreCase(
                    BdpMenu.SPLIT_VIEW_MENU_ITEM ))
        {
                BigDataProcessor2.generalThreadPool.submit(() -> {
                    // TODO: Make Command
                    new SplitViewMergeDialog( viewer );
                });
        }
        else if(e.getActionCommand().equalsIgnoreCase(
                BdpMenu.CONFIGURE_LOGGING_MENU_ITEM ))
        {
            BigDataProcessor2.generalThreadPool.submit(() -> {
                Logger.showLoggingLevelDialog();
            });
        }
    }

}
