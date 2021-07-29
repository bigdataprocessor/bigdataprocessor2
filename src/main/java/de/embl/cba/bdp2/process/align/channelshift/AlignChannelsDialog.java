/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2021 EMBL
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
package de.embl.cba.bdp2.process.align.channelshift;

import bdv.tools.brightness.SliderPanel;
import bdv.util.BoundedValue;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.record.ScriptRecorder;
import de.embl.cba.bdp2.dialog.AbstractProcessingDialog;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewer.ImageViewer;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import javax.swing.*;
import java.util.ArrayList;

public class AlignChannelsDialog< T extends RealType< T > & NativeType< T > > extends AbstractProcessingDialog< T >
{
	private ArrayList< BoundedValue > boundedValues;
	private ArrayList< SliderPanel > sliderPanels;
	private ChromaticShiftUpdateListener updateListener;
	private final ChannelShifter channelShifter;
	private final long numChannels;
	private ArrayList< long[] > shifts;

	public AlignChannelsDialog( final ImageViewer< T > viewer  )
	{
		this.viewer = viewer;
		this.inputImage = viewer.getImage();

		channelShifter = new ChannelShifter( inputImage.getRai() );
		numChannels = inputImage.getRai().dimension( DimensionOrder.C );
		createPanel();
	}

	@Override
	protected void recordMacro()
	{
		final ScriptRecorder recorder = new ScriptRecorder( AlignChannelsCommand.COMMAND_FULL_NAME, inputImage, outputImage );
		recorder.addCommandParameter( "shifts", Utils.longsToDelimitedString( shifts ) );

		// Image< R > alignChannels( Image< R > image, List< long[] > shifts )
		recorder.setBDP2FunctionName( "alignChannels" );
		recorder.addAPIFunctionPrequelComment( AlignChannelsCommand.COMMAND_NAME );
		recorder.addAPIFunctionParameter( shifts );
		recorder.record();
	}

	protected void createPanel()
	{
		panel = new JPanel();
		panel.setLayout( new BoxLayout( panel, BoxLayout.PAGE_AXIS ) );

		boundedValues = new ArrayList<>();
		sliderPanels = new ArrayList<>();
		updateListener = new ChromaticShiftUpdateListener();

		final String[] xyz = { "X", "Y", "Z" };

		for ( int c = 0; c < numChannels; c++ )
		{
			for ( String axis : xyz )
			{
				createValueAndSlider( c, axis );
			}
		}
	}

	private void createValueAndSlider( int c, String axis )
	{
		final BoundedValue boundedValue
				= new BoundedValue(
				-200, // TODO: how much?
				200,
				0 );

		final SliderPanel sliderPanel = new SliderPanel(
				"Channel " + c + ", " + axis,
				boundedValue,
				1 );

		boundedValue.setUpdateListener( updateListener );

		boundedValues.add( boundedValue );
		sliderPanels.add( sliderPanel );
		panel.add( sliderPanel );
	}

	class ChromaticShiftUpdateListener implements BoundedValue.UpdateListener
	{
		private ArrayList< long[] > previousTranslations;

		@Override
		public synchronized void update()
		{
			shifts = getShiftsXYZT();

			if ( ! shiftsChanged( shifts ) ) return;

			updateSliders();

			final RandomAccessibleInterval< T > correctedRAI = channelShifter.getShiftedRai( shifts );

			outputImage = new Image( inputImage );
			outputImage.setRai( correctedRAI );
			outputImage.setName( inputImage.getName() + "-align" );

			viewer.replaceImage( outputImage, false, true );
		}

		private boolean shiftsChanged( ArrayList< long[] > translations )
		{
			if ( previousTranslations == null )
			{
				previousTranslations = translations;
				return true;
			}
			else
			{
				for ( int c = 0; c < numChannels; c++ )
					for ( int d = 0; d < 3; d++ )
						if ( translations.get( c )[ d ] != previousTranslations.get( c )[ d ] )
						{
							previousTranslations = translations;
							return true;
						}
			}

			previousTranslations = translations;
			return false;
		}

		private ArrayList< long[] > getShiftsXYZT()
		{
			final ArrayList< long[] > translationsXYZT = new ArrayList<>();
			int valueIndex = 0;
			for ( int c = 0; c < numChannels; c++ )
			{
				long[] translation = new long[ 4 ];

				for ( int d = 0; d < 3; d++ )
					translation[ d ] = boundedValues.get( valueIndex++ ).getCurrentValue();

				translationsXYZT.add( translation );
			}
			return translationsXYZT;
		}

		private void updateSliders()
		{
			int i = 0;
			for ( int c = 0; c < numChannels; c++ )
				for ( int d = 0; d < 3; d++ )
					sliderPanels.get( i++ ).update();
		}
	}
}
