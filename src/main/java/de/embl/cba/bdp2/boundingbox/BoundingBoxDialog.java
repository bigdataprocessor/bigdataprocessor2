package de.embl.cba.bdp2.boundingbox;

import bdv.tools.boundingbox.BoxSelectionOptions;
import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import de.embl.cba.bdp2.utils.DimensionOrder;
import net.imglib2.FinalRealInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.Intervals;
import bdv.tools.boundingbox.TransformedRealBoxSelectionDialog;

import static de.embl.cba.bdp2.fileinfosource.FileInfoConstants.*;

public class BoundingBoxDialog
{

    private Bdv bdv;
    public static final int X = 0;
    public static final int Y = 1;
    public static final int Z = 2;
    public static final int T = 3;
    public int[] selectedMin; // TODO: double?
    public int[] selectedMax; // TODO: double?


    public BoundingBoxDialog( Bdv bdv ) {
        this.bdv = bdv;
    }


    public void show(RandomAccessibleInterval rai, double[] voxelSize) {
        final int[] min, max;
        min = new int[4];
        min[T] = (int) rai.min( DimensionOrder.T);
        max = new int[4];
        max[T] = (int) rai.max( DimensionOrder.T);

        for (int d = 0; d < 3; d++) {
            min[d] = (int) (rai.min(d) * voxelSize[d]);
            max[d] = (int) (rai.max(d) * voxelSize[d]);
        }
        long[] size = new long[MAX_ALLOWED_IMAGE_DIMS];
        rai.dimensions(size);
        int[] center = new int[3];
        int[] width = new int[3];
        int[] initialBBSize = new int[3];
        for (int d = 0; d < 3; d++) {
            width[d] = (max[d] - min[d]);
            center[d] = (int) ((min[d] + width[d] / 2.0));
            initialBBSize[d] = width[d] / 4;
        }

        if (initialBBSize[Z] < 1) { // Check if Z goes below 1
            initialBBSize[Z] = 1;
        }
        int[] minBB = new int[]{center[X] - initialBBSize[X] / 2, center[Y] - initialBBSize[Y] / 2, center[Z] - initialBBSize[Z] / 2}; //Positioning the new BB at the center of the image.
        int[] maxBB = new int[]{center[X] + initialBBSize[X] / 2, center[Y] + initialBBSize[Y] / 2, center[Z] + initialBBSize[Z] / 2}; //Positioning the new BB at the center of the image.

        final Interval initialInterval, rangeInterval;
        initialInterval = Intervals.createMinMax(minBB[X], minBB[Y], minBB[Z],
                maxBB[X], maxBB[Y], maxBB[Z]); // the initially selected bounding box

        rangeInterval = Intervals.createMinMax(min[X], min[Y], min[Z],
                max[X], max[Y], max[Z]);// the range (bounding box of possible bounding boxes)

        final AffineTransform3D boxTransform = new AffineTransform3D();

        final TransformedRealBoxSelectionDialog.Result result = BdvFunctions.selectRealBox(
                bdv,
                boxTransform,
                initialInterval,
                rangeInterval,
                BoxSelectionOptions.options()
                        .title("Select box")
                        .initialTimepointRange(min[T], max[T])
                        .selectTimepointRange(min[T], max[T])
        );

        if (result.isValid()) {
            FinalRealInterval finalRealInterval = (FinalRealInterval) result.getInterval();
            this.selectedMax = new int[4];
            this.selectedMin = new int[4];

            for (int d = 0; d < finalRealInterval.numDimensions(); ++d) {
                selectedMin[d] = (int) finalRealInterval.realMin(d);
                selectedMax[d] = (int) finalRealInterval.realMax(d);
            }
            selectedMin[T] = result.getMinTimepoint();
            selectedMax[T] = result.getMaxTimepoint();
        }
    }
}
