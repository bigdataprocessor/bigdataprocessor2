package de.embl.cba.bdp2.image;

import de.embl.cba.bdp2.process.AbstractProcessingCommand;
import de.embl.cba.bdp2.service.ImageService;
import de.embl.cba.bdp2.utils.Utils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Arrays;

@Plugin(type = Command.class, menuPath = de.embl.cba.bdp2.dialog.Utils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractProcessingCommand.COMMAND_PROCESS_PATH + ImageRenameCommand.COMMAND_FULL_NAME )
public class ImageRenameCommand< R extends RealType< R > & NativeType< R > > extends AbstractProcessingCommand< R >
{
    public static final String COMMAND_NAME = "Rename...";
    public static final String COMMAND_FULL_NAME = Utils.COMMAND_BDP2_PREFIX + COMMAND_NAME;

    @Parameter(label = "Channel names (comma separated list)")
    String channelNames = "ch0,ch1,ch2";
    public static String CHANNEL_NAMES_PARAMETER = "channelNames";

    @Override
    public void run()
    {
        process();
        handleOutputImage( false, true );
        ImageService.imageNameToImage.put( outputImage.getName(), outputImage );
    }

    private void process()
    {
        outputImage = inputImage.newImage( inputImage.getRai() );
        outputImage.setName( outputImageName );
        final String[] names = Arrays.stream( channelNames.split( "," ) ).map( String::trim ).toArray( String[]::new );
        outputImage.setChannelNames( names );
    }
}
