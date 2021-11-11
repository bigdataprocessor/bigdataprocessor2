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
package de.embl.cba.bdp2.process.align.splitchip;

import bdv.tools.boundingbox.TransformedBox;
import bdv.tools.boundingbox.TransformedBoxOverlay;
import bdv.tools.brightness.SliderPanel;
import bdv.util.BoundedValue;
import bdv.util.ModifiableInterval;
import bdv.viewer.OverlayRenderer;
import bdv.viewer.ViewerPanel;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.process.align.channelshift.AlignChannelsDialog;
import de.embl.cba.bdp2.process.align.channelshift.RegionOptimiser;
import de.embl.cba.bdp2.dialog.AbstractProcessingDialog;
import de.embl.cba.bdp2.dialog.DisplaySettings;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.record.ScriptRecorder;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewer.ImageViewer;
import ij.Prefs;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.listeners.Listeners;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static de.embl.cba.bdp2.dialog.DialogUtils.setOutputViewerPosition;
import static de.embl.cba.bdp2.process.align.channelshift.RegionOptimiser.adjustModifiableInterval;


public class SplitChipDialog< R extends RealType< R > & NativeType< R > > extends AbstractProcessingDialog< R >
{
	public static final int X = 0;
	public static final int Y = 1;
	public static final int Z = 2;
	public static final int CHANNEL = 0;

	private Map< String, BoundedValue > boundedValues;
	private ArrayList< SliderPanel > sliderPanels;
	private SelectionUpdateListener updateListener;
	private ArrayList< ModifiableInterval > intervalsXYZ;
	private ImageViewer outputViewer;
	private int numChannelsAfterMerge;
	private ArrayList< OverlayRenderer > overlayRenderers;

	public SplitChipDialog( final ImageViewer< R > viewer )
	{
		this.viewer = viewer;
		this.inputImage = viewer.getImage();

		overlayRenderers = new ArrayList<>();

		initIntervals();
		showIntervalOverlays();
		showMerge();
		createPanel();
	}

	@Override
	protected void ok(){
		recordMacro();
		viewer.close();
		this.dispose();
	}

	@Override
	protected void cancel(){
		removeOverlays();
		outputViewer.close();
		this.dispose();
	}

	public void showIntervalOverlays()
	{
		for ( ModifiableInterval interval : intervalsXYZ )
		{
			addOverlayToViewer( interval );
		}

		viewer.repaint();
	}

	@Override
	protected void recordMacro()
	{
		final ScriptRecorder recorder = new ScriptRecorder( SplitChipCommand.COMMAND_FULL_NAME, inputImage, outputImage );
		ArrayList< long[] > regions = intervalsXYZAsMinDimLongs( intervalsXYZ, CHANNEL );
		String intervalsString = Utils.longsToDelimitedString( regions );
		Prefs.set( getImageJPrefsKey(), intervalsString );
		recorder.addCommandParameter( "intervalsString", intervalsString );

		// Image< R > alignChannelsSpitChip( Image< R > image, List< long[] > regions )
		recorder.setBDP2FunctionName( "alignChannelsSpitChip" );
		recorder.addAPIFunctionPrequelComment(  SplitChipCommand.COMMAND_NAME );
		recorder.addAPIFunctionParameter( regions );

		recorder.record();
	}

	private String getImageJPrefsKey()
	{
		return AlignChannelsDialog.class.getSimpleName() + "." + "Regions";
	}

	private static ArrayList< long[] > intervalsXYZAsMinDimLongs( ArrayList< ModifiableInterval > intervalsXYZ, int channel )
	{
		ArrayList< long[] > intervals = new ArrayList<>(  );
		for ( ModifiableInterval interval : intervalsXYZ )
		{
			final long[] longs = new long[ 5 ];
			longs[ 0 ] = interval.min( 0 );
			longs[ 1 ] = interval.min( 1 );
			longs[ 2 ] = interval.dimension( 0 );
			longs[ 3 ] = interval.dimension( 1 );
			longs[ 4 ] = channel;
			intervals.add( longs );
		}
		return intervals;
	}

	public void initIntervals()
	{
		intervalsXYZ = new ArrayList<>();
		final int margin = ( int ) ( viewer.getImage().getRai().dimension( 0 ) * 0.01 );
		for ( int c = 0; c < 2; c++ )
		{
			intervalsXYZ.add( createInterval( c, margin ) );
		}

		numChannelsAfterMerge = intervalsXYZ.size();
	}

