package de.embl.cba.bdp2.process.bin;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.lazyalgorithm.view.NeighborhoodViews;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

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
		RandomAccessibleInterval< T > binnedRai = binImageWithNonZeroSpatialOffset( inputImage, span );

		return ( Image< T > ) new Image(
					binnedRai,
					inputImage.getName(),
					inputImage.getChannelNames(),
					getBinnedVoxelSize( span, inputImage.getVoxelSize() ),
					inputImage.getVoxelUnit(),
					inputImage.getFileInfos()
		);
	}

	public static < T extends RealType< T > & NativeType< T > > RandomAccessibleInterval< T > binImageWithNonZeroSpatialOffset( Image< T > inputImage, long[] span )
	{
		RandomAccessibleInterval< T > rai = inputImage.getRai();

		final long[ ] min = new long[ 5 ];
		rai.min( min );
		rai = Views.zeroMin( rai );

		RandomAccessibleInterval< T > binnedRai = NeighborhoodViews.averageBinnedView( rai, span );

		for ( int d = 0; d < 3; d++ )
		{
			min[ d ] /= span[ d ];
		}
		binnedRai = Views.translate( binnedRai, min );
		return binnedRai;
	}
}
