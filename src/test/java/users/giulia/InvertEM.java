package users.giulia;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.process.splitviewmerge.SplitViewMerger;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.utils.DimensionOrder;
import net.imagej.ImageJ;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.junit.Test;

import static junit.framework.Assert.assertTrue;

public class InvertEM
{

	public < R extends RealType< R > & NativeType< R > > void invertEM()
	{

		final BigDataProcessor2< R > bdp = new BigDataProcessor2<>();

		final Image< R > image = bdp.openImage(
				"/Volumes/emcf/Mizzon/projects/Julian_FIBSEM/fib-SEM/20190730_batch6-blockB-prep/20190730_02UA_01GA_cell1",
				FileInfos.EM_TIFF_SLICES,
				".*.tif" );

		bdp.showImage( image );

		System.out.println("Done.");
	}


	public static void main( String[] args )
	{
		new ImageJ().ui().showUI();
		new InvertEM().invertEM();
	}
}
