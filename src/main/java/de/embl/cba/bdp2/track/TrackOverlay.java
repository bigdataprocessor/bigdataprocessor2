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
package de.embl.cba.bdp2.track;

import bdv.util.BdvHandle;
import bdv.util.BdvOverlay;
import net.imglib2.realtransform.AffineTransform3D;

import java.awt.*;


public class TrackOverlay extends BdvOverlay
{
	private final BdvHandle bdvHandle;
	private final Track track;
	private final double depthOfField;

	enum Shape
	{
		Filled,
		Empty
	}

	public TrackOverlay( BdvHandle bdvHandle, Track track, double depthOfField )
	{
		super();
		this.bdvHandle = bdvHandle;
		this.track = track;
		this.depthOfField = depthOfField;
	}

	@Override
	protected void draw( final Graphics2D g )
	{
		final int numTimepoints = bdvHandle.getViewerPanel().state().getNumTimepoints();
		for ( int t = 0; t < numTimepoints ; t++ )
		{
			drawShape( g, t );
		}
	}

	private void drawShape( Graphics2D g, int t )
	{
		if ( ! track.getTimePoints().contains( t ) ) return;

		final double[] position = track.getPosition( t );

		final AffineTransform3D viewerTransform = new AffineTransform3D();
		getCurrentTransform3D( viewerTransform );

		final double[] positionInViewer = new double[ 3 ];
		viewerTransform.apply( position, positionInViewer );
		final double z = positionInViewer[ 2 ];

		// set appearance
		final int size = getSize( t, z );
		final Shape shape = getShape( t );
		final Color color = getColor( z, track.getType( t ) );

		// draw
		final int x = ( int ) ( positionInViewer[ 0 ] - 0.5 * size );
		final int y = ( int ) ( positionInViewer[ 1 ] - 0.5 * size );
		g.setColor( color );
		if ( shape.equals( Shape.Filled ) )
			g.fillOval( x, y, size, size );
		else if ( shape.equals( Shape.Empty ))
			g.drawOval( x, y, size, size );

	}

	private int getSize( int t, double z )
	{
		int size;

		if ( t == bdvHandle.getViewerPanel().state().getCurrentTimepoint() )
			size = getZSize( z );
		else
			size = 3;
		return size;
	}

	private Shape getShape( int t )
	{
		if ( t == bdvHandle.getViewerPanel().state().getCurrentTimepoint() )
			return Shape.Empty;
		else
			return Shape.Filled;
	}

	private Color getColor( final double depth, TrackPosition.PositionType type )
	{
		int alpha = 255 - ( int ) Math.round( Math.abs( depth ) );

		if ( alpha < 64 )
			alpha = 64;

		if ( type.equals( TrackPosition.PositionType.Anchor ))
			return new Color( 255, 255, 0, alpha );
		else if ( type.equals( TrackPosition.PositionType.Interpolated ) )
			return new Color( 0, 0, 255, alpha );
		else
			throw new RuntimeException( "Cannot color type: " + type );
	}

	private int getZSize( final double depth )
	{
		return ( int ) Math.max( 5, 20 - 1.0 / depthOfField * Math.round( Math.abs( depth ) ) );
	}

}
