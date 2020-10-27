package develop;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.BigDataProcessor2Command;
import de.embl.cba.bdp2.image.Image;
import net.imagej.ImageJ;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class DevelopBioFormatsOpening
{
	public static < R extends RealType< R > & NativeType< R > >  void main( String[] args )
	{
		// Configure all services
		ImageJ imageJ = new ImageJ();
		imageJ.command().run( BigDataProcessor2Command.class, true );

		String filePath = "/Volumes/cba/exchange/bigdataprocessor/data/czi/20180125CAGtdtomato_ERT2CreLuVeLu_notamox_03_Average_Subset.czi";
		int series = 0;
		Image< R > image = BigDataProcessor2.openBioFormats( filePath, series );
		BigDataProcessor2.showImage( image );
	}
}
