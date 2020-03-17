package de.embl.cba.bdp2.bin;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.lazyalgorithm.view.NeighborhoodViews;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.util.Arrays;

public class Binner
{
	public static double[] getBinnedVoxelSize( long[] span, double[] voxelSpacing )
	{
		final double[] newVoxelSize = new double[ voxelSpacing.length ];

		for ( int d = 0; d < 3; d++ )
			newVoxelSize[ d ] = voxelSpacing[ d ] * span[ d ];

		return newVoxelSize;
	}

	public static < T extends RealType< T > & NativeType< T > >
	Image< T > bin( Image< T > inputImage, long[] span )
	{
//		boolean allOne = true;
//		for ( int i = 0; i < span.length; i++ )
//			if ( span[ i ] != 1 )
//				allOne = false;
//
//		if ( allOne ) return inputImage;
//
//		boolean someSmallerOne = false;
//		for ( int i = 0; i < span.length; i++ )
//			if ( span[ i ] < 1 )
//				someSmallerOne = true;
//
//		if ( someSmallerOne )
//			throw new UnsupportedOperationException( "The minimal bin width is 1.\n " +
//					"Some values of the requested binning span were smaller than one: " + Arrays.toString( span ) );

		final RandomAccessibleInterval< T > binnedRai =
				NeighborhoodViews.averageBinnedView( inputImage.getRai(), span );

		return ( Image< T > ) new Image(
					binnedRai,
					inputImage.getName(),
					getBinnedVoxelSize( span, inputImage.getVoxelSpacing() ),
					inputImage.getVoxelUnit(),
					inputImage.getFileInfos()
		);
	}
}
