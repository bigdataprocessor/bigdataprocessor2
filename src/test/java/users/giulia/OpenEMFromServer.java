package users.giulia;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.load.files.FileInfos;
import de.embl.cba.bdp2.BigDataProcessor2;
import net.imagej.ImageJ;
import net.imglib2.FinalInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class OpenEMFromServer
{

	public < R extends RealType< R > & NativeType< R > > void view()
	{
		new ImageJ().ui().showUI();

		final BigDataProcessor2< R > bdp = new BigDataProcessor2<>();

		final Image< R > image = bdp.openImage(
				"/Volumes/emcf/Mizzon/projects/Julian_FIBSEM/fib-SEM/20190730_batch6-blockB-prep/20190730_02UA_01GA_cell1",
				FileInfos.TIFF_SLICES,
				".*.tif" );

		final Image< R > crop = BigDataProcessor2.crop( image, new FinalInterval(
				new long[]{ 1500, 3600, 880, 0, 0 },
				new long[]{ 4050, 4800, 950, 0, 0 }
		) );

		final Image< R > bin = BigDataProcessor2.bin( crop, new long[]{ 3, 3, 1, 1, 1 } );

		bdp.showImage( bin );

		System.out.println("Done.");
	}


	public static void main( String[] args )
	{
		new OpenEMFromServer().view();
	}
}
