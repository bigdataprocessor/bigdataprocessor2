package develop;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.utils.RAISlicer;
import net.imagej.ImageJ;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.view.Views;

import static de.embl.cba.bdp2.open.NamingSchemes.LUXENDO;

public class DevelopSavingWithoutMemoryLeak
{
	public static void main( String[] args )
	{
		final ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();

		String regExp = LUXENDO.replace( "STACK", "" + 6 );

		// /Volumes/cba/exchange/bigdataprocessor/data/mouse_2cam_publication_figure/stack_6_channel_2
		final Image image = BigDataProcessor2.openHDF5Series(
				"/Volumes/cba/exchange/bigdataprocessor/data/mouse_2cam_publication_figure",
				regExp,
				"Data" );

		RandomAccessibleInterval volumeView = Views.dropSingletonDimensions(  RAISlicer.getVolumeView( image.getRai(), 0, 10 ) );

		// TODO: this method always accesses the first image! remove this!
		// create RandomAccess
		final RandomAccess< ? > randomAccess = volumeView.randomAccess( );

		// place it at the first pixel

		// volumeView.min( randomAccess );

		//Util.getTypeFromInterval(  )

		//Object o = randomAccess.get();

//		BigDataProcessor2.showImage( image );
//
//		final SavingSettings savingSettings = SavingSettings.getDefaults();
//		savingSettings.saveFileType = SavingSettings.SaveFileType.Tiff_VOLUMES;
//		savingSettings.numIOThreads = 1;
//		savingSettings.saveProjections = false;
//		savingSettings.saveVolumes = true;
//		savingSettings.volumesFilePathStump = "/Volumes/cba/exchange/bigdataprocessor/data/tmp/volumes-";
//
//		BigDataProcessor2.saveImage( image, savingSettings, new LoggingProgressListener( "Files saved" ) );
	}
}
