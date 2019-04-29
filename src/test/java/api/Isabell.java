package api;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.CachedCellImgReader;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.process.splitviewmerge.RegionOptimiser;
import de.embl.cba.bdp2.process.splitviewmerge.SplitImageMerger;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.viewers.ImageViewer;
import de.embl.cba.bdp2.viewers.ViewerUtils;
import net.imagej.ImageJ;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.util.ArrayList;

/**
 * IMPORTANT NOTE: Adjust Max value to 255 in the Big Data Viewer. (Settings>Brightness and Color>Max)
 */

public class Isabell
{
    public static < R extends RealType< R > & NativeType< R > > void main( String[] args)
    {
//        final ImageJ imageJ = new ImageJ();
//        imageJ.ui().showUI();

        final BigDataProcessor2< R > bdp = new BigDataProcessor2<>();

        final Image< R > image = bdp.openHdf5Data(
                "/Users/tischer/Desktop/stack_0_channel_0",
                FileInfos.SINGLE_CHANNEL_TIMELAPSE,
                ".*.h5",
                "Data" );

        // TODO:
        // image.setVoxelUnit( );
        // image.setVoxelSpacing(  );

        final ArrayList< long[] > centres = new ArrayList<>();
        centres.add( new long[]{ 522, 1143 } );
        centres.add( new long[]{ 1396, 546 } );
        final long[] spans = { 900 , 900 };

        final ArrayList< long[] > optimisedCentres =
                RegionOptimiser.optimiseCentres2D(
                        image,
                        centres,
                        spans );

        final Image< R > merge = SplitImageMerger.merge( image, optimisedCentres, spans );

        bdp.showImage( merge );

    }

}