	protected void createPanel()
	{
		panel = new JPanel();
		panel.setLayout( new BoxLayout( panel, BoxLayout.PAGE_AXIS ) );
		addRegionSliders();
	}

	/*
	 * TODO: currently not implemented due to missing BigStitcher dependency
	 * see branch: withPhaseCorrelation
	 */
	private void optimise()
	{
		final double[] shift = RegionOptimiser.optimiseIntervals(
				inputImage,
				intervalsXYZ );

		adjustModifiableInterval( shift, intervalsXYZ.get( 1 ) );

		for ( int d = 0; d < 2; d++ )
		{
			final int currentValue = boundedValues
					.get( getMinimumName( d, 1 ) )
					.getCurrentValue();

			boundedValues
					.get( getMinimumName( d, 1 ) )
					.setCurrentValue( ( int ) ( currentValue - shift[ d ] ) );
		}
	}

	private void showMerge()
	{
		createOutputImage();
		showOutputImage();
	}

	private void createOutputImage()
	{
		outputImage = SplitChipMerger.mergeIntervalsXYZ(
						inputImage,
						intervalsXYZ, // in the UI this contains 2 channels
						CHANNEL ); // TODO: Could be different channel?
		outputImage.setChannelNames( createMergedChannelNames() );
	}

	private String[] createMergedChannelNames()
	{
		final String channelName = inputImage.getChannelNames()[ CHANNEL ];
		final String[] mergedChannelNames = new String[ numChannelsAfterMerge ];
		for ( int c = 0; c < numChannelsAfterMerge; c++ )
		{
			mergedChannelNames[ c ] = channelName + "_region" + c;
		}
		return mergedChannelNames;
	}

	private void showOutputImage()
	{
		if ( outputViewer == null )
		{
			outputViewer = BigDataProcessor2.showImage( outputImage, false );

			setOutputViewerPosition( viewer, outputViewer );

			final DisplaySettings displaySettings = viewer.getDisplaySettings().get( CHANNEL );
			for ( int c = 0; c < numChannelsAfterMerge; c++ )
			{
				outputViewer.setDisplaySettings( displaySettings.getDisplayRangeMin(), displaySettings.getDisplayRangeMax(), null, c );
			}
		}
		else
		{
			outputViewer.replaceImage( outputImage, false, false );
		}
	}

	private void addRegionSliders()
	{
		sliderPanels = new ArrayList<>(  );
		boundedValues = new HashMap<>(  );
		updateListener = new SelectionUpdateListener( intervalsXYZ );

		for ( int c = 0; c < 2; c++)
			addMinimumSliders( c );

		for ( int d = 0; d < 2; d++ )
			addSpanSliders( d );
	}

	private void addSpanSliders( int d )
	{
		String name = getSpanName( d );
		boundedValues.put( name,
				new BoundedValue(
						0,
						getRangeMax( d ),
						getSpan( intervalsXYZ.get( 0 ), d ) ) );
		createPanel( name , boundedValues.get( name ) );
	}

	private void addMinimumSliders( int c )
	{
		for ( int d = 0; d < 2; d++ )
		{
			String name = getMinimumName( d, c );

			boundedValues.put( name, new BoundedValue(
							0,
							getRangeMax( d ),
							(int) intervalsXYZ.get( c ).min( d ) ) );

			createPanel( name, boundedValues.get( name ) );
		}
	}

	private int getRangeMax( int d )
	{
		final long span = viewer.getImage().getRai().dimension( d );
		return ( int ) span;
	}

	private int getSpan( ModifiableInterval interval, int d )
	{
		return ( int ) interval.dimension( d );
	}

	private ModifiableInterval createInterval( int c, int margin )
	{
		final RandomAccessibleInterval rai = viewer.getImage().getRai();

		final FinalInterval interval3D = getInitial3DInterval( rai, c, margin );

		ModifiableInterval modifiableInterval3D = new ModifiableInterval( interval3D );

		return modifiableInterval3D;
	}

	private void addOverlayToViewer( ModifiableInterval modifiableInterval3D )
	{
		final TransformedBoxOverlay transformedBoxOverlay =
				new TransformedBoxOverlay( new TransformedBox()
				{
					@Override
					public void getTransform( final AffineTransform3D transform )
					{
						viewer.getSourceTransform( transform );
					}

					@Override
					public Interval getInterval()
					{
						return modifiableInterval3D;
					}

				} );

		transformedBoxOverlay.boxDisplayMode().set( TransformedBoxOverlay.BoxDisplayMode.SECTION );

		final ViewerPanel viewerPanel = viewer.getBdvHandle().getViewerPanel();
		viewerPanel.getDisplay().addOverlayRenderer( transformedBoxOverlay );
		viewerPanel.addRenderTransformListener( transformedBoxOverlay );

		overlayRenderers.add( transformedBoxOverlay );
	}

