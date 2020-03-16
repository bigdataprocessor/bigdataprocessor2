package users.giulia;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.load.files.FileInfos;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import net.imagej.ImageJ;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class OpenEMFromServer01
{

	public < R extends RealType< R > & NativeType< R > > void open()
	{
		new ImageJ().ui().showUI();

		final Image< R > image = BigDataProcessor2.openImage(
				"/Volumes/emcf/Mizzon/projects/Klaske_FIBSEM/FIB-SEM-target2/20190506-Spongilla_cell2/19-05-06_Spongilla_cell2_3/ImageProcessingSteps/ROIs/ROI3__neighbouringCells",
				FileInfos.TIFF_SLICES,
				".*.tif" );

		BigDataProcessor2.showImage( image, false );


//		final Image< R > crop = BigDataProcessor2.crop( image, new FinalInterval(
//				new long[]{ 1500, 3600, 880, 0, 0 },
//				new long[]{ 4050, 4800, 950, 0, 0 }
//		) );
//
//		final Image< R > bin = BigDataProcessor2.bin( crop, new long[]{ 1, 1, 0, 0, 0 } );

		System.out.println("Done.");
	}


	public static void main( String[] args )
	{
		new OpenEMFromServer01().open();
	}
}
