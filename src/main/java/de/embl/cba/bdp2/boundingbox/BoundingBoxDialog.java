/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2021 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package de.embl.cba.bdp2.boundingbox;

import bdv.tools.boundingbox.BoxSelectionOptions;
import bdv.tools.boundingbox.TransformedBoxSelectionDialog;
import bdv.tools.boundingbox.TransformedRealBoxSelectionDialog;
import bdv.util.Bdv;
import bdv.util.BdvFunctions;
import bdv.util.BdvHandle;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdv.utils.BdvUtils;
import net.imglib2.*;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;

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

    private FinalInterval getVoxelUnits5DInterval()
    {
        final RandomAccessibleInterval< R > rai = image.getRai();

        double[] voxelSpacing = getVoxelSpacings();

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

    private FinalRealInterval getCalibrated5DInterval()
    {
        final RandomAccessibleInterval< R > rai = image.getRai();

        double[] min = { selectedMin[ X ], selectedMin[ Y ], selectedMin[ Z ], rai.min( DimensionOrder.C), selectedMin[ T ] };
        double[] max = { selectedMax[ X ], selectedMax[ Y ], selectedMax[ Z ], rai.max( DimensionOrder.C), selectedMax[ T ] };

        return new FinalRealInterval( min, max );
    }

    private double[] getVoxelSpacings()
    {
        double[] voxelSpacing;
        if ( selectionIsCalibrated )
            voxelSpacing = image.getVoxelDimensions();
        else
            voxelSpacing = new double[]{ 1, 1, 1 };
        return voxelSpacing;
    }

    public FinalInterval getVoxelSelectionInterval()
    {
        FinalInterval interval;
        if ( selectedMax != null && selectedMin != null ) {
            interval = getVoxelUnits5DInterval();
        }else{
            interval =  null;
        }
        return interval;
    }

    public FinalRealInterval getRealSelectionInterval()
    {
        FinalRealInterval interval;
        if ( selectedMax != null && selectedMin != null ) {
            interval = getCalibrated5DInterval();
        }else{
            interval =  null;
        }
        return interval;
    }


    public void showRealBoxAndWaitForResult()
    {
        setInitialSelectionAndRange( true );

        final TransformedRealBoxSelectionDialog.Result result = showRealBox( image.getVoxelUnit().getSymbol() );
        if ( result.isValid() )
        {
            collectSelection( result.getInterval(), result.getMinTimepoint(), result.getMaxTimepoint() );
            selectionIsCalibrated = true;
        }
    }

    public void showVoxelBoxAndWaitForResult() {

        setInitialSelectionAndRange( false );

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
        boxTransform.set( image.getVoxelDimensions()[0], 0,0  );
        boxTransform.set( image.getVoxelDimensions()[1], 1,1  );
        boxTransform.set( image.getVoxelDimensions()[2], 2,2  );

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

    private void setInitialSelectionAndRange( boolean calibrated )
    {
        setRangeInterval( calibrated );
        setInitialInterval( calibrated );
    }

    private void setInitialInterval( boolean calibrated )
    {
        final FinalRealInterval viewerBoundingInterval = BdvUtils.getViewerGlobalBoundingInterval( bdv );
        double[] initialCenter = new double[ 3 ];
        double[] initialSize = new double[ 3 ];

        for (int d = 0; d < 3; d++)
        {
            initialCenter[ d ] = ( viewerBoundingInterval.realMax( d ) + viewerBoundingInterval.realMin( d ) ) / 2.0;
            initialSize[ d ] = ( viewerBoundingInterval.realMax( d ) - viewerBoundingInterval.realMin( d ) ) / 2.0;

            if ( ! calibrated )
            {
                initialCenter[ d ] /= image.getVoxelDimensions()[ d ];
                initialSize[ d ] /= image.getVoxelDimensions()[ d ];
            }
        }

        // TODO: improve this: take whole range in the smaller direction (up or down..)
        initialSize[ DimensionOrder.Z ] = image.getRai().dimension( DimensionOrder.Z ) / 10;

        if ( calibrated )
            initialSize[ DimensionOrder.Z ] *= image.getVoxelDimensions()[ DimensionOrder.Z ];

        initialSize[ DimensionOrder.Z ] = (int) Math.max( initialSize[ DimensionOrder.Z ],
                Math.ceil( image.getVoxelDimensions()[ DimensionOrder.Z ] ) );

        double[] minBB = new double[]{
                initialCenter[ X ] - initialSize[ X ] / 2,
                initialCenter[ Y ] - initialSize[ Y ] / 2,
                initialCenter[ Z ] - initialSize[ Z ] / 2 };

        double[] maxBB = new double[]{
                initialCenter[ X ] + initialSize[ X ] / 2,
                initialCenter[ Y ] + initialSize[ Y ] / 2,
                initialCenter[ Z ] + initialSize[ Z ] / 2 };

        initialInterval = Intervals.createMinMax(
                (long) minBB[X], (long) minBB[Y], (long) minBB[Z],
                (long) maxBB[X], (long) maxBB[Y], (long) maxBB[Z]);
    }

    private void setRangeInterval( boolean calibrated )
    {
        min = new int[ 4 ];
        max = new int[ 4 ];

        setRangeXYZ( image, calibrated );
        setRangeT( image );

        rangeInterval = Intervals.createMinMax(
                min[X], min[Y], min[Z],
                max[X], max[Y], max[Z]);
    }

    private void setRangeT( Image< R > image )
    {
        min[T] = (int) image.getRai().min( DimensionOrder.T );
        max[T] = (int) image.getRai().max( DimensionOrder.T );
    }

    private void setRangeXYZ( Image< R > image, boolean calibrated )
    {
        for (int d = 0; d < 3; d++)
        {
            min[ d ] = (int) ( image.getRai().min( d ) );
            max[ d ] = (int) ( image.getRai().max( d ) );

            if ( calibrated )
            {
                min[ d ] *= image.getVoxelDimensions()[ d ];
                max[ d ] *= image.getVoxelDimensions()[ d ];
            }
        }
    }
}
