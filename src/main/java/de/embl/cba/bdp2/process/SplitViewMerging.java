package de.embl.cba.bdp2.process;

import bdv.tools.boundingbox.TransformedBox;
import bdv.tools.boundingbox.TransformedBoxOverlay;
import bdv.tools.brightness.SliderPanelDouble;
import bdv.util.BoundedValueDouble;
import bdv.util.ModifiableRealInterval;
import bdv.viewer.ViewerPanel;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imglib2.FinalRealInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SplitViewMerging < R extends RealType< R > & NativeType< R > >
{

	public static final String[] CX = new String[]{"Channel 0, Centre X", "Channel 1, Centre X" };
	public static final String[] CY = new String[]{"Channel 0, Centre Y", "Channel 1, Centre Y" };
	public static final String W = "Width";
	public static final String H = "Height";
	public static final int X = 0;
	public static final int Y = 1;
	public static final int Z = 2;

	private final BdvImageViewer< R > viewer;
	private Map< String, BoundedValueDouble > boundedValues;
	private ArrayList< SliderPanelDouble > sliderPanels;
	private SelectionUpdateListener updateListener;
	private JPanel panel;
	private ArrayList< RandomAccessibleInterval< R > > shiftedChannelRAIs;
	private int numChannels = 1;

	public SplitViewMerging( final BdvImageViewer< R > viewer )
	{
		this.viewer = viewer;

		final ArrayList< ModifiableRealInterval > intervals = new ArrayList<>();
		intervals.add( showRegionSelectionOverlay() );
		intervals.add( showRegionSelectionOverlay() );

		showRegionSelectionDialog( intervals );

	}

	private void showRegionSelectionDialog( ArrayList< ModifiableRealInterval > intervals )
	{
		panel = new JPanel();
		panel.setLayout( new BoxLayout( panel, BoxLayout.PAGE_AXIS ) );
		sliderPanels = new ArrayList<>(  );
		boundedValues = new HashMap<>(  );

		updateListener = new SelectionUpdateListener( intervals );

		for ( int d = 0; d < 2; d++ )
		{
			boundedValues.put( CX[ d ], new BoundedValueDouble(
					0.0,
					getRangeMax( X ),
					getCenter( intervals.get( d ), X ) ) );
			createPanel( CX[ d ], boundedValues.get( CX[ d ] ) );

			boundedValues.put( CY[ d ],
					new BoundedValueDouble(
							0,
							getRangeMax( Y ),
							getCenter( intervals.get( d ), Y ) ) );
			createPanel( CY[ d ], boundedValues.get( CY[ d ] ) );
		}

		boundedValues.put( W,
				new BoundedValueDouble(
						0,
						getRangeMax( X ),
						getSpan( intervals.get( 0 ), X ) ) );
		createPanel( W , boundedValues.get( W ) );

		boundedValues.put( H,
				new BoundedValueDouble(
						0,
						getRangeMax( Y ),
						getSpan( intervals.get( 0 ), Y ) ) );
		createPanel( H , boundedValues.get( H ) );

		showFrame( panel );
	}

	private double getRangeMax( int d )
	{
		return viewer.getImage().getRai().dimension( d )
				* viewer.getImage().getVoxelSpacing()[ d ];
	}

	private double getCenter( ModifiableRealInterval interval, int d )
	{
		return ( interval.realMax( d ) - interval.realMin( d ) ) / 2 + interval.realMin( d );
	}

	private double getSpan( ModifiableRealInterval interval, int d )
	{
		return ( interval.realMax( d ) - interval.realMin( d ) );
	}

	private ModifiableRealInterval showRegionSelectionOverlay()
	{
		final RandomAccessibleInterval rai = viewer.getImage().getRai();
		final double[] min = new double[ 3 ];
		final double[] max = new double[ 3 ];

		final double[] voxelSpacing = viewer.getImage().getVoxelSpacing();

		for (int d = 0; d < 3; d++) {
			min[d] = (int) (rai.min(d) * voxelSpacing[d]);
			max[d] = (int) (0.7 * rai.max(d) * voxelSpacing[d]);
		}

		final FinalRealInterval interval = new FinalRealInterval( min, max );

		ModifiableRealInterval modifiableRealInterval = new ModifiableRealInterval( interval );

		final TransformedBoxOverlay transformedBoxOverlay =
				new TransformedBoxOverlay( new TransformedBox()
				{
					@Override
					public void getTransform( final AffineTransform3D transform )
					{
						viewer.getBdvStackSource().getSources().get( 0 )
								.getSpimSource().getSourceTransform( 0, 0, transform );
					}

					@Override
					public Interval getInterval()
					{
						return Intervals.largestContainedInterval( modifiableRealInterval );
					}


				} );

		transformedBoxOverlay.boxDisplayMode().set( TransformedBoxOverlay.BoxDisplayMode.SECTION );

		final ViewerPanel viewerPanel = viewer.getBdvStackSource().getBdvHandle().getViewerPanel();
		viewerPanel.getDisplay().addOverlayRenderer(transformedBoxOverlay);
		viewerPanel.addRenderTransformListener(transformedBoxOverlay);

		return modifiableRealInterval;

	}

	private void showFrame( JPanel panel )
	{
		final JFrame frame = new JFrame( "Region Selection" );
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

		frame.setContentPane( panel );
		frame.setBounds(
				MouseInfo.getPointerInfo().getLocation().x,
				MouseInfo.getPointerInfo().getLocation().y,
				120, 10);
		frame.setResizable( false );
		frame.pack();
		frame.setVisible( true );
	}

	private void createPanel( String name, BoundedValueDouble boundedValue )
	{

		final SliderPanelDouble sliderPanel =
				new SliderPanelDouble(
						name,
						boundedValue,
						1 );

		boundedValue.setUpdateListener( updateListener );
		sliderPanels.add( sliderPanel );
		panel.add( sliderPanel );
	}

	class SelectionUpdateListener implements BoundedValueDouble.UpdateListener
	{
		private final ArrayList< ModifiableRealInterval > intervals;

		public SelectionUpdateListener( ArrayList< ModifiableRealInterval > intervals )
		{
			this.intervals = intervals;
		}

		@Override
		public synchronized void update()
		{
			updateSliders();

			updateInterval( 0 );
			updateInterval( 1 );

			viewer.repaint();

			/*final ArrayList< long[] > translations = getTranslations();

			if ( ! isTranslationsChanged( translations ) ) return;

			updateSliders();

			shiftedChannelRAIs = new ArrayList<>();
			for ( int c = 0; c < numChannels; c++ )
				shiftedChannelRAIs.add( Views.translate( channelRAIs.get( c ), translations.get( c ) ) );

			Interval intersect = shiftedChannelRAIs.get( 0 );
			for ( int c = 1; c < numChannels; c++ )
				intersect = Intervals.intersect( intersect, shiftedChannelRAIs.get( c ) );

			final ArrayList< RandomAccessibleInterval< T > > cropped = new ArrayList<>();
			for ( int c = 0; c < numChannels; c++ )
			{
				final IntervalView< T > crop = Views.interval( shiftedChannelRAIs.get( c ), intersect );
				cropped.add(  crop );
			}

			shiftedChannelRAIs = cropped;*/

		}

		public void updateInterval( int c )
		{
			final ModifiableRealInterval interval = intervals.get( c );

			final double[] min = new double[ 3 ];
			final double[] max = new double[ 3 ];

			min[ X ] = boundedValues.get( CX[ c ] ).getCurrentValue()
					- boundedValues.get( W ).getCurrentValue() / 2.0 ;

			max[ X ] = boundedValues.get( CX[ c ] ).getCurrentValue()
					+ boundedValues.get( W ).getCurrentValue() / 2.0 ;

			min[ Y ] = boundedValues.get( CY[ c ] ).getCurrentValue()
					- boundedValues.get( H ).getCurrentValue() / 2.0 ;

			max[ Y ] = boundedValues.get( CY[ c ] ).getCurrentValue()
					+ boundedValues.get( H ).getCurrentValue() / 2.0 ;

			min[ Z ] = interval.realMin( Z );
			max[ Z ] = interval.realMax( Z );


//			final AffineTransform3D transform = new AffineTransform3D();
//			viewer.getBdvStackSource().getSources().get( 0 )
//					.getSpimSource().getSourceTransform( 0, 0, transform );
			final double[] voxelSpacing = viewer.getImage().getVoxelSpacing();

			for ( int d = 0; d < 2; d++ )
			{
				min[ d ] /= voxelSpacing[ d ];
				max[ d ] /= voxelSpacing[ d ];
			}

			interval.set( new FinalRealInterval( min, max ) );
		}

//		private boolean isTranslationsChanged( ArrayList< long[] > translations )
//		{
//			if ( previousTranslations == null )
//			{
//				previousTranslations = translations;
//				return true;
//			}
//			else
//			{
//				for ( int c = 0; c < numChannels; c++ )
//					for ( int d = 0; d < 3; d++ )
//						if ( translations.get( c )[ d ] != previousTranslations.get( c )[ d ] )
//						{
//							previousTranslations = translations;
//							return true;
//						}
//			}
//
//			previousTranslations = translations;
//			return false;
//		}


		private void updateSliders()
		{
			for ( SliderPanelDouble sliderPanel : sliderPanels )
					sliderPanel.update();
		}
	}

}
