package test;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.open.core.NamingSchemes;
import loci.common.DebugTools;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import static junit.framework.Assert.assertTrue;

public class TestFolderPatternOpening
{
	// TODO: make a proper test
	public < R extends RealType< R > & NativeType< R > > void openSubfoldersWithFolderPattern()
	{
		DebugTools.setRootLevel("OFF"); // Bio-Formats

		final Image< R > image = BigDataProcessor2.openImage(
				"/Volumes/cba/exchange/Isabell_Schneider/3-Color",
				NamingSchemes.LOAD_CHANNELS_FROM_FOLDERS,
				"stack_10_.*/.*" );

		// bdp.showImage( image );
	}


	public static void main( String[] args )
	{
//		new TestRegionMerging().mergeTwoRegionsFromOneChannel();
		new TestFolderPatternOpening().openSubfoldersWithFolderPattern();
	}
}
