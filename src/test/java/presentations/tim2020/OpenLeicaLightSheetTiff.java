package presentations.tim2020;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.load.files.FileInfos;
import de.embl.cba.bdp2.ui.BigDataProcessor2;

public class OpenLeicaLightSheetTiff
{
	public static void main( String[] args )
	{
		final Image image = BigDataProcessor2.openImage( "/g/cba/exchange/bigdataprocessor/data/tim2020/leica-light-sheet-tiff-planes",
				FileInfos.LEICA_LIGHT_SHEET_TIFF,
				".*"
		);

		// The image calibration is read in cm.
		// I do not know why, we fix it here to micrometer.
		final double[] voxelSpacing = image.getVoxelSpacing();
		final String voxelUnit = BigDataProcessor2.fixVoxelSpacingAndUnit( voxelSpacing, image.getVoxelUnit() );
		voxelSpacing[ 2 ] = 3.7;
		image.setVoxelSpacing( voxelSpacing );
		image.setVoxelUnit( voxelUnit );

		BigDataProcessor2.showImage( image );
	}
}
