# Contribute

We are welcoming contributions to BigDataProcessor2 (BDP2)!

The easiest way to get in touch is by submitting an [issue](https://github.com/bigdataprocessor/bigdataprocessor2/issues).

## Adding an image processing step

The key functionality of BDP2 is to process an [Image](https://github.com/bigdataprocessor/bigdataprocessor2/blob/master/src/main/java/de/embl/cba/bdp2/image/Image.java#L17), which essentially is a 5D (x,y,z,c,t) array with some metadata.

Image processing steps are autodiscovered during runtime and can thus also live in other repositories as long as they are on the Java class path during runtime.

In order for an image processing step to be discovered, it must be a SciJava plugin that extends [AbstractImageProcessingCommand](https://github.com/bigdataprocessor/bigdataprocessor2/blob/master/src/main/java/de/embl/cba/bdp2/process/AbstractImageProcessingCommand.java#L14).


### Minimal lazy processing command example
Here is the code for a minimal working example that lazily adds a constant value to all pixels:
```Java
@Plugin(type = AbstractImageProcessingCommand.class, name = AddValueCommand.COMMAND_NAME, menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractImageProcessingCommand.COMMAND_PATH + AddValueCommand.COMMAND_FULL_NAME )
public class AddValueCommand< R extends RealType< R > & NativeType< R > > extends AbstractImageProcessingCommand< R >
{
    public static final String COMMAND_NAME = "Add Value...";
    public static final String COMMAND_FULL_NAME = BigDataProcessor2Menu.COMMAND_BDP2_PREFIX + COMMAND_NAME;

    // ...one may add a callback here to
    // give a live preview of the processed image
    @Parameter(label = "Value")
    public double value;

    public void run()
    {
        outputImage = process( inputImage, value );
        handleOutputImage( false, true );
    }

    /**
     * Static method that does the processing, with signature:
     * outputImage = process( inputImage, parameters... );
     * One does not strictly have to implement this static method with this signature, but we recommend it such that the code can also be easily used via its Java API.
     */
    public static < R extends RealType< R > & NativeType< R > > Image< R > process( Image< R > image, final double value  )
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

        return outputImage;
    }

    /**
     * This is the method that will be called from the BDP2 menu
     * and thus is must be implemented.
     *
     * @param viewer
     *                 The active BigDataViewer window.
     *                 The corresponding active image can be accessed with viewer.getImage()
     */
    @Override
    public void showDialog( ImageViewer< R > viewer )
    {
        // Show the UI of this Command
        Services.getCommandService().run( AddValueCommand.class, true );

        // For simplicity, the "viewer" input parameter is not used in this example.
        // It may be used to build a more sophisticated and interactive UI that automatically operates on and updates the active image. See, e.g. de.embl.cba.bdp2.process.bin.BinCommand
    }
}

```

Another relatively easy example to get started is the [Rename](https://github.com/bigdataprocessor/bigdataprocessor2/blob/master/src/main/java/de/embl/cba/bdp2/process/rename/ImageRenameCommand.java#L15) command.

Many more examples can be found in the [process](https://github.com/bigdataprocessor/bigdataprocessor2/tree/master/src/main/java/de/embl/cba/bdp2/process) package.



Please do not hestitate to [get in touch](https://github.com/bigdataprocessor/bigdataprocessor2/issues) for advise.
