package de.embl.cba.bdp2;

import de.embl.cba.bdp2.log.LoggingLevelCommand;
import de.embl.cba.bdp2.open.AbstractOpenFileSeriesCommand;
import de.embl.cba.bdp2.open.bioformats.OpenBDVBioFormatsCommand;
import de.embl.cba.bdp2.open.fileseries.*;
import de.embl.cba.bdp2.process.bin.BinCommand;
import de.embl.cba.bdp2.process.bin.BinDialog;
import de.embl.cba.bdp2.process.calibrate.SetVoxelSizeCommand;
import de.embl.cba.bdp2.process.calibrate.CalibrationDialog;
import de.embl.cba.bdp2.process.convert.MultiChannelUnsignedByteTypeConverterCommand;
import de.embl.cba.bdp2.process.convert.MultiChannelUnsignedByteTypeConverterDialog;
import de.embl.cba.bdp2.process.crop.CropCommand;
import de.embl.cba.bdp2.process.crop.CropDialog;
import de.embl.cba.bdp2.open.samples.DownloadAndOpenSampleDataCommand;
import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.save.SaveFileType;
import de.embl.cba.bdp2.track.ApplyTrackCommand;
import de.embl.cba.bdp2.track.ApplyTrackDialog;
import de.embl.cba.bdp2.track.TrackCreator;
import de.embl.cba.bdp2.process.rename.ImageRenameCommand;
import de.embl.cba.bdp2.process.rename.ImageRenameDialog;
import de.embl.cba.bdp2.record.LanguageDialog;
import de.embl.cba.bdp2.save.SaveAdvancedDialog;
import de.embl.cba.bdp2.scijava.Services;
import de.embl.cba.bdp2.process.align.channelshift.AlignChannelsCommand;
import de.embl.cba.bdp2.process.align.splitchip.SplitChipCommand;
import de.embl.cba.bdp2.process.align.splitchip.SplitChipDialog;
import de.embl.cba.bdp2.process.align.channelshift.AlignChannelsDialog;
import de.embl.cba.bdp2.devel.register.RegisteredViews;
import de.embl.cba.bdp2.devel.register.Registration;
import de.embl.cba.bdp2.service.ImageViewerService;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewer.ImageViewer;
import de.embl.cba.tables.FileAndUrlUtils;
import ij.IJ;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class BigDataProcessor2MenuActions implements ActionListener {

    private final BigDataProcessor2Menu menu;
    private ImageViewer viewer;
    private final ArrayList< JMenu > menus;

    public BigDataProcessor2MenuActions()
    {
        menu = new BigDataProcessor2Menu(this);
        menus = menu.getMenus();
    }

    public void setViewer( ImageViewer viewer ){
        this.viewer = viewer;
    }

    public List< JMenu > getMenus()
    {
        return menus;
    }

    @Override
    public synchronized void actionPerformed(ActionEvent e)
    {
        final ImageViewer activeViewer = ImageViewerService.getActiveViewer();

        this.viewer = activeViewer;

        if (e.getActionCommand().equalsIgnoreCase( BigDataProcessor2Menu.SAVE_AS_IMARIS_VOLUMES_MENU_ITEM ))
        {
            BigDataProcessor2.threadPool.submit(() -> {
                SaveAdvancedDialog saveDialog = new SaveAdvancedDialog( viewer, SaveFileType.ImarisVolumes );
                saveDialog.setVisible(true);
            });
        }
        else if ( e.getActionCommand().equalsIgnoreCase( BigDataProcessor2Menu.ABOUT ) )
        {
            BigDataProcessor2.threadPool.submit(() ->
            {
                FileAndUrlUtils.openURI( "https://imagej.net/BigDataProcessor2" );
            });
        }
        else if ( e.getActionCommand().equalsIgnoreCase( BigDataProcessor2Menu.CITE ) )
        {
            BigDataProcessor2.threadPool.submit(() ->
            {
                FileAndUrlUtils.openURI( "https://github.com/bigdataprocessor/bigDataProcessor2/blob/master/README.md#cite" );
            });
        }
        else if ( e.getActionCommand().equalsIgnoreCase( BigDataProcessor2Menu.README ) )
        {
            BigDataProcessor2.threadPool.submit(() ->
            {
                FileAndUrlUtils.openURI( "https://github.com/bigdataprocessor/bigDataProcessor2/blob/master/README.md#user-guide" );
            });
        }
        else if ( e.getActionCommand().equalsIgnoreCase( BigDataProcessor2Menu.ISSUE ) )
        {
            BigDataProcessor2.threadPool.submit(() ->
            {
                FileAndUrlUtils.openURI( "https://github.com/bigdataprocessor/bigDataProcessor2/issues" );
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase( BigDataProcessor2Menu.SAVE_AS_TIFF_VOLUMES_MENU_ITEM ))
        {
            BigDataProcessor2.threadPool.submit(() -> {
                if (! isImageSelected( viewer ) ) return;
                SaveAdvancedDialog saveDialog = new SaveAdvancedDialog( viewer, SaveFileType.TIFFVolumes );
                saveDialog.setVisible(true);
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase( BigDataProcessor2Menu.SAVE_AS_TIFF_PLANES_MENU_ITEM ))
        {
            BigDataProcessor2.threadPool.submit(() -> {
                if (! isImageSelected( viewer ) ) return;
                SaveAdvancedDialog saveDialog = new SaveAdvancedDialog( viewer, SaveFileType.TIFFPlanes );
                saveDialog.setVisible(true);
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase( SetVoxelSizeCommand.COMMAND_NAME ))
        {
            BigDataProcessor2.threadPool.submit(() -> {
                if (! isImageSelected( viewer ) ) return;
                new CalibrationDialog< >( viewer ).showDialog();
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase( BigDataProcessor2Menu.RECORD ))
        {
            BigDataProcessor2.threadPool.submit(() -> {
                new LanguageDialog();
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase( BigDataProcessor2Menu.CREATE_TRACK ))
        {
            BigDataProcessor2.threadPool.submit(() -> {
                if (! isImageSelected( viewer ) ) return;
                new TrackCreator( viewer, "track" );
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase( ApplyTrackCommand.COMMAND_NAME ))
        {
            BigDataProcessor2.threadPool.submit(() -> {
                if (! isImageSelected( viewer ) ) return;
                new ApplyTrackDialog( viewer );
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase( BigDataProcessor2Menu.REGISTER_VOLUME_SIFT_MENU_ITEM ))
        {
            BigDataProcessor2.threadPool.submit(() ->
			{
                if (! isImageSelected( viewer ) ) return;
                Integer channel = DialogUtils.getChannel( viewer );
                if ( channel == null ) return;
                RegisteredViews.showSIFTVolumeAlignedBdvView( viewer );
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase( BigDataProcessor2Menu.REGISTER_MOVIE_SIFT_MENU_ITEM ))
        {
            BigDataProcessor2.threadPool.submit(() -> {
                if (! isImageSelected( viewer ) ) return;
                Integer channel = DialogUtils.getChannel( viewer );
                if ( channel == null ) return;
                RegisteredViews.createAlignedMovieView( viewer, Registration.SIFT_CORRESPONDENCES, channel );
            });
        }
        else if (e.getActionCommand().equalsIgnoreCase( BigDataProcessor2Menu.REGISTER_MOVIE_PHASE_CORRELATION_MENU_ITEM ))
        {
            BigDataProcessor2.threadPool.submit(() -> {
                if (! isImageSelected( viewer ) ) return;
                Integer channel = DialogUtils.getChannel( viewer );
                if ( channel == null ) return;
                RegisteredViews.createAlignedMovieView( viewer, Registration.PHASE_CORRELATION, 0 );
            });
        }
        else if(e.getActionCommand().equalsIgnoreCase( CropCommand.COMMAND_NAME ))
        {
            BigDataProcessor2.threadPool.submit(() -> {
                if (! isImageSelected( viewer ) ) return;
                new CropDialog<>( viewer );
            });
        }
        else if(e.getActionCommand().equalsIgnoreCase( BigDataProcessor2Menu.IMAGEJ_VIEW_MENU_ITEM ))
        {
            BigDataProcessor2.threadPool.submit(() -> {
                if (! isImageSelected( viewer ) ) return;
                Utils.asImagePlus( viewer.getImage() ).show();
            });
        }
        else if(e.getActionCommand().equalsIgnoreCase( MultiChannelUnsignedByteTypeConverterCommand.COMMAND_NAME ))
        {
            BigDataProcessor2.threadPool.submit(() ->
			{
                if (! isImageSelected( viewer ) ) return;
                new MultiChannelUnsignedByteTypeConverterDialog<>( viewer );
            });
        }
        else if(e.getActionCommand().equalsIgnoreCase( BinCommand.COMMAND_NAME ))
        {
            BigDataProcessor2.threadPool.submit(() ->
            {
                if (! isImageSelected( viewer ) ) return;
                new BinDialog<>( viewer );
            });
        }
        else if( e.getActionCommand().equalsIgnoreCase( AlignChannelsCommand.COMMAND_NAME ) )
        {
            BigDataProcessor2.threadPool.submit(() ->
            {
                if (! isImageSelected( viewer ) ) return;
                new AlignChannelsDialog<>( viewer );
            });
        }
        else if( e.getActionCommand().equalsIgnoreCase( SplitChipCommand.COMMAND_NAME ) )
        {
            BigDataProcessor2.threadPool.submit(() ->
            {
                if (! isImageSelected( viewer ) ) return;
                new SplitChipDialog( viewer );
            });
        }
        else if( e.getActionCommand().equalsIgnoreCase( BigDataProcessor2Menu.LOG ))
        {
            BigDataProcessor2.threadPool.submit(() ->
            {
                Services.getCommandService().run( LoggingLevelCommand.class, true );
            });
        }
        else if( e.getActionCommand().equalsIgnoreCase( ImageRenameCommand.COMMAND_NAME ) )
        {
            BigDataProcessor2.threadPool.submit(() ->
            {
                if (! isImageSelected( viewer ) ) return;
                new ImageRenameDialog<>( viewer );
            });
        }
        else if( e.getActionCommand().equalsIgnoreCase( OpenHelpCommand.COMMAND_NAME ) )
        {
            BigDataProcessor2.threadPool.submit(() ->
            {
                Services.getCommandService().run( OpenHelpCommand.class, true );
            });
        }
        else if( e.getActionCommand().equalsIgnoreCase( OpenBDVBioFormatsCommand.COMMAND_NAME ) )
        {
            BigDataProcessor2.threadPool.submit(() ->
            {
                Services.getCommandService().run( OpenBDVBioFormatsCommand.class, true );
            });
        }
        else if( e.getActionCommand().equalsIgnoreCase( DownloadAndOpenSampleDataCommand.COMMAND_NAME ) )
        {
            BigDataProcessor2.threadPool.submit(() ->
            {
                DownloadAndOpenSampleDataCommand.parentImageViewer = viewer;
                Services.getCommandService().run( DownloadAndOpenSampleDataCommand.class, true );
            });
        }
        else if( e.getActionCommand().equalsIgnoreCase( OpenEMTIFFPlanesFileSeriesCommand.COMMAND_NAME ) )
        {
            BigDataProcessor2.threadPool.submit(() ->
            {
                OpenEMTIFFPlanesFileSeriesCommand.parentViewer = viewer;
                Services.getCommandService().run( OpenEMTIFFPlanesFileSeriesCommand.class, true );
            });
        }
        else if( e.getActionCommand().equalsIgnoreCase( OpenSingleTIFFVolumeCommand.COMMAND_NAME ) )
        {
            BigDataProcessor2.threadPool.submit(() ->
            {
                OpenSingleTIFFVolumeCommand.parentViewer = viewer;
                Services.getCommandService().run( OpenSingleTIFFVolumeCommand.class, true );
            });
        }
        else if( e.getActionCommand().equalsIgnoreCase( OpenFileSeriesCommand.COMMAND_NAME ) )
        {
            BigDataProcessor2.threadPool.submit(() ->
            {
                AbstractOpenFileSeriesCommand.parentViewer = viewer;
                Services.getCommandService().run( OpenFileSeriesCommand.class, true );
            });
        }
        else if( e.getActionCommand().equalsIgnoreCase( OpenLeicaDSLTIFFPlaneSeriesCommand.COMMAND_NAME ) )
        {
            BigDataProcessor2.threadPool.submit(() ->
            {
                AbstractOpenFileSeriesCommand.parentViewer = viewer;
                Services.getCommandService().run( OpenLeicaDSLTIFFPlaneSeriesCommand.class, true );
            });
        }
        else if( e.getActionCommand().equalsIgnoreCase( OpenLuxendoHDF5SeriesCommand.COMMAND_NAME ) )
        {
            BigDataProcessor2.threadPool.submit(() ->
            {
                AbstractOpenFileSeriesCommand.parentViewer = viewer;
                Services.getCommandService().run( OpenLuxendoHDF5SeriesCommand.class, true );
            });
        }
        else if( e.getActionCommand().equalsIgnoreCase( OpenViventisTIFFSeriesCommand.COMMAND_NAME ) )
        {
            BigDataProcessor2.threadPool.submit(() ->
            {
                AbstractOpenFileSeriesCommand.parentViewer = viewer;
                Services.getCommandService().run( OpenViventisTIFFSeriesCommand.class, true );
            });
        }
    }

    private boolean isImageSelected( ImageViewer viewer )
    {
        if ( viewer == null )
        {
            IJ.showMessage("No image selected.\n\nPlease select an image by either\n- clicking on an existing BigDataViewer window, or\n- open a new image using the [ BigDataProcessor2 > Open ] menu.");
            return false;
        }
        else
        {
            return true;
        }
    }
}
