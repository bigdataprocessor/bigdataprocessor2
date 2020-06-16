package de.embl.cba.bdp2.open;

import de.embl.cba.bdp2.calibrate.CalibrationUtils;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.read.NamingScheme;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.swing.*;

import static de.embl.cba.bdp2.read.NamingScheme.*;
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
    public static final String COMMAND_NAME = "Open...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP_PREFIX + COMMAND_NAME;
    public static final String USE_CUSTOM = "Use custom";

    //@Parameter(label = "Subset file using regular expression" )
    //String filterPattern = ".*";

    //@Parameter(label = "Regular expression help", callback = "showRegExpHelp", required = false)
    //Button regExpHelpButton;

    @Parameter(label = "Regular expression templates",
            choices = {
                    USE_CUSTOM,
                    MULTI_CHANNEL_TIFF_VOLUMES_FROM_SUBFOLDERS,
                    MULTI_CHANNEL_OME_TIFF_VOLUMES,
                    MULTI_CHANNEL_TIFF_VOLUMES })
    String regExpTemplate = NamingScheme.SINGLE_CHANNEL_TIMELAPSE;

    @Parameter(label = "Custom regular expression")
    String regExp = MULTI_CHANNEL_TIFF_VOLUMES_FROM_SUBFOLDERS;

    @Parameter(label = "Hdf5 data set name (optional)", required = false)
    String hdf5DataSetName = "Data";

    public void run()
    {
        SwingUtilities.invokeLater( () ->  {

            if ( ! regExpTemplate.equals( USE_CUSTOM ) )
            {
                regExp = regExpTemplate;
            }

            if ( regExp.endsWith( ".h5" ) )
            {
                outputImage = BigDataProcessor2.openImageFromHdf5(
                        directory.toString(),
                        regExp,
                        ".*",
                        hdf5DataSetName );
            }
            else if ( regExp.contains( ".tif" ) ) // .tiff .ome.tif
            {
                outputImage = BigDataProcessor2.openImage(
                        directory.toString(),
                        regExp,
                        ".*" );
            }

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
