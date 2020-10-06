# Contribute

We are welcoming contributions to BigDataProcessor2 (BDP2)!

The easiest way to get in touch is by submitting an [issue](https://github.com/bigdataprocessor/bigdataprocessor2/issues).

## Adding an image processing step

The key functionality of BDP2 is to process an [Image](https://github.com/bigdataprocessor/bigdataprocessor2/blob/master/src/main/java/de/embl/cba/bdp2/image/Image.java#L17), which essentially is a 5D (x,y,z,c,t) array with some metadata.

Image processing steps are autodiscovered during runtime and can thus also live in other repositories as long as they are on the Java class path during runtime.

In order for an image processing step to be discovered, it must be a SciJava plugin of type [AbstractImageProcessingCommand](https://github.com/bigdataprocessor/bigdataprocessor2/blob/master/src/main/java/de/embl/cba/bdp2/process/AbstractImageProcessingCommand.java#L14).

Many examples can be found in the [process](https://github.com/bigdataprocessor/bigdataprocessor2/tree/master/src/main/java/de/embl/cba/bdp2/process) package.

An easy example to get started is the [Rename](https://github.com/bigdataprocessor/bigdataprocessor2/blob/master/src/main/java/de/embl/cba/bdp2/process/rename/ImageRenameCommand.java#L15) command.

Please do not hestitate to [get in touch](https://github.com/bigdataprocessor/bigdataprocessor2/issues) for advise.
