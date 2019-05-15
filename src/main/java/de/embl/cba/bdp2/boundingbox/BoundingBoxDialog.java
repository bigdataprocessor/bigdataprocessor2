package de.embl.cba.bdp2.boundingbox;

import bdv.tools.boundingbox.BoxSelectionOptions;
import bdv.tools.boundingbox.TransformedBoxSelectionDialog;
import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandle;
import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.utils.DimensionOrder;
import net.imglib2.*;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.Scale;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import bdv.tools.boundingbox.TransformedRealBoxSelectionDialog;

public class BoundingBoxDialog < R extends RealType< R > & NativeType< R > >
{

    private final Image< R > image;
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
    private boolean selectionIsCalibrated;


    public BoundingBoxDialog( BdvHandle bdvHandle, Image< R > image )
    {
        this.bdv = bdvHandle;
        this.image = image;
    }

    public FinalInterval getVoxelUnits5DInterval()
    {
        final RandomAccessibleInterval< R > rai = image.getRai();

        double[] voxelSpacing;
        if ( selectionIsCalibrated )
            voxelSpacing = image.getVoxelSpacing();
        else
            voxelSpacing = new double[]{ 1, 1, 1 };

        long[] minMax = {
                (long) ( selectedMin[ X ] / voxelSpacing[ DimensionOrder.X] ),
                (long) ( selectedMin[ Y ] / voxelSpacing[ DimensionOrder.Y] ),
                (long) ( selectedMin[ Z ] / voxelSpacing[ DimensionOrder.Z] ),
                rai.min( DimensionOrder.C),
                (long) selectedMin[ T ],
                (long) ( selectedMax[ X ] / voxelSpacing[ DimensionOrder.X] ),
                (long) ( selectedMax[ Y ] / voxelSpacing[ DimensionOrder.Y] ),
                (long) ( selectedMax[ Z ] / voxelSpacing[ DimensionOrder.Z] ),
                rai.max( DimensionOrder.C),
                (long)  selectedMax[ T ]};

        return Intervals.createMinMax(minMax);
    }

    public FinalInterval getVoxelUnitsSelectionInterval( )
    {
        FinalInterval interval;
        if ( selectedMax != null && selectedMin != null ) {
            interval = getVoxelUnits5DInterval();
        }else{
            interval =  null;
        }
        return interval;
    }

    public void showCalibratedUnitsBox() {

        setInitialSelectionAndRange( image.getRai(), image.getVoxelSpacing() );

        final TransformedRealBoxSelectionDialog.Result result = showRealBox( image.getVoxelUnit() );

        if ( result.isValid() )
        {
            collectSelection( result.getInterval(), result.getMinTimepoint(), result.getMaxTimepoint() );
            selectionIsCalibrated = true;
        }
    }

    public void showVoxelUnitsBox() {

        setInitialSelectionAndRange( image.getRai(), new double[]{1,1,1} );

        final TransformedBoxSelectionDialog.Result result = showBox( );

        if ( result.isValid() )
        {
            collectSelection( result.getInterval(), result.getMinTimepoint(), result.getMaxTimepoint() );
            selectionIsCalibrated = false;
        }

    }

    private void collectSelection( RealInterval interval, int minTimepoint, int maxTimepoint )
    {
        selectedMax = new double[4];
        selectedMin = new double[4];

        for (int d = 0; d < interval.numDimensions(); ++d)
        {
            selectedMin[d] = interval.realMin(d);
            selectedMax[d] = interval.realMax(d);
        }

        selectedMin[T] = minTimepoint;
        selectedMax[T] = maxTimepoint;
    }



    public TransformedRealBoxSelectionDialog.Result showRealBox( String voxelUnit )
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

    public TransformedBoxSelectionDialog.Result showBox( )
    {
        final AffineTransform3D boxTransform = new AffineTransform3D();
        boxTransform.set( image.getVoxelSpacing()[0], 0,0  );
        boxTransform.set( image.getVoxelSpacing()[1], 1,1  );
        boxTransform.set( image.getVoxelSpacing()[2], 2,2  );

        return BdvFunctions.selectBox(
                bdv,
                boxTransform,
                initialInterval,
                rangeInterval,
                BoxSelectionOptions.options()
                        .title( getBoxTitle( null ) )
                        .initialTimepointRange( min[ T ], max[ T ] )
                        .selectTimepointRange( min[ T ], max[ T ] )
        );
    }


    public String getBoxTitle( String voxelUnit )
    {
        String title = "Select box";
        if ( voxelUnit != null )
            title += " [ " + voxelUnit + " ]";
        else
            title += " [ Voxels ]";
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
