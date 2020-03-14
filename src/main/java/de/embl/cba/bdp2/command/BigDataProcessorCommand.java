package de.embl.cba.bdp2.command;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.ui.HelpDialog;
import loci.common.DebugTools;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.Button;

import javax.swing.*;
import java.io.File;
import java.util.Arrays;

import static de.embl.cba.bdp2.loading.files.FileInfos.PATTERN_LUXENDO;

@Plugin(type = Command.class, menuPath = "Plugins>BigDataTools>BigDataProcessor2", initializer = "init")
public class BigDataProcessorCommand < R extends RealType< R > & NativeType< R > > implements Command {

    @Parameter(label = "Image data directory", style = "directory")
    File directory;

    @Parameter(label = "Subset files using regular expression")
    String filterPattern = ".*";

    @Parameter(label = "Regular expression help", callback = "showRegExpHelp")
    Button regExpHelpButton;

    @Parameter(label = "Image files scheme",
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

    @Parameter(label = "Auto contrast")
    boolean autoContrast = false;

    public void run()
    {
        DebugTools.setRootLevel( "OFF" ); // Bio-Formats

        final Image< R > image =
                BigDataProcessor2.openImage(
                        directory.toString(),
                        namingScheme,
                        filterPattern );

        BigDataProcessor2.showVoxelSpacingDialog( image );
        Logger.info( "Image voxel unit: " + image.getVoxelUnit() );
        Logger.info( "Image voxel size: " + Arrays.toString( image.getVoxelSpacing() ) );

        SwingUtilities.invokeLater( () ->  {
            BigDataProcessor2.showImage( image, autoContrast );
        });
    }

    public void showRegExpHelp()
    {
        SwingUtilities.invokeLater( () -> {
            final HelpDialog helpDialog = new HelpDialog( null,
                    BigDataProcessorCommand.class.getResource( "/RegExpHelp.html" ) );
            helpDialog.setVisible( true );
        } );

    }

}
