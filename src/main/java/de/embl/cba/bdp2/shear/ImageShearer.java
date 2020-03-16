package de.embl.cba.bdp2.shear;

import de.embl.cba.bdp2.utils.DimensionOrder;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.List;


/**
 * TODO: make a proper class, e.g. consuming an Image
 *
 *
 */
public class ImageShearer
{
	// TODO: move to Shearing but still have convenience access from here
	public static <T extends RealType<T> & NativeType<T> >
	RandomAccessibleInterval shearRai5D(
			final RandomAccessibleInterval< T > raiXYZCT, // TODO: take an image instead
			ShearingSettings shearingSettings )
	{
		// TODO: Refactor into class
		System.out.println("Shear Factor X " + shearingSettings.shearingFactorX);
		System.out.println("Shear Factor Y " + shearingSettings.shearingFactorY);

		List< RandomAccessibleInterval< T > > timeTracks = new ArrayList<>();
		int nTimeFrames = (int) raiXYZCT.dimension( DimensionOrder.T );
		int nChannels = (int) raiXYZCT.dimension( DimensionOrder.C );
		System.out.println("Shear Factor X " + shearingSettings.shearingFactorX);
		System.out.println("Shear Factor Y " + shearingSettings.shearingFactorY);
		AffineTransform3D affine = new AffineTransform3D();
		affine.set(shearingSettings.shearingFactorX, 0, 2);
		affine.set(shearingSettings.shearingFactorY, 1, 2);
		List< Rai5DShearer > tasks = new ArrayList<>();
	   // long startTime = System.currentTimeMillis();
		for (int t = 0; t < nTimeFrames; ++t) {
			Rai5DShearer task = new Rai5DShearer(raiXYZCT, t, nChannels, affine, shearingSettings.interpolationFactory);
			task.fork();
			tasks.add(task);
		}
		for ( Rai5DShearer task : tasks) {
			timeTracks.add((RandomAccessibleInterval) task.join());
		}
		final RandomAccessibleInterval sheared = Views.stack( timeTracks );
//        System.out.println("Time elapsed (ms) " + (System.currentTimeMillis() - startTime));


		return sheared;
	}
}
