package benchmark;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.save.SaveFileType;
import de.embl.cba.bdp2.save.SavingSettings;
import ij.IJ;
import org.renjin.gnur.api.R;

public class BenchmarkPublication
{
	public static void main( String[] args )
	{

		Image image = BigDataProcessor2.openHdf5Series(
				"/Users/tischer/Downloads/tmp-luxendo",
				".*stack_6_(?<C1>channel_.*)/(?<C2>Cam_.*)_(?<T>\\d+).h5",
				"Data" );
		image = BigDataProcessor2.bin( image, new long[]{3,3,1,1,1} );

		SavingSettings savingSettings = new SavingSettings();
		savingSettings.volumesFilePathStump = "/Users/tischer/Downloads/tmp-luxendo-bdp2-out//volumes/tmp-luxendo-binned";
		savingSettings.projectionsFilePathStump = "/Users/tischer/Downloads/tmp-luxendo-bdp2-out//projections/tmp-luxendo-binned";
		savingSettings.numIOThreads = 1;
		savingSettings.numProcessingThreads = 4;
		savingSettings.fileType = SaveFileType.TiffVolumes;
		savingSettings.saveProjections = true;
		savingSettings.saveVolumes = true;
		savingSettings.compression = "None";
		savingSettings.tStart = 0;
		savingSettings.tEnd = 1;
		BigDataProcessor2.saveImageAndWaitUntilDone( image, savingSettings );
		IJ.run("BDP2 Set Logging Level...", "level=Benchmark");
	}
}
