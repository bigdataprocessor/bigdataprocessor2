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
package de.embl.cba.bdp2.process.bin;

import bdv.tools.brightness.SliderPanel;
import bdv.util.BoundedValue;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.record.ScriptRecorder;
import de.embl.cba.bdp2.dialog.AbstractProcessingDialog;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewer.ImageViewer;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;

public class BinDialog< R extends RealType< R > & NativeType< R > > extends AbstractProcessingDialog< R >
{
	private long[] span;

	public BinDialog( final ImageViewer< R > viewer )
	{
		this.inputImage = viewer.getImage();
		this.viewer = viewer;

		Logger.info( "Image size [GB]: " + Utils.getSizeGB( this.inputImage.getRai() ) );
		createPanel();
	}

	@Override
	protected void recordMacro()
	{
		final ScriptRecorder recorder = new ScriptRecorder( BinCommand.COMMAND_FULL_NAME, inputImage, outputImage );

		recorder.addCommandParameter( "binWidthXPixels",  span[ 0 ] );
		recorder.addCommandParameter( "binWidthYPixels",  span[ 1 ] );
		recorder.addCommandParameter( "binWidthZPixels",  span[ 2 ] );

		// Image< R > bin( Image< R > image, long[] spanXYZCT )
		recorder.setBDP2FunctionName( "bin" );
		recorder.addAPIFunctionPrequelComment( BinCommand.COMMAND_NAME );
		recorder.addAPIFunctionParameter( span );

		recorder.record();
	}

	@Override
	protected void createPanel()
	{
		panel = new JPanel();
		panel.setLayout( new BoxLayout( panel, BoxLayout.PAGE_AXIS ) );

		final ArrayList< BoundedValue > boundedValues = new ArrayList<>();
		final ArrayList< SliderPanel > sliderPanels = new ArrayList<>();

		final String[] xyz = { "X", "Y", "Z" };

		for ( int d = 0; d < 3; d++ )
		{
			boundedValues.add(
					new BoundedValue(1,21,1 ) );

			sliderPanels.add(
					new SliderPanel(
							"  Binning " + xyz[ d ] ,
								boundedValues.get( d ),
								1 ));
		}

		class UpdateListener implements BoundedValue.UpdateListener
		{
			private long[] previousSpan;

			@Override
			public synchronized void update()
			{
				span = getNewSpan();

				if ( ! isSpanChanged( span ) ) return;

				previousSpan = span;

				outputImage = Binner.bin( inputImage, span );
				outputImage.setName( inputImage.getName() + "-bin" );
				viewer.replaceImage( outputImage, false, true );

				for ( SliderPanel sliderPanel : sliderPanels )
				{
					sliderPanel.update();
				}

				Logger.info( "Binning: "
						+ span[ 0 ] + " , "
						+ span[ 1 ] + " , "
						+ span[ 2 ] + " [Pixels]" +
						"; Size [GB]: " + Utils.getSizeGB( outputImage.getRai() ) );
			}

			private boolean isSpanChanged( long[] span )
			{
				if ( previousSpan == null ) return true;

				for ( int d = 0; d < 3; d++ )
					if ( span[ d ] != previousSpan[ d ] )
						return true;

				return false;
			}

			private long[] getNewSpan()
			{
				final long[] span = new long[ 5 ];
				Arrays.fill( span, 1 );
				for ( int d = 0; d < 3; d++ )
					span[ d ] = boundedValues.get( d ).getCurrentValue() ;
				return span;
			}
		}

		final UpdateListener updateListener = new UpdateListener();

		for ( int d = 0; d < 3; d++ )
		{
			boundedValues.get( d ).setUpdateListener( updateListener );
			panel.add( sliderPanels.get( d ) );
		}
	}
}
