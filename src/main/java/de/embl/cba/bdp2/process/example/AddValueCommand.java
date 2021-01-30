package de.embl.cba.bdp2.process.example;

import de.embl.cba.bdp2.BigDataProcessor2Menu;
import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.process.AbstractImageProcessingCommand;
import de.embl.cba.bdp2.scijava.Services;
import de.embl.cba.bdp2.viewer.ImageViewer;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = AbstractImageProcessingCommand.class, name = AddValueCommand.COMMAND_NAME, menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractImageProcessingCommand.COMMAND_PATH + AddValueCommand.COMMAND_FULL_NAME )
public class AddValueCommand< R extends RealType< R > & NativeType< R > > extends AbstractImageProcessingCommand< R >
{
    public static final String COMMAND_NAME = "Add Value...";
    public static final String COMMAND_FULL_NAME = BigDataProcessor2Menu.COMMAND_BDP2_PREFIX + COMMAND_NAME;

    @Parameter(label = "Value")
    public double value;

    /**
     * Method that will be executed when the OK button is pressed.
     */
    public void run()
    {
        outputImage = addValue( inputImage, value );
        Logger.info( "# " + COMMAND_FULL_NAME );
        Logger.info( "Added value: " + value );

        // call method of parent class to display the processed image
        handleOutputImage( false, true );
    }

    /**
     * Method that does the processing.
     * The fact that it is static enables convenient recording of the Java API.
     */
    public static < R extends RealType< R > & NativeType< R > > Image< R > addValue( Image< R > image, final double value  )
    {
        // Make a copy of the image (no pixel data is copied here)
        Image< R > outputImage = new Image<>( image );

        // Get the 5D rai (x,y,z,c,t) containing the pixel data
        final RandomAccessibleInterval< R > rai = image.getRai();

        // Lazily add the value to each pixel in the rai
        // Note: There are no checks in this implementation whether the
        // result can be represented in the current data type R
        final RandomAccessibleInterval< R > convert = Converters.convert( rai, ( i, o ) -> o.setReal( i.getRealDouble() + value ), Util.getTypeFromInterval( rai ) );

        // Set this rai as the pixel source of the output image
        outputImage.setRai( convert );

        // Append suffix to image name, indicating the processing
        outputImage.setName( image.getName() + "-add" );

        return outputImage;
    }

    /**
     * This is the method that will be called from the BDP2 menu.
     *
     * @param imageViewer
     */
    @Override
    public void showDialog( ImageViewer< R > imageViewer )
    {
        // Show the UI of this Command
        Services.getCommandService().run( AddValueCommand.class, true );
    }
}
