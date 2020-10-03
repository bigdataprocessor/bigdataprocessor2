package presentations.tim2020;

import de.embl.cba.bdp2.process.calibrate.CalibrationUtils;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.open.core.NamingSchemes;

public class OpenLeicaLightSheetTiff
{
	public static void main( String[] args )
	{
		final Image image = BigDataProcessor2.openImage( "/g/cba/exchange/bigdataprocessor/data/tim2020/leica-light-sheet-tiff-planes",
				NamingSchemes.LEICA_DSL_TIFF_PLANES_REG_EXP,
				".*"
		);

		// The image calibration is core in cm.
		// I do not know why, we fix it here to micrometer.
		final double[] voxelSpacing = image.getVoxelSize();
		final String voxelUnit = CalibrationUtils.fixVoxelSizeAndUnit( voxelSpacing, image.getVoxelUnit() );
		voxelSpacing[ 2 ] = voxelSpacing[ 1 ];
		image.setVoxelSize( voxelSpacing );
		image.setVoxelUnit( voxelUnit );


		BigDataProcessor2.showImage( image);
	}
}
