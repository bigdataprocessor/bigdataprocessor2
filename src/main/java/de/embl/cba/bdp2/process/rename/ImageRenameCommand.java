package de.embl.cba.bdp2.process.rename;

import de.embl.cba.bdp2.BigDataProcessor2Menu;
import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.process.AbstractImageProcessingCommand;
import de.embl.cba.bdp2.service.ImageService;
import de.embl.cba.bdp2.viewer.ImageViewer;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Arrays;

@Plugin(type = AbstractImageProcessingCommand.class, name = ImageRenameCommand.COMMAND_NAME, menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractImageProcessingCommand.COMMAND_PATH + ImageRenameCommand.COMMAND_FULL_NAME )
public class ImageRenameCommand< R extends RealType< R > & NativeType< R > > extends AbstractImageProcessingCommand< R >
{
    public static final String COMMAND_NAME = "Rename...";
    public static final String COMMAND_FULL_NAME = BigDataProcessor2Menu.COMMAND_BDP2_PREFIX + COMMAND_NAME;

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
        outputImage = new Image<>( inputImage );
        outputImage.setRai( inputImage.getRai() );
        outputImage.setName( outputImageName );
        final String[] names = Arrays.stream( channelNames.split( "," ) ).map( String::trim ).toArray( String[]::new );
        outputImage.setChannelNames( names );
    }

    @Override
    public void showDialog( ImageViewer< R > imageViewer )
    {
        new ImageRenameDialog<>( imageViewer ).showDialog();
    }
}
