package de.embl.cba.bdp2.devel;

import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.Utils;
import net.imglib2.FinalInterval;
import net.imglib2.FinalRealInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.realtransform.AffineRandomAccessible;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.RealViews;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public class Rai5DTimePointTransformer< R extends RealType< R > & NativeType< R > > extends RecursiveTask< RandomAccessibleInterval >
{
	private RandomAccessibleInterval raiXYZCT;
	private int t;
	private final int numChannels;
	private final AffineTransform3D affine;
	private InterpolatorFactory interpolatorFactory;

	public Rai5DTimePointTransformer( RandomAccessibleInterval raiXYZCT, int timePoint, AffineTransform3D affine, InterpolatorFactory interpolatorFactory) {
		this.raiXYZCT = raiXYZCT;
		this.t = timePoint;
		this.numChannels = (int) raiXYZCT.dimension( DimensionOrder.C );
		this.affine = affine;
		this.interpolatorFactory = interpolatorFactory;
	}

	@Override
	protected RandomAccessibleInterval< R > compute()
	{
		RandomAccessibleInterval< R > timeSlice = Views.hyperSlice( raiXYZCT, DimensionOrder.T, t);
		List< RandomAccessibleInterval< R > > channels = new ArrayList<>();
		for ( int channel = 0; channel < numChannels; ++channel)
		{
			RandomAccessibleInterval< R > volume = Views.hyperSlice( timeSlice, DimensionOrder.C, channel);
			RealRandomAccessible< R > rra = Views.interpolate( Views.extendZero( volume ), this.interpolatorFactory);
			AffineRandomAccessible ara = RealViews.affine( rra, affine );
			FinalRealInterval bounds = affine.estimateBounds( volume );
			FinalInterval transformedInterval = Utils.asIntegerInterval( bounds );
			RandomAccessibleInterval intervalView = Views.interval( ara, transformedInterval );
			channels.add( intervalView );
		}
		return Views.stack( channels );
	}
}
