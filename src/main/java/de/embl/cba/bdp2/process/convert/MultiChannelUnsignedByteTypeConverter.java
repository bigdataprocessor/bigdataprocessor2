/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2022 EMBL
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
package de.embl.cba.bdp2.process.convert;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.utils.DimensionOrder;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.converter.RealUnsignedByteConverter;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.List;

public class MultiChannelUnsignedByteTypeConverter< R extends RealType< R > & NativeType< R > >
{
	private final Image< R > inputImage;
	private final List< RealUnsignedByteConverter< R > > converters;

	public MultiChannelUnsignedByteTypeConverter( Image< R > inputImage, List< double[] > contrastLimits )
	{
		this.inputImage = inputImage;
		this.converters = new ArrayList<>(  );
		for ( double[] contrastLimit : contrastLimits )
		{
			converters.add( new RealUnsignedByteConverter<>( contrastLimit[ 0 ], contrastLimit[ 1] ) );
		}
	}

	public Image< R > getConvertedImage()
	{
		final ArrayList< RandomAccessibleInterval< R > > convertedChannelRais = new ArrayList<>();

		for ( int c = 0; c < inputImage.getNumChannels(); c++ )
		{
			final IntervalView< R > channel = Views.hyperSlice( inputImage.getRai(), DimensionOrder.C, c );
			final RandomAccessibleInterval< R > convertedRai =
					Converters.convert(
							( RandomAccessibleInterval ) channel,
							converters.get( c ),
							new UnsignedByteType() );
			convertedChannelRais.add( convertedRai );
		}

		final IntervalView< R > convertedRai = Views.permute( Views.stack( convertedChannelRais ), 3, 4 );

		final Image< R > outputImage = new Image< >( inputImage );
		outputImage.setRai( convertedRai );
		outputImage.setName( inputImage.getName() + "-8bit" );
		return outputImage;
	}

	public List< RealUnsignedByteConverter< R > >  getConverters()
	{
		return converters;
	}
}
