package tests;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.process.splitviewmerge.SplitViewMerger;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.utils.DimensionOrder;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;

public class TestRegionMerging
{
	@Test
	public < R extends RealType< R > & NativeType< R > > void mergeTwoRegionsFromSingleChannel()
	{

		final BigDataProcessor2< R > bdp = new BigDataProcessor2<>();

		final Image< R > image = bdp.openImage(
				"/Users/tischer/Documents/fiji-plugin-bigDataTools2/src/test/resources/test-data/region-merging/one-channel",
				FileInfos.SINGLE_CHANNEL_TIMELAPSE,
				".*" );

		bdp.showImage( image );

		final SplitViewMerger merger = new SplitViewMerger();
		final int sizeXY = 100;
		merger.addIntervalXYC( 86, 5, sizeXY, sizeXY, 0 );
		merger.addIntervalXYC( 1, 65, sizeXY, sizeXY, 0 );

		final Image< R > merged = merger.mergeIntervalXYC( image );

		assertTrue( merged.getRai().min( DimensionOrder.C ) == 0 );
		assertTrue( merged.getRai().max( DimensionOrder.C ) == 1 );
		assertTrue( merged.getRai().dimension( DimensionOrder.X ) == sizeXY );
	}

	public static void main( String[] args )
	{
		new TestRegionMerging().mergeTwoRegionsFromSingleChannel();
	}
}
