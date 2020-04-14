package publication;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.files.FileInfos;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imagej.ImageJ;
import net.imglib2.FinalInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.util.ArrayList;

public class FigureCreator
{
	public static < R extends RealType< R > & NativeType< R > > void main( String[] args )
	{
		final ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();

		final Image image = BigDataProcessor2.openHdf5Image( "/Volumes/USB Drive/tim2020/luxendo-two-channel-movie",
				FileInfos.LOAD_CHANNELS_FROM_FOLDERS,
				FileInfos.PATTERN_LUXENDO,
				"Data"
		);
		image.setName( "raw" );
		BigDataProcessor2.calibrate( image,  new double[]{0.3, 0.3, 1.0}, image.getVoxelUnit() );
		BigDataProcessor2.showImage( image);

		// crop
		//
		final Image crop = BigDataProcessor2.crop( image, new FinalInterval( new long[]{ 477, 487, 31, 0, 0 }, new long[]{ 1567, 1491, 143, 1, 1 } ) );

		// correct chromatic shift
		//
		final ArrayList< long[] > shifts = new ArrayList<>();
		shifts.add( new long[]{0,0,0,0} );
		shifts.add( new long[]{30,-10,0,0} );
		final Image shift = BigDataProcessor2.correctChromaticShift( crop, shifts );

		// bin
		//
		final Image bin = BigDataProcessor2.bin( shift, new long[]{ 3, 3, 1, 1, 1 } );

		// show processed
		//
		bin.setName( "processed" );
		final BdvImageViewer viewer = BigDataProcessor2.showImage( bin, true );
		viewer.setDisplayRange( 100, 300, 0);

		/**
		 * run("BDP Crop...", "inputimage=[dfgdf] outputimagename=[dfgdf-crop] viewingmodality=[Show image in new viewer] minx=477 miny=487 minz=31 minc=0 mint=0 maxx=1567 maxy=1491 maxz=143 maxc=1 maxt=1 ");
		 * run("BDP Correct Chromatic Shift...", "inputimage=[dfgdf-crop] outputimagename=[dfgdf-crop] viewingmodality=[Replace image in current viewer] shifts=[0,0,0,0;30,-10,0,0] ");
		 * run("BDP Bin...", "inputimage=[dfgdf-crop] outputimagename=[dfgdf-crop-binned] viewingmodality=[Replace image in current viewer] binwidthxpixels=3 binwidthypixels=3 binwidthzpixels=3 ");
		 * display settings: 100, 300; 100, 1100
		 */
	}

}
