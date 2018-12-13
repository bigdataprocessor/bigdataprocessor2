package de.embl.cba.bigDataTools2.dataStreamingGUI;

import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoConstants;
import de.embl.cba.bigDataTools2.imaris.ImarisUtils;
import de.embl.cba.bigDataTools2.viewers.ViewerUtils;
import net.imagej.DatasetService;
import net.imagej.ops.OpService;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.command.DynamicCommand;
import org.scijava.command.Interactive;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;
import org.scijava.widget.Button;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Plugin(type = Command.class, menuPath = "Plugins>BigDataTools>Lazy Loading", initializer = "init")
public class LazyLoadingCommand<T extends RealType<T> & NativeType< T > > extends DynamicCommand implements Interactive
{
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

	@Parameter( label = "Image data directory", style = "directory" )
	File directory = new File( "" );

	@Parameter ( label = "Subset files using regular expression",
			choices = {
			".*",
			".*--C.*",
			".*Left.*",
			".*Right.*",
			".*short.*",
			".*long.*",
			".*Target.*",
			".*LSEA00.*",
			".*LSEA01.*"} )
	String filterPattern = ".*";

	@Parameter ( label = "Image files scheme",
			choices = {
			FileInfoConstants.SINGLE_CHANNEL_TIMELAPSE,
			FileInfoConstants.LEICA_SINGLE_TIFF,
			FileInfoConstants.LOAD_CHANNELS_FROM_FOLDERS,
			FileInfoConstants.EM_TIFF_SLICES,
			FileInfoConstants.PATTERN_1,
			FileInfoConstants.PATTERN_2,
			FileInfoConstants.PATTERN_3,
			FileInfoConstants.PATTERN_4,
			FileInfoConstants.PATTERN_5,
			FileInfoConstants.PATTERN_6 })
	String namingScheme = FileInfoConstants.SINGLE_CHANNEL_TIMELAPSE;

	@Parameter ( label = "Hdf5 data set name",
			choices = {
			"None",
			"Data",
			"Data111",
			ImarisUtils.RESOLUTION_LEVEL + "0/Data",
			ImarisUtils.RESOLUTION_LEVEL + "1/Data",
			ImarisUtils.RESOLUTION_LEVEL + "2/Data",
			ImarisUtils.RESOLUTION_LEVEL + "3/Data",
			"ITKImage/0/VoxelData",
			"Data222",
			"Data444"})
	String hdf5DataSet = "None";

	@Parameter ( label = "Image viewer",
			choices = {
			ViewerUtils.BIG_DATA_VIEWER
			//ViewerUtils.IMAGE_HYPERSTACK_VIEWER
			})
	String imageViewerChoice = ViewerUtils.BIG_DATA_VIEWER;


	@Parameter(label = "Lazy load", callback = "lazyLoad")
	private Button lazyLoadButton;


	private static final DataStreamingTools dataStreamingTools = new DataStreamingTools();
	private ExecutorService uiActionThreadPool;


	public void init()
	{
		this.uiActionThreadPool = Executors.newFixedThreadPool(4); //TODO --ashis
	}

	private void lazyLoad()
	{
		uiActionThreadPool.submit(() -> dataStreamingTools.openFromDirectory(
				directory.toString(),
				namingScheme,
				filterPattern,
				hdf5DataSet,
				ViewerUtils.getImageViewer( imageViewerChoice )
		));

	}

}
