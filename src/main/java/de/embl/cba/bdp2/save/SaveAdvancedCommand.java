package de.embl.cba.bdp2.save;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.BigDataProcessor2Menu;
import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.service.ImageService;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;


/**
 * TODO: How to add a HELP button for the regular expression without screwing up the macro recording?
 *
 *
 * @param <R>
 */
@Plugin(type = Command.class, menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + SaveAdvancedCommand.COMMAND_SAVE_PATH + SaveAdvancedCommand.COMMAND_FULL_NAME )
public class SaveAdvancedCommand< R extends RealType< R > & NativeType< R > > implements Command
{
    public static final String COMMAND_SAVE_PATH = "Commands>Save>";

    public static final String COMMAND_NAME = "Save As...";
    public static final String COMMAND_FULL_NAME = BigDataProcessor2Menu.COMMAND_BDP2_PREFIX + COMMAND_NAME;

    @Parameter(label = "Input image name")
    protected Image< R > inputImage = ImageService.imageNameToImage.values().iterator().next();
    public static final String INPUT_IMAGE_PARAMETER = "inputImage";

    @Parameter(label = "Saving directory", style = "directory")
    File directory;
    public static String DIRECTORY_PARAMETER = "directory";

    @Parameter(label = "Save volumes")
    boolean saveVolumes;
    public static String SAVE_VOLUMES_PARAMETER = "saveVolumes";

    @Parameter(label = "Save projections")
    boolean saveProjections;
    public static String SAVE_PROJECTIONS_PARAMETER = "saveProjections";

    @Parameter(label = "First time frame (zero-based)")
    int tStart = 0;
    public static String T_START_PARAMETER = "tStart";

    @Parameter(label = "Last time frame (inclusive)")
    int tEnd = 0;
    public static String T_END_PARAMETER = "tEnd";


    /**
     * Must be one of the SaveFileType enum class entries
     */
    @Parameter(label = "File type", choices =
            {
                    "TIFFPlanes",
                    "TIFFVolumes",
                    "ImarisVolumes"
            })
    String fileType;
    public static String SAVE_FILE_TYPE_PARAMETER = "fileType";

    @Parameter(label = "TIFF compression", choices =
            {
                    SavingSettings.COMPRESSION_NONE,
                    SavingSettings.COMPRESSION_ZLIB,
                    SavingSettings.COMPRESSION_LZW
            })
    String tiffCompression;
    public static String TIFF_COMPRESSION_PARAMETER = "tiffCompression";

    @Parameter(label = "Number of I/O threads")
    int numIOThreads;
    public static String NUM_IO_THREADS_PARAMETER = "numIOThreads";

    @Parameter(label = "Number of processing threads")
    int numProcessingThreads;
    public static String NUM_PROCESSING_THREADS_PARAMETER = "numProcessingThreads";

    public void run()
    {
        Logger.info( COMMAND_FULL_NAME );

        SavingSettings savingSettings = getSavingSettings();

        BigDataProcessor2.saveImageAndWaitUntilDone( inputImage, savingSettings );

        Logger.info( COMMAND_FULL_NAME + ": Done!" );
    }

    private SavingSettings getSavingSettings()
    {
        SavingSettings savingSettings = new SavingSettings();
        savingSettings.fileType = SaveFileType.valueOf( fileType );
        savingSettings.compression = tiffCompression;
        savingSettings.rowsPerStrip = 10;
        savingSettings.saveVolumes = saveVolumes;
        savingSettings.saveProjections = saveProjections;
        savingSettings.volumesFilePathStump = SavingSettings.createFilePathStump( inputImage, "volumes", directory.toString() );
        savingSettings.projectionsFilePathStump = SavingSettings.createFilePathStump( inputImage, "projections", directory.toString() );
        savingSettings.numIOThreads = numIOThreads;
        savingSettings.numProcessingThreads = numProcessingThreads;
        savingSettings.tStart = tStart;
        savingSettings.tEnd = tEnd;
        // TODO: how to fetch the display settings? They are with the image viewer....
        // savingSettings.displaySettings =

        return savingSettings;
    }
}
