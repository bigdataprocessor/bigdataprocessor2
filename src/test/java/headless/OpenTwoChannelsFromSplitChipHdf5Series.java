package headless;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.CachedCellImgReader;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.process.splitviewmerge.RegionMerger;
import de.embl.cba.bdp2.process.splitviewmerge.RegionOptimiser;
import de.embl.cba.bdp2.viewers.ImageViewer;
import de.embl.cba.bdp2.viewers.ViewerUtils;
import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.util.ArrayList;

/**
 * IMPORTANT NOTE: Adjust Max value to 255 in the Big Data Viewer. (Settings>Brightness and Color>Max)
 */

public class OpenTwoChannelsFromSplitChipHdf5Series
{
    public static < R extends RealType< R > & NativeType< R > > void main( String[] args)
    {
        final ImageJ imageJ = new ImageJ();
        imageJ.ui().showUI();

        // BigDataProcessor2 bigDataProcessor2 = new BigDataProcessor2();
        final ImageViewer viewer = ViewerUtils
                .getImageViewer( ViewerUtils.BIG_DATA_VIEWER );

        String imageDirectory = "/Users/tischer/Desktop/stack_0_channel_0";

        final FileInfos fileInfos = new FileInfos( imageDirectory,
                FileInfos.SINGLE_CHANNEL_TIMELAPSE,
                ".*.h5", "Data" );

        fileInfos.voxelSpacing = new double[]{ 0.5, 0.5, 10.0 };

        final Image< R > image = CachedCellImgReader.asImage( fileInfos );

        final ArrayList< double[] > centres = new ArrayList<>();
        centres.add( new double[]{ 522.0, 1143.0 } );
        centres.add( new double[]{ 1407.0 + 50, 546.0 + 50 } );
        final double[] spans = { 800, 800 };

        final ArrayList< double[] > optimisedCentres =
                RegionOptimiser.optimiseCentres2D(
                        image.getRai(),
                        centres,
                        spans,
                        fileInfos.voxelSpacing );

        final RandomAccessibleInterval< R > colorRAI
                = RegionMerger.merge(
                        image.getRai(), optimisedCentres, spans, fileInfos.voxelSpacing );

        viewer.show(
                colorRAI,
                image.getName(),
                image.getVoxelSpacing(),
                image.getVoxelUnit(),
                true );

    }

}
