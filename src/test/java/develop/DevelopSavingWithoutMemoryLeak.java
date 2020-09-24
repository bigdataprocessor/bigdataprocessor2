package develop;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.progress.LoggingProgressListener;
import de.embl.cba.bdp2.save.SavingSettings;
import net.imagej.ImageJ;

import static de.embl.cba.bdp2.open.core.NamingSchemes.LUXENDO_REGEXP;

public class DevelopSavingWithoutMemoryLeak
{
	public static void main( String[] args )
	{
		final ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();

		String regExp = LUXENDO_REGEXP.replace( "STACK", "" + 6 );

		// /Volumes/cba/exchange/bigdataprocessor/data/mouse_2cam_publication_figure/stack_6_channel_2
		final Image image = BigDataProcessor2.openImageFromHdf5(
				"/Volumes/cba/exchange/bigdataprocessor/data/mouse_2cam_publication_figure",
				regExp,
				regExp,
				"Data" );

		BigDataProcessor2.showImage( image );

//		final SavingSettings savingSettings = SavingSettings.getDefaults();
//		savingSettings.saveFileType = SavingSettings.SaveFileType.TIFF_VOLUMES;
//		savingSettings.numIOThreads = 1;
//		savingSettings.saveProjections = false;
//		savingSettings.saveVolumes = true;
//		savingSettings.volumesFilePathStump = "/Volumes/cba/exchange/bigdataprocessor/data/tmp/volumes-";
//
//		BigDataProcessor2.saveImage( image, savingSettings, new LoggingProgressListener( "Files saved" ) );
	}
}
