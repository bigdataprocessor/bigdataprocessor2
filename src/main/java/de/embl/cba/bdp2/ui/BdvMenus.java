package de.embl.cba.bdp2.ui;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.bin.BinningDialog;
import de.embl.cba.bdp2.calibrate.CalibrationDialog;
import de.embl.cba.bdp2.convert.UnsignedByteTypeConversionDialog;
import de.embl.cba.bdp2.crop.CroppingDialog;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.process.splitviewmerge.SplitViewMergingDialog;
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

public class BdvMenus
        extends JMenu implements ActionListener {

    private final ImageMenu imageMenu;
    private final OthersMenu otherMenu;
    private final ProcessMenu processMenu;
    private BdvImageViewer viewer;

    public BdvMenus(){
        imageMenu = new ImageMenu(this);
        processMenu = new ProcessMenu(this);
        otherMenu = new OthersMenu(this);
    }

    public void setViewer( BdvImageViewer viewer ){
        this.viewer = viewer;
    }

    public List< JMenu > getMenus(){ //Add new menu items here.
        List<JMenu> jMenuList = new ArrayList<>();
        jMenuList.add( imageMenu );
        jMenuList.add( processMenu );
        jMenuList.add( otherMenu );
        return jMenuList;
    }

    @Override
    public synchronized void actionPerformed(ActionEvent e)
    {
        if (e.getActionCommand().equalsIgnoreCase(
                UIDisplayConstants.SAVE_AS_MENU_ITEM ))
        {
            BigDataProcessor2.generalThreadPool.submit(() -> {
                SaveMenuDialog saveMenuDialog = new SaveMenuDialog( viewer );
                saveMenuDialog.setVisible(true);
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase(
                UIDisplayConstants.CALIBRATE_MENU_ITEM ))
        {
            BigDataProcessor2.generalThreadPool.submit(() -> {
                new CalibrationDialog< >( viewer );
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase(
                UIDisplayConstants.OBLIQUE_MENU_ITEM ))
        {
            BigDataProcessor2.generalThreadPool.submit(() -> {
                ShearMenuDialog shearMenuDialog = new ShearMenuDialog( viewer );
                shearMenuDialog.setVisible(true);
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase(
        		UIDisplayConstants.APPLY_TRACK_MENU_ITEM ))
        {
            BigDataProcessor2.generalThreadPool.submit(() -> {
                new ApplyTrackDialog( viewer );
            });
        }else if (e.getActionCommand().equalsIgnoreCase(
        		UIDisplayConstants.REGISTER_VOLUME_SIFT_MENU_ITEM ))
        {
            BigDataProcessor2.generalThreadPool.submit(() ->
			{
                Integer channel = Utils.getChannel( viewer );
                if ( channel == null ) return;
                RegisteredViews.showSIFTVolumeAlignedBdvView( viewer );
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase(
        		UIDisplayConstants.REGISTER_MOVIE_SIFT_MENU_ITEM ))
        {
            BigDataProcessor2.generalThreadPool.submit(() -> {
                Integer channel = Utils.getChannel( viewer );
                if ( channel == null ) return;
                RegisteredViews.createAlignedMovieView( viewer, Registration.SIFT_CORRESPONDENCES, channel );
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase(
        		UIDisplayConstants.REGISTER_MOVIE_PHASE_CORRELATION_MENU_ITEM ))
        {
            BigDataProcessor2.generalThreadPool.submit(() -> {
                Integer channel = Utils.getChannel( viewer );
                if ( channel == null ) return;
                RegisteredViews.createAlignedMovieView( viewer, Registration.PHASE_CORRELATION, 0 );
            });
        }
        else if(e.getActionCommand().equalsIgnoreCase(
        		UIDisplayConstants.CROP_MENU_ITEM ))
        {
        	new Thread( () ->  {
        	    new CroppingDialog<>( viewer );
            }).start();
        }
        else if(e.getActionCommand().equalsIgnoreCase(
        		UIDisplayConstants.IMAGEJ_VIEW_MENU_ITEM ))
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
        }else if(e.getActionCommand().equalsIgnoreCase(
                UIDisplayConstants.EIGHT_BIT_CONVERSION_MENU_ITEM ))
        {
            BigDataProcessor2.generalThreadPool.submit(() ->
			{
                new UnsignedByteTypeConversionDialog( viewer );
            });
        }
        else if(e.getActionCommand().equalsIgnoreCase(
                UIDisplayConstants.BINNING_MENU_ITEM ))
        {
            BigDataProcessor2.generalThreadPool.submit(() ->
            {
                new BinningDialog<>( viewer );
            });
        }
        else if(e.getActionCommand().equalsIgnoreCase(
            UIDisplayConstants.CHROMATIC_SHIFT_CORRECTION_MENU_ITEM )){
            BigDataProcessor2.generalThreadPool.submit(() -> {
                new ChromaticShiftDialog<>( viewer );
            });
        }else if(e.getActionCommand().equalsIgnoreCase(
                    UIDisplayConstants.SPLIT_VIEW_MENU_ITEM ))
        {
                BigDataProcessor2.generalThreadPool.submit(() -> {
                    // TODO: Make Command
                    new SplitViewMergingDialog( viewer );
                });
        }
        else if(e.getActionCommand().equalsIgnoreCase(
                UIDisplayConstants.CONFIGURE_LOGGING_MENU_ITEM ))
        {
            BigDataProcessor2.generalThreadPool.submit(() -> {
                Logger.showLoggingLevelDialog();
            });
        }
    }

}
