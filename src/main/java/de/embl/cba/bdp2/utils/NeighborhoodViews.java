/*-
 * #%L
 * Various Java code for ImageJ
 * %%
 * Copyright (C) 2018 - 2024 EMBL
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
package de.embl.cba.bdp2.utils;

import de.embl.cba.bdp2.utils.NeighborhoodAverageConverter;
import de.embl.cba.bdp2.utils.NeighborhoodNonZeroBoundariesConverter;
import de.embl.cba.bdp2.utils.RectangleShape2;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.View;
import net.imglib2.algorithm.neighborhood.HyperSphereShape;
import net.imglib2.algorithm.neighborhood.Neighborhood;
import net.imglib2.algorithm.neighborhood.Shape;
import net.imglib2.converter.Converter;
import net.imglib2.converter.Converters;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

public class NeighborhoodViews implements View
{
	public static < R extends RealType< R > >
	RandomAccessibleInterval< R > averageBinnedView(
			RandomAccessibleInterval< R > rai,
			long[] span )
	{
		return Views.subsample(
					NeighborhoodViews.rectangleAverageView( rai, span ),
					span );
	}

	/**
	 * Provides an averaged filtered view on the input data.
	 */
	public static < R extends RealType< R > >
	RandomAccessibleInterval< R > rectangleAverageView(
			RandomAccessibleInterval< R > rai,
			long[] span )
	{
		return neighborhoodConvertedView(
				rai,
				new NeighborhoodAverageConverter(),
				new RectangleShape2( span, false ) );
	}


	/**
	 * Provides an boundary filtered view on the input data.
	 * That is, only pixels where some pixel in the
	 * neighborhood has a different value are keeping their value.
	 * Other pixels are set to zero.
	 *
	 * TODO: also enable even kernels?!
	 */
	public static < R extends RealType< R > >
	RandomAccessibleInterval< R > nonZeroBoundariesView(
			RandomAccessibleInterval< R > rai,
			long radius )
	{
		return neighborhoodConvertedView(
				rai,
				new NeighborhoodNonZeroBoundariesConverter<>(  ),
				new HyperSphereShape( radius ) );
	}


	public static < R extends RealType< R > >
	RandomAccessibleInterval< R > neighborhoodConvertedView(
			RandomAccessibleInterval< R > rai,
			Converter< Neighborhood< R >, R > neighborhoodAverageConverter,
			Shape shape )
	{
		final RandomAccessible< Neighborhood< R > > nra =
				shape.neighborhoodsRandomAccessible(
						Views.extendBorder( rai ) );

		final RandomAccessibleInterval< Neighborhood< R > > nrai
				= Views.interval( nra, rai );

		return Converters.convert( nrai,
				neighborhoodAverageConverter,
				rai.getType() );


	}

}
