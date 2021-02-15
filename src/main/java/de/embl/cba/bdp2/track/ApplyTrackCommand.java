package de.embl.cba.bdp2.track;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.BigDataProcessor2Menu;
import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.process.AbstractImageProcessingCommand;
import de.embl.cba.bdp2.viewer.ImageViewer;
import de.embl.cba.bdv.utils.BdvUtils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;

@Plugin(type = Command.class, name = ApplyTrackCommand.COMMAND_NAME, menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractImageProcessingCommand.COMMAND_PATH + ApplyTrackCommand.COMMAND_FULL_NAME )
public class ApplyTrackCommand< R extends RealType< R > & NativeType< R > > extends AbstractImageProcessingCommand< R >
{
    public static final String COMMAND_NAME = "Apply Track...";
    public static final String COMMAND_FULL_NAME =  BigDataProcessor2Menu.COMMAND_BDP2_PREFIX + COMMAND_NAME;

//    @Parameter(label = "Track")
//    Track track;

    @Parameter(label = "Track")
    File file;
    public static final String TRACK_FILE_PARAMETER = "file";

    @Parameter(label = "Center image on track positions")
    Boolean centerImage = false;
    public static final String CENTER_IMAGE_PARAMETER = "centerImage";

    public void run()
    {
        process();
        showOutputImage();
    }

    private void process()
    {
        outputImage = BigDataProcessor2.applyTrack( file, inputImage, centerImage );
    }

    public void showOutputImage()
    {
        final ImageViewer viewer = handleOutputImage( false, false );
        if ( centerImage )
            BdvUtils.moveToPosition( viewer.getBdvHandle(), new double[]{ 0, 0, 0 }, 0 , 0);
    }

    @Override
    public void showDialog( ImageViewer< R > imageViewer )
    {
        new ApplyTrackDialog<>( imageViewer ).showDialog();
    }
}
