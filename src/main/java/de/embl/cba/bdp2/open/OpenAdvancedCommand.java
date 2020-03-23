package de.embl.cba.bdp2.open;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.files.FileInfos;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.dialog.HelpDialog;
import loci.common.DebugTools;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.swing.*;
import java.io.File;

import static de.embl.cba.bdp2.utils.Utils.COMMAND_PREFIX;


/**
 * TODO: How to add a HELP button for the regular expression without screwing up the macro recording?
 *
 *
 * @param <R>
 */
@Plugin(type = Command.class, menuPath = "Plugins>BigDataProcessor2>Open>" + OpenAdvancedCommand.COMMAND_NAME )
public class OpenAdvancedCommand< R extends RealType< R > & NativeType< R > > implements Command {

    public static final String COMMAND_NAME = COMMAND_PREFIX + "Open Advanced...";
    @Parameter(label = "Image data directory", style = "directory")
    File directory;

    @Parameter(label = "Subset files using regular expression" )
    String filterPattern = ".*";

    //@Parameter(label = "Regular expression help", callback = "showRegExpHelp", required = false)
    //Button regExpHelpButton;

    @Parameter(label = "Image naming scheme",
            choices = {
                    FileInfos.SINGLE_CHANNEL_TIMELAPSE,
                    FileInfos.LEICA_LIGHT_SHEET_TIFF,
                    FileInfos.LOAD_CHANNELS_FROM_FOLDERS,
                    FileInfos.TIFF_SLICES,
                    FileInfos.PATTERN_1,
                    FileInfos.PATTERN_2,
                    FileInfos.PATTERN_3,
                    FileInfos.PATTERN_4,
                    FileInfos.PATTERN_5,
                    FileInfos.PATTERN_6})
    String namingScheme = FileInfos.SINGLE_CHANNEL_TIMELAPSE;

    @Parameter(label = "Hdf5 data set name (optional)", required = false)
    String hdf5DataSetName = "Data";

    //@Parameter(label = "Auto contrast")
    boolean autoContrast = true;

    public void run()
    {
        SwingUtilities.invokeLater( () ->  {
            DebugTools.setRootLevel( "OFF" ); // Bio-Formats

            final Image< R > image =
                    BigDataProcessor2.openImage(
                            directory.toString(),
                            namingScheme,
                            filterPattern );

            BigDataProcessor2.showImage( image, autoContrast );
        });
    }

    public void showRegExpHelp()
    {
        SwingUtilities.invokeLater( () -> {
            final HelpDialog helpDialog = new HelpDialog( null,
                    OpenAdvancedCommand.class.getResource( "/RegExpHelp.html" ) );
            helpDialog.setVisible( true );
        } );
    }
}
