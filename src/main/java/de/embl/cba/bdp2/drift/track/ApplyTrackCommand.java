package de.embl.cba.bdp2.drift.track;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.process.AbstractProcessingCommand;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import de.embl.cba.bdv.utils.BdvUtils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;

@Plugin(type = Command.class, menuPath = de.embl.cba.bdp2.dialog.Utils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractProcessingCommand.COMMAND_PROCESS_PATH  + ApplyTrackCommand.COMMAND_FULL_NAME )
public class ApplyTrackCommand< R extends RealType< R > & NativeType< R > > extends AbstractProcessingCommand
{
    public static final String COMMAND_NAME = "Apply Track...";
    public static final String COMMAND_FULL_NAME =  Utils.COMMAND_BDP2_PREFIX + COMMAND_NAME;

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
        final BdvImageViewer viewer = handleOutputImage( false, false );
        if ( centerImage )
            BdvUtils.moveToPosition( viewer.getBdvHandle(), new double[]{ 0, 0, 0 }, 0 , 0);
    }
}