	private void removeOverlays()
	{
		final Listeners< OverlayRenderer > overlays = viewer.getBdvHandle().getViewerPanel().getDisplay().overlays();

		for ( OverlayRenderer overlayRenderer : overlayRenderers )
		{
			overlays.remove( overlayRenderer );
		}
	}

	private FinalInterval getInitial3DInterval(
			RandomAccessibleInterval rai,
			int c,
			int margin )
	{
		final long[] min = new long[ 3 ];
		final long[] max = new long[ 3 ];
		int d;
		boolean couldUsePreviousChoice;

		String recentChoice = Prefs.get( getImageJPrefsKey(), null );

		if ( recentChoice != null )
		{
			couldUsePreviousChoice = true;

			// format: minX,minY,dimX,dimY
			String channelChoice = recentChoice.split( ";" )[ c ];

			for ( d = 0; d < 2; d++ )
			{
				min[ d ] = Integer.parseInt( channelChoice.split( "," )[ d ] );
				max[ d ] = min[ d ] + Integer.parseInt( channelChoice.split( "," )[ d + 2 ] ) - 1;

				if ( min[ d ] < rai.min( d ) ) couldUsePreviousChoice = false;
				if ( max[ d ] > rai.max( d ) ) couldUsePreviousChoice = false;
			}
		}
		else
		{
			couldUsePreviousChoice = false;
		}

		if ( ! couldUsePreviousChoice )
		{
			// set some defaults

			d = X;

			if ( c == 0 )
			{
				min[ d ] = rai.min( d ) + margin;
				max[ d ] = rai.max( d ) / 2 - margin;
			}

			if ( c == 1 )
			{
				min[ d ] = rai.max( d ) / 2 + margin;
				max[ d ] = rai.max( d ) - margin;
			}

			d = Y;

			min[ d ] = rai.min( d ) + margin ;
			max[ d ] = rai.max( d ) - margin;
		}

		min[ Z ] = 0;
		max[ Z ] = rai.dimension( Z );

		return new FinalInterval( min, max );
	}

	private String getMinimumName( int d, int c )
	{
		final String[] xy = { "X", "Y" };
		return "Channel " + c + ", Minimum " + xy[ d ] + " [Pixel]";
	}

	private String getSpanName( int d )
	{
		final String[] spanNames = { "Width [Pixel]", "Height [Pixel]" };
		return spanNames[ d ];
	}

	private void createPanel( String name, BoundedValue boundedValue )
	{
		final SliderPanel sliderPanel =
				new SliderPanel(
						name,
						boundedValue,
						1 );

		boundedValue.setUpdateListener( updateListener );
		sliderPanels.add( sliderPanel );
		panel.add( sliderPanel );
	}

	class SelectionUpdateListener implements BoundedValue.UpdateListener
	{
		private final ArrayList< ModifiableInterval > intervals;

		public SelectionUpdateListener( ArrayList< ModifiableInterval > intervals )
		{
			this.intervals = intervals;
		}

		@Override
		public synchronized void update()
		{
			updateSliders();

			for ( int c = 0; c < 2; c++ )
				updateInterval( c );

			showMerge();
			viewer.repaint();
		}

		public void updateInterval( int c )
		{
			final ModifiableInterval interval = intervals.get( c );
			
			final long[] min = new long[ 3 ];
			final long[] max = new long[ 3 ];

			for ( int d = 0; d < 2; d++ )
			{
				min[ d ] = boundedValues.get( getMinimumName( d, c ) ).getCurrentValue();
				final long span = boundedValues.get( getSpanName( d ) ).getCurrentValue();
				max[ d ] = ( min[ d ] + span - 1);
			}

			final RandomAccessibleInterval< R > rai = viewer.getImage().getRai();
			
			min[ Z ] = rai.min( Z );
			max[ Z ] = rai.max( Z );

			interval.set( new FinalInterval( min, max ) );
		}

		private void updateSliders()
		{
			for ( SliderPanel sliderPanel : sliderPanels )
					sliderPanel.update();
		}
	}

}
