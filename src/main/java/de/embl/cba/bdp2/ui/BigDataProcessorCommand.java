package de.embl.cba.bdp2.ui;

import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.logging.IJLazySwingLogger;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.imaris.ImarisUtils;
import de.embl.cba.bdp2.viewers.ViewerUtils;
import net.imagej.DatasetService;
import net.imagej.ops.OpService;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;

import java.io.File;

@Plugin(type = Command.class, menuPath = "Plugins>BigDataTools>BigDataProcessor2", initializer = "init")
public class BigDataProcessorCommand<T extends RealType<T> & NativeType<T>> implements Command {
    @Parameter
    public static UIService uiService;

    @Parameter
    public DatasetService datasetService;

    @Parameter
    public LogService logService;

    @Parameter
    public OpService opService;

    @Parameter
    public StatusService statusService;

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

    @Parameter(label = "Image viewer",
            choices = {
                    ViewerUtils.BIG_DATA_VIEWER
                    //ViewerUtils.IJ1_VIEWER
            })
    String imageViewerChoice = ViewerUtils.BIG_DATA_VIEWER;

    @Parameter(label = "Auto Contrast")
    boolean autoContrast = true;

    private static final BigDataProcessor2 BIG_DATA_CONVERTER = new BigDataProcessor2();
    public static final Logger logger = new IJLazySwingLogger();

    public void run()
    {
        BIG_DATA_CONVERTER.openFromDirectory(
                directory.toString(),
                namingScheme,
                filterPattern,
                autoContrast,
                ViewerUtils.getImageViewer( imageViewerChoice ));
    }

}
