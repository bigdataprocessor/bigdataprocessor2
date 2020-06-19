package de.embl.cba.bdp2.drift.track;

import de.embl.cba.bdp2.process.AbstractProcessingCommand;
import de.embl.cba.bdp2.utils.Utils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, menuPath = "Plugins>BigDataProcessor2>" + AbstractProcessingCommand.COMMAND_PROCESS_PATH  + CorrectDriftWithTrackCommand.COMMAND_FULL_NAME )
public class CorrectDriftWithTrackCommand< R extends RealType< R > & NativeType< R > > extends AbstractProcessingCommand
{
    public static final String COMMAND_NAME = "Apply Track...";
    public static final String COMMAND_FULL_NAME =  Utils.COMMAND_BDP_PREFIX + COMMAND_NAME;

    @Parameter(label = "Track")
    Track track;

    public void run()
    {
        process();
        handleOutputImage( true, false );
     }

    private void process()
    {
        final TrackApplier< R > trackApplier = new TrackApplier<>( inputImage );
        outputImage = trackApplier.applyTrack( track );
    }
}
