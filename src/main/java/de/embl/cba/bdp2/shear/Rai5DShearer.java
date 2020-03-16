package de.embl.cba.bdp2.shear;

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

public class Rai5DShearer<T extends RealType<T> & NativeType<T> > extends RecursiveTask< RandomAccessibleInterval >
{
	private RandomAccessibleInterval raiXYZCT;
	private int t;
	private final int nChannels;
	private final AffineTransform3D affine;
	private InterpolatorFactory interpolatorFactory;

	public Rai5DShearer( RandomAccessibleInterval raiXYZCT, int time, int nChannels, AffineTransform3D affine, InterpolatorFactory interpolatorFactory) {
		this.raiXYZCT = raiXYZCT;
		this.t = time;
		this.nChannels = nChannels;
		this.affine = affine;
		this.interpolatorFactory = interpolatorFactory;
	}

	@Override
	protected RandomAccessibleInterval<T> compute() {
		List<RandomAccessibleInterval<T>> channelTracks = new ArrayList<>();
		RandomAccessibleInterval tStep = Views.hyperSlice( raiXYZCT, DimensionOrder.T, t);
		for (int channel = 0; channel < nChannels; ++channel) {
			RandomAccessibleInterval cStep = Views.hyperSlice(tStep, DimensionOrder.C, channel);
			RealRandomAccessible real = Views.interpolate(Views.extendZero(cStep),this.interpolatorFactory);
			AffineRandomAccessible af = RealViews.affine(real, affine);
			FinalRealInterval transformedRealInterval = affine.estimateBounds(cStep);
			FinalInterval transformedInterval = Utils.asIntegerInterval(transformedRealInterval);
			RandomAccessibleInterval intervalView = Views.interval(af, transformedInterval);
			channelTracks.add(intervalView);
		}
		return Views.stack(channelTracks);
	}
}
