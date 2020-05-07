package de.embl.cba.bdp2.image;

import de.embl.cba.bdp2.process.AbstractProcessingCommand;
import de.embl.cba.bdp2.service.ImageService;
import de.embl.cba.bdp2.utils.Utils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, menuPath = "Plugins>BigDataProcessor2>" + AbstractProcessingCommand.COMMAND_PROCESS_PATH + ImageRenameCommand.COMMAND_FULL_NAME )
public class ImageRenameCommand< R extends RealType< R > & NativeType< R > > extends AbstractProcessingCommand< R >
{
    public static final String COMMAND_NAME = "Rename...";
    public static final String COMMAND_FULL_NAME = Utils.COMMAND_BDP_PREFIX + COMMAND_NAME;

    @Override
    public void run()
    {
        process();
        handleOutputImage( false, true );
        ImageService.nameToImage.put( outputImage.getName(), outputImage );
    }

    private void process()
    {
        outputImage = inputImage.newImage( inputImage.getRai() );
        outputImage.setName( outputImageName );
    }
}
