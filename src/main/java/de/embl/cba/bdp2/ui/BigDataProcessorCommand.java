package de.embl.cba.bdp2.ui;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;

@Plugin(type = Command.class, menuPath = "Plugins>BigDataTools>BigDataProcessor2", initializer = "init")
public class BigDataProcessorCommand < R extends RealType< R > & NativeType< R >>
        implements Command {

    @Parameter(label = "Image data directory", style = "directory")
    File directory;

    @Parameter(label = "Subset files using regular expression",
            choices = {
                    ".*",
                    ".*--C.*",
                    ".*Left.*",
                    ".*Right.*",
                    ".*short.*",
                    ".*long.*",
                    ".*Target.*",
                    ".*LSEA00.*",
                    ".*LSEA01.*"})
    String filterPattern = ".*";

    @Parameter(label = "Image files scheme",
            choices = {
                    FileInfos.SINGLE_CHANNEL_TIMELAPSE,
                    FileInfos.LEICA_SINGLE_TIFF,
                    FileInfos.LOAD_CHANNELS_FROM_FOLDERS,
                    FileInfos.EM_TIFF_SLICES,
                    FileInfos.PATTERN_1,
                    FileInfos.PATTERN_2,
                    FileInfos.PATTERN_3,
                    FileInfos.PATTERN_4,
                    FileInfos.PATTERN_5,
                    FileInfos.PATTERN_6})
    String namingScheme = FileInfos.SINGLE_CHANNEL_TIMELAPSE;

    public void run()
    {
        final BigDataProcessor2< R > bdp = new BigDataProcessor2< >();

        final Image< R > image =
                bdp.openImage( directory.toString(), namingScheme, filterPattern );

        bdp.showVoxelSpacingDialog( image );
        bdp.showImage( image );

    }

}
