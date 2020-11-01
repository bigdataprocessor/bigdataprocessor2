package de.embl.cba.bdp2.process.convert;

import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.process.AbstractImageProcessingCommand;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewer.ImageViewer;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.ArrayList;

import static de.embl.cba.bdp2.dialog.Utils.*;

@Plugin(type = AbstractImageProcessingCommand.class, name = MultiChannelUnsignedByteTypeConverterCommand.COMMAND_NAME, menuPath = BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractImageProcessingCommand.COMMAND_PROCESS_PATH + MultiChannelUnsignedByteTypeConverterCommand.COMMAND_FULL_NAME )
public class MultiChannelUnsignedByteTypeConverterCommand< R extends RealType< R > & NativeType< R > > extends AbstractImageProcessingCommand< R >
{
    public static final String COMMAND_NAME = "Convert to 8-Bit...";
    public static final String COMMAND_FULL_NAME = Utils.COMMAND_BDP2_PREFIX + COMMAND_NAME;

    @Parameter(label = "Map to 0 [channel0, channel1, ...]")
    String mapTo0 = "0, 0";

    @Parameter(label = "Map to 255 [channel0, channel1, ...]")
    String mapTo255 = "65535, 65535";

    @Override
    public void run()
    {
        Logger.info( COMMAND_FULL_NAME );
        process();
        handleOutputImage( true, true );
    }

    private void process()
    {
        int[] mins = Utils.delimitedStringToIntegerArray( mapTo0, "," );
        int[] maxs = Utils.delimitedStringToIntegerArray( mapTo255, "," );

        long numChannels = inputImage.getNumChannels();

        if ( mins.length != maxs.length || mins.length != numChannels )
        {
            Logger.error( "The min and max mappings for the conversion must have the same length and correspond to the number of channels of the image.");
            return;
        }

        ArrayList< double[] > contrastLimits = new ArrayList<>();
        for ( int channel = 0; channel < numChannels; channel++ )
        {
            contrastLimits.add( new double[]{ mins[ channel ], maxs[ channel ] } );
        }

        MultiChannelUnsignedByteTypeConverter converter = new MultiChannelUnsignedByteTypeConverter<>( inputImage, contrastLimits );

        outputImage = converter.getConvertedImage();
    }

    @Override
    public void showDialog( ImageViewer< R > imageViewer )
    {
        new MultiChannelUnsignedByteTypeConverterDialog< R >( imageViewer ).showDialog();
    }
}
