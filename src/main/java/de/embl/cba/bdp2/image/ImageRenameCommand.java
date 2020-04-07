package de.embl.cba.bdp2.image;

import de.embl.cba.bdp2.scijava.command.AbstractProcessingCommand;
import de.embl.cba.bdp2.service.ImageService;
import de.embl.cba.bdp2.utils.Utils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, menuPath = "Plugins>BigDataProcessor2>Image>" + ImageRenameCommand.COMMAND_NAME )
public class ImageRenameCommand< R extends RealType< R > & NativeType< R > > extends AbstractProcessingCommand< R >
{
    public static final String BDP_MENU_NAME = "Rename...";
    public static final String COMMAND_NAME = Utils.COMMAND_PREFIX + BDP_MENU_NAME;

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
