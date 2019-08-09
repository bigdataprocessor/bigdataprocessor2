package tests;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.process.splitviewmerge.SplitViewMerger;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.utils.DimensionOrder;
import loci.common.DebugTools;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;

public class TestRegionMerging
{
	public static boolean showImages = false;

	@Test
	public < R extends RealType< R > & NativeType< R > > void mergeTwoRegionsFromOneChannel()
	{
		DebugTools.setRootLevel("OFF"); // Bio-Formats

		final Image< R > image = BigDataProcessor2.openImage(
				"/Users/tischer/Documents/fiji-plugin-bigDataTools2/src/test/resources/test-data/region-merging/one-channel",
				FileInfos.SINGLE_CHANNEL_TIMELAPSE,
				".*" );

		if ( showImages )
			BigDataProcessor2.showImage( image );

		final SplitViewMerger merger = new SplitViewMerger();
		final int sizeXY = 100;
		merger.addIntervalXYC( 86, 5, sizeXY, sizeXY, 0 );
		merger.addIntervalXYC( 1, 65, sizeXY, sizeXY, 0 );

		final Image< R > merged = merger.mergeIntervalsXYC( image );
		merged.setName( "two-channels" );

		assertTrue( merged.getRai().min( DimensionOrder.C ) == 0 );
		assertTrue( merged.getRai().max( DimensionOrder.C ) == 1 );
		assertTrue( merged.getRai().dimension( DimensionOrder.X ) == sizeXY );

		System.out.println("Done.");
	}

	@Test
	public < R extends RealType< R > & NativeType< R > > void mergeThreeRegionsFromTwoChannels()
	{
		DebugTools.setRootLevel("OFF"); // Bio-Formats

		final BigDataProcessor2< R > bdp = new BigDataProcessor2<>();

		final Image< R > image = bdp.openImage(
				"/Users/tischer/Documents/fiji-plugin-bigDataTools2/src/test/resources/test-data/region-merging/two-channel",
				FileInfos.PATTERN_2,
				".*" );

		if ( showImages )
			BigDataProcessor2.showImage( image );


		final SplitViewMerger merger = new SplitViewMerger();
		final int sizeXY = 100;
		merger.addIntervalXYC( 131, 30, sizeXY, sizeXY, 0 );
		merger.addIntervalXYC( 12, 110, sizeXY, sizeXY, 0 );
		merger.addIntervalXYC( 131, 30, sizeXY, sizeXY, 1 );

		final Image< R > merged = merger.mergeIntervalsXYC( image );
		merged.setName( "three-channels" );

		if ( showImages )
			bdp.showImage( merged );

		assertTrue( merged.getRai().min( DimensionOrder.C ) == 0 );
		assertTrue( merged.getRai().max( DimensionOrder.C ) == 2 );
		assertTrue( merged.getRai().dimension( DimensionOrder.X ) == sizeXY );

		System.out.println("Done.");

	}


	public static void main( String[] args )
	{
		showImages = true;
//		new TestRegionMerging().mergeTwoRegionsFromOneChannel();
		new TestRegionMerging().mergeThreeRegionsFromTwoChannels();
	}
}
