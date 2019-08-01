package de.embl.cba.bdp2.bin;

import de.embl.cba.bdp2.Image;
import de.embl.cba.lazyalgorithm.LazyDownsampler;
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
			newVoxelSize[ d ] = voxelSpacing[ d ] * ( 2 * span[ d ] + 1 );

		return newVoxelSize;
	}

	public static < T extends RealType< T > & NativeType< T > >
	Image< T > bin( Image< T > inputImage, long[] radii )
	{
		final RandomAccessibleInterval< T > downSampleView =
				new LazyDownsampler<>( inputImage.getRai(), radii ).getDownsampledView();

		return ( Image< T > ) new Image(
					downSampleView,
					inputImage.getName(),
					getBinnedVoxelSize(
							radii,
							inputImage.getVoxelSpacing() ),
					inputImage.getVoxelUnit(),
					inputImage.getFileInfos()
		);
	}
}
