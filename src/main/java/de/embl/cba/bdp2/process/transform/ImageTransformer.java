/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2025 EMBL
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
package de.embl.cba.bdp2.process.transform;

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

		// TODO: Do we really want a Views.zeroMin here?
		final RandomAccessibleInterval< R > transformedXYZCT = Views.zeroMin( Views.stack( timePoints ) );
		final Image< R > transformedImage = new Image( this.image );
		transformedImage.setRai( transformedXYZCT );

		// TODO: also adapt voxel size
		return transformedImage;
	}
}
