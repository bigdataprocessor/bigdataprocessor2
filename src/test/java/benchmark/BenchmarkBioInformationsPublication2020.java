package benchmark;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.BigDataProcessor2Command;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.save.SaveFileType;
import de.embl.cba.bdp2.save.SavingSettings;
import net.imagej.ImageJ;

public class BenchmarkBioInformationsPublication2020
{
	public static void main( String[] args )
	{
		ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();
		imageJ.command().run( BigDataProcessor2Command.class, true );
		Logger.setLevel( Logger.Level.Benchmark );

		String root = "/Users/tischer/Desktop/bpd2-benchmark/h5";
//		String root = "/Users/tischer/Desktop/bpd2-benchmark/tif";

//		String root = "/Volumes/cba/exchange/bigdataprocessor/data/benchmark";

		Image image = BigDataProcessor2.openHDF5Series( root + "/in",".*stack_6_(?<C1>channel_.*)/(?<C2>Cam_.*)_(?<T>\\d+).h5","Data" );

//		Image image = BigDataProcessor2.openTiffSeries( root + "/in", "(?<T>.*).tif" );
//
		//image = BigDataProcessor2.bin( image, new long[]{3,3,1,1,1} );
//
		BigDataProcessor2.showImage( image );

//		SavingSettings savingSettings = new SavingSettings();
//		savingSettings.volumesFilePathStump = root + "/out/volumes";
//		savingSettings.numIOThreads = 1;
//		savingSettings.numProcessingThreads = 4;
//		savingSettings.fileType = SaveFileType.TiffVolumes;
//		savingSettings.saveProjections = false;
//		savingSettings.saveVolumes = true;
//		savingSettings.compression = savingSettings.COMPRESSION_NONE;
//		savingSettings.tStart = 0;
//		savingSettings.tEnd = 9;
//		BigDataProcessor2.saveImageAndWaitUntilDone( image, savingSettings );
	}
}
