package de.embl.cba.bdp2.process;

import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.Utils;
import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.AbstractImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.loops.LoopBuilder;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import static de.embl.cba.bdp2.utils.DimensionOrder.*;

public abstract class Processor
{
	public static < R extends RealType< R > & NativeType< R > >
	ImagePlus getProcessedDataCubeAsImagePlus(
			RandomAccessibleInterval< R > rai,
			int c,
			int t,
			double[] voxelSpacing,
			String voxelUnit )
	{

		long start = System.currentTimeMillis();

		long[] minInterval = new long[]{
				rai.min( DimensionOrder.X ),
				rai.min( DimensionOrder.Y ),
				rai.min( DimensionOrder.Z ),
				c,
				t };

		long[] maxInterval = new long[]{
				rai.max( DimensionOrder.X ),
				rai.max( DimensionOrder.Y ),
				rai.max( DimensionOrder.Z ),
				c,
				t };

		// TODO: instead of copying the ImagePlus, one could copyVolumeRAI the RAI
		// and while doing so, employ multi-threading

		RandomAccessibleInterval< R > oneChannelAndTimePoint =
				Views.interval( rai, minInterval, maxInterval);


		ImagePlus imagePlus =
				Utils.wrap3DRaiToCalibratedImagePlus(
						oneChannelAndTimePoint,
						voxelSpacing,
						voxelUnit,
					"");


		final ImagePlus duplicate = imagePlus.duplicate();

		Logger.log( "Load and process data cube [ s ]: "
				+ ( System.currentTimeMillis() - start ) / 1000);

		return duplicate;
	}

	public static < R extends RealType< R > & NativeType< R > >
	RandomAccessibleInterval< R > get3DRai(
			RandomAccessibleInterval< R > image,
			int c,
			int t )
	{
		long start = System.currentTimeMillis();

		long[] minInterval = new long[]{
				image.min( X ),
				image.min( Y ),
				image.min( Z ),
				c,
				t };

		long[] maxInterval = new long[]{
				image.max( X ),
				image.max( Y ),
				image.max( Z ),
				c,
				t };

		RandomAccessibleInterval raiXYZ =
				Views.dropSingletonDimensions(
						Views.interval( image, minInterval, maxInterval ) );

		// force view into RAM
		// TODO: Here, multi-threading could be interesting
		raiXYZ = copyVolumeRAI( raiXYZ );

		Logger.debug( "Load and process data cube [ s ]: "
				+ ( System.currentTimeMillis() - start ) / 1000);

		return raiXYZ;
	}

	/**
	 *
	 * Create a copyVolumeRAI, thereby forcing computation of a potential
	 * cascade of views.
	 *
	 * @param volume
	 * @param <R>
	 * @return
	 */
	public static < R extends RealType< R > & NativeType< R > >
	RandomAccessibleInterval< R > copyVolumeRAI( RandomAccessibleInterval< R > volume )
	{
		final long numElements =
				AbstractImg.numElements( Intervals.dimensionsAsLongArray( volume ) );

		RandomAccessibleInterval< R > copy;


		if ( numElements < Integer.MAX_VALUE - 1 )
		{
			copy = new ArrayImgFactory( Util.getTypeFromInterval( volume ) ).create( volume );
		}
		else
		{
			int nz = (int) ( numElements / ( volume.dimension( 0  ) * volume.dimension( 1 ) ) );

			final int[] cellSize = {
					( int ) volume.dimension( 0 ),
					( int ) volume.dimension( 1 ),
					nz };

			copy = new CellImgFactory( Util.getTypeFromInterval( volume ), cellSize ).create( volume );
		}

		LoopBuilder.setImages( copy, volume ).forEachPixel( Type::set );

		return copy;
	}
}
