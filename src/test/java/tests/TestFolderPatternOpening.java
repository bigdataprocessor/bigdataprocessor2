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

public class TestFolderPatternOpening
{
	@Test
	public < R extends RealType< R > & NativeType< R > > void openSubfoldersWithFolderPattern()
	{

		final BigDataProcessor2< R > bdp = new BigDataProcessor2<>();

		final Image< R > image = bdp.openImage(
				"/Volumes/cba/exchange/Isabell_Schneider/3-Color",
				FileInfos.LOAD_CHANNELS_FROM_FOLDERS,
				"stack_10_.*/.*" );

		bdp.showImage( image );
	}


	public static void main( String[] args )
	{
//		new TestRegionMerging().mergeTwoRegionsFromOneChannel();
		new TestFolderPatternOpening().openSubfoldersWithFolderPattern();
	}
}
