package de.embl.cba.bdp2.transform;

import de.embl.cba.bdp2.devel.Rai5DTimePointTransformer;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.utils.DimensionOrder;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.List;

public class ImageTransformer < R extends RealType< R > & NativeType< R > >
{
	private final Image< R > image;
	private final AffineTransform3D transform3D;
	private InterpolatorFactory interpolatorFactory;

	public ImageTransformer( Image< R > image, AffineTransform3D transform3D, InterpolatorFactory interpolatorFactory )
	{
		this.image = image;
		this.transform3D = transform3D;
		this.interpolatorFactory = interpolatorFactory;
	}

	public Image transform()
	{
		final RandomAccessibleInterval< R > raiXYZCT = image.getRai();
		List< RandomAccessibleInterval< R > > timePoints = new ArrayList<>();

		int numTimePoints = (int) raiXYZCT.dimension( DimensionOrder.T );
		List< Rai5DTimePointTransformer > tasks = new ArrayList<>();
		for (int t = 0; t < numTimePoints; ++t)
		{
			Rai5DTimePointTransformer task = new Rai5DTimePointTransformer( raiXYZCT, t, transform3D, interpolatorFactory );
			task.fork();
			tasks.add(task);
		}
		for ( Rai5DTimePointTransformer task : tasks) {
			timePoints.add( (RandomAccessibleInterval) task.join() );
		}

		final RandomAccessibleInterval< R > transformedXYZCT = Views.zeroMin( Views.stack( timePoints ) );

		final Image< R > transformedImage = image.newImage( transformedXYZCT );
		// TODO: also adapt voxel size
		return transformedImage;
	}
}
