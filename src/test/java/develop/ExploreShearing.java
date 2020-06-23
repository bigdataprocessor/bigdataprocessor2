package develop;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.core.NamingSchemes;
import de.embl.cba.bdp2.shear.ImageShearer;
import de.embl.cba.bdp2.shear.ShearMenuDialog;
import de.embl.cba.bdp2.shear.ShearingSettings;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class ExploreShearing < R extends RealType< R > & NativeType< R > >
{
	public static < R extends RealType< R > & NativeType< R > > void main( String[] args )
	{
		final Image< R > image = BigDataProcessor2.openImage(
				"src/test/resources/shear_transform_test",
				NamingSchemes.SINGLE_CHANNEL_TIMELAPSE,
				".*");

		final BdvImageViewer bdvImageViewer = BigDataProcessor2.showImage( image);

		final ShearingSettings shearingSettings = new ShearingSettings();
		ShearMenuDialog dialog = new ShearMenuDialog( bdvImageViewer );
		dialog.getShearingSettings( shearingSettings ); // sets default values.

		final RandomAccessibleInterval shearRaiXYZCT = ImageShearer.shearRai5D( image.getRai(), shearingSettings );
		final Image shearImage = image.newImage( shearRaiXYZCT );
		bdvImageViewer.replaceImage( shearImage, false, true );
	}
}
