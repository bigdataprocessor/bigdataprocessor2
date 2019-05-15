package de.embl.cba.bdp2.boundingbox;

import bdv.tools.boundingbox.BoxSelectionOptions;
import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.utils.DimensionOrder;
import net.imglib2.FinalRealInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.util.Intervals;
import bdv.tools.boundingbox.TransformedRealBoxSelectionDialog;

public class BoundingBoxDialog
{

    private Bdv bdv;
    public static final int X = 0;
    public static final int Y = 1;
    public static final int Z = 2;
    public static final int T = 3;
    public double[] selectedMin;
    public double[] selectedMax;
    private Interval initialInterval;
    private Interval rangeInterval;
    private int[] min;
    private int[] max;


    public BoundingBoxDialog( Bdv bdv )
    {
        this.bdv = bdv;
    }

    public void showCalibratedUnitsBox( RandomAccessibleInterval rai, double[] voxelSpacing, String voxelUnit ) {

        setInitialSelectionAndRange( rai, voxelSpacing );

        final TransformedRealBoxSelectionDialog.Result result = showBox( voxelUnit );

        if ( result.isValid() )
        {
            FinalRealInterval finalRealInterval = (FinalRealInterval) result.getInterval();
            selectedMax = new double[4];
            selectedMin = new double[4];

            for (int d = 0; d < finalRealInterval.numDimensions(); ++d)
            {
                selectedMin[d] = finalRealInterval.realMin(d);
                selectedMax[d] = finalRealInterval.realMax(d);
            }

            selectedMin[T] = result.getMinTimepoint();
            selectedMax[T] = result.getMaxTimepoint();
        }
    }


    public void showVoxelUnitsBox( RandomAccessibleInterval rai ) {

        setInitialSelectionAndRange( rai, new double[]{1,1,1} );

        final TransformedRealBoxSelectionDialog.Result result = showBox( null );

        if (result.isValid())
        {
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

    public TransformedRealBoxSelectionDialog.Result showBox( String voxelUnit )
    {
        final AffineTransform3D boxTransform = new AffineTransform3D();

        return BdvFunctions.selectRealBox(
                bdv,
                boxTransform,
                initialInterval,
                rangeInterval,
                BoxSelectionOptions.options()
                        .title( getBoxTitle( voxelUnit ) )
                        .initialTimepointRange( min[ T ], max[ T ] )
                        .selectTimepointRange( min[ T ], max[ T ] )
        );
    }

    public String getBoxTitle( String voxelUnit )
    {
        String title = "Select box";
        if ( voxelUnit != null )
            title += "[ " + voxelUnit + " ]";
        else
            title += "[ voxels ]";
        return title;
    }

    public void setInitialSelectionAndRange( RandomAccessibleInterval rai, double[] voxelSpacing )
    {
        min = new int[4];
        min[T] = (int) rai.min( DimensionOrder.T);
        max = new int[4];
        max[T] = (int) rai.max( DimensionOrder.T);

        for (int d = 0; d < 3; d++) {
            min[d] = (int) (rai.min(d) * voxelSpacing[d]);
            max[d] = (int) (rai.max(d) * voxelSpacing[d]);
        }

        long[] size = new long[ FileInfos.MAX_ALLOWED_IMAGE_DIMS];
        rai.dimensions(size);
        int[] center = new int[3];
        int[] width = new int[3];
        int[] initialBBSize = new int[3];

        for (int d = 0; d < 3; d++)
        {
            width[d] = ( max[d] - min[d]);
            center[d] = (int) (( min[d] + width[d] / 2.0));
            initialBBSize[d] = width[d] / 4;
        }

        if ( initialBBSize[Z] < 1 )
        { // Check if Z goes below 1
            initialBBSize[Z] = 1;
        }

        int[] minBB = new int[]{
                center[X] - initialBBSize[X] / 2,
                center[Y] - initialBBSize[Y] / 2,
                center[Z] - initialBBSize[Z] / 2};

        int[] maxBB = new int[]{
                center[X] + initialBBSize[X] / 2,
                center[Y] + initialBBSize[Y] / 2,
                center[Z] + initialBBSize[Z] / 2};

        initialInterval = Intervals.createMinMax(
                minBB[X], minBB[Y], minBB[Z],
                maxBB[X], maxBB[Y], maxBB[Z]);

        rangeInterval = Intervals.createMinMax(
                min[X], min[Y], min[Z],
                max[X], max[Y], max[Z]);
    }
}
