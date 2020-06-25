package de.embl.cba.bdp2.drift.track;

import de.embl.cba.bdp2.BigDataProcessor2Command;
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

@Plugin(type = Command.class, menuPath = BigDataProcessor2Command.BIGDATAPROCESSOR2_PLUGINS_MENU_ROOT + AbstractProcessingCommand.COMMAND_PROCESS_PATH  + ApplyTrackCommand.COMMAND_FULL_NAME )
public class ApplyTrackCommand< R extends RealType< R > & NativeType< R > > extends AbstractProcessingCommand
{
    public static final String COMMAND_NAME = "Apply Track...";
    public static final String COMMAND_FULL_NAME =  Utils.COMMAND_BDP_PREFIX + COMMAND_NAME;

//    @Parameter(label = "Track")
//    Track track;

    @Parameter(label = "Track")
    File file;

    public void run()
    {
        process();
        showOutputImage();
    }

    private void process()
    {
        final Track track = Tracks.fromJsonFile( file );
        final TrackApplier< R > trackApplier = new TrackApplier<>( inputImage );
        outputImage = trackApplier.applyTrack( track );
    }

    public void showOutputImage()
    {
        final BdvImageViewer viewer = handleOutputImage( false, false );
        BdvUtils.moveToPosition( viewer.getBdvHandle(), new double[]{ 0, 0, 0 }, 0 , 0);
        viewer.autoContrast();
    }
}
