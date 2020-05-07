package de.embl.cba.bdp2.open;

import de.embl.cba.bdp2.calibrate.CalibrationUtils;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.read.NamingScheme;
import loci.common.DebugTools;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.swing.*;

import static de.embl.cba.bdp2.utils.Utils.COMMAND_BDP_PREFIX;


/**
 * TODO: How to add a HELP button for the regular expression without screwing up the macro recording?
 *
 *
 * @param <R>
 */
@Plugin(type = Command.class, menuPath = "Plugins>BigDataProcessor2>" + AbstractOpenCommand.COMMAND_OPEN_PATH + OpenAdvancedCommand.COMMAND_FULL_NAME )
public class OpenAdvancedCommand< R extends RealType< R > & NativeType< R > > extends AbstractOpenCommand< R >
{
    public static final String COMMAND_NAME = "Open Advanced...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP_PREFIX + COMMAND_NAME;

    @Parameter(label = "Subset file using regular expression" )
    String filterPattern = ".*";

    //@Parameter(label = "Regular expression help", callback = "showRegExpHelp", required = false)
    //Button regExpHelpButton;

    @Parameter(label = "Image naming scheme",
            choices = {
                    NamingScheme.SINGLE_CHANNEL_TIMELAPSE,
                    NamingScheme.LEICA_LIGHT_SHEET_TIFF,
                    NamingScheme.LOAD_CHANNELS_FROM_FOLDERS,
                    NamingScheme.TIFF_SLICES,
                    NamingScheme.PATTERN_1,
                    NamingScheme.PATTERN_2,
                    NamingScheme.PATTERN_3,
                    NamingScheme.PATTERN_4,
                    NamingScheme.PATTERN_5,
                    NamingScheme.PATTERN_6})
    String namingScheme = NamingScheme.SINGLE_CHANNEL_TIMELAPSE;

    @Parameter(label = "Hdf5 data set name (optional)", required = false)
    String hdf5DataSetName = "Data";

    public void run()
    {
        SwingUtilities.invokeLater( () ->  {
            outputImage = BigDataProcessor2.openImage(
                    directory.toString(),
                    namingScheme,
                    filterPattern );

            fixVoxelSpacing( outputImage );

            handleOutputImage( autoContrast, false );
        });
    }

    private void fixVoxelSpacing( Image< R > image )
    {
        // Sometimes Leica is calibrated as cm, which makes no sense
        final double[] voxelSpacing = image.getVoxelSpacing();
        final String voxelUnit = CalibrationUtils.fixVoxelSpacingAndUnit( voxelSpacing, image.getVoxelUnit() );
        image.setVoxelSpacing( voxelSpacing );
        image.setVoxelUnit( voxelUnit );
    }
}
