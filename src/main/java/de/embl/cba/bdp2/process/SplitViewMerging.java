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
	public static final int X = 0;
	public static final int Y = 1;
	public static final int Z = 2;

	private final BdvImageViewer< R > viewer;
	private Map< String, BoundedValueDouble > boundedValues;
	private ArrayList< SliderPanelDouble > sliderPanels;
	private SelectionUpdateListener updateListener;
	private JPanel panel;

	public SplitViewMerging( final BdvImageViewer< R > viewer )
	{
		this.viewer = viewer;

		final ArrayList< ModifiableRealInterval > intervals = new ArrayList<>();

		final int margin = ( int ) ( viewer.getImage().getRai().dimension( 0 ) * 0.01 );

		for ( int c = 0; c < 2; c++ )
			intervals.add( showRegionSelectionOverlay( c, margin ) );

		showRegionSelectionDialog( intervals );
	}

	private void showRegionSelectionDialog( ArrayList< ModifiableRealInterval > intervals )
	{
		panel = new JPanel();
		panel.setLayout( new BoxLayout( panel, BoxLayout.PAGE_AXIS ) );
		sliderPanels = new ArrayList<>(  );
		boundedValues = new HashMap<>(  );

		updateListener = new SelectionUpdateListener( intervals );

		for ( int c = 0; c < 2; c++)
			for ( int d = 0; d < 2; d++ )
			{
				String name = getCenterName( d, c );
				final double center = getCenter( intervals.get( c ), d );
				boundedValues.put( name, new BoundedValueDouble(
						0.0,
						getRangeMax( d ),
						center ) );
				createPanel( name, boundedValues.get( name ) );
			}

		for ( int d = 0; d < 2; d++ )
		{
			String name = getSpanName( d );
			boundedValues.put( name,
					new BoundedValueDouble(
							0,
							getRangeMax( d ),
							getSpan( intervals.get( 0 ), d ) ) );
			createPanel( name , boundedValues.get( name ) );
		}

		showFrame( panel );
	}

	private double getRangeMax( int d )
	{
		final double span =
				viewer.getImage().getRai().dimension( d )
				* viewer.getImage().getVoxelSpacing()[ d ];

		return span;
	}

	private double getCenter( ModifiableRealInterval interval, int d )
	{
		return ( interval.realMax( d ) - interval.realMin( d ) ) / 2.0 + interval.realMin( d );
	}

	private double getSpan( ModifiableRealInterval interval, int d )
	{
		return ( interval.realMax( d ) - interval.realMin( d ) );
	}

	private ModifiableRealInterval showRegionSelectionOverlay( int c, int margin )
	{
		final RandomAccessibleInterval rai = viewer.getImage().getRai();
		final double[] min = new double[ 3 ];
		final double[] max = new double[ 3 ];

		final double[] voxelSpacing = viewer.getImage().getVoxelSpacing();

		int d = X;

		if ( c == 0 )
		{
			min[ d ] = rai.min( d ) * voxelSpacing[ d ] + margin * voxelSpacing[ d ];
			max[ d ] = rai.max( d ) * voxelSpacing[ d ] / 2 - margin * voxelSpacing[ d ];
		}

		if ( c == 1 )
		{
			min[ d ] = rai.max( d ) * voxelSpacing[ d ] / 2 + margin * voxelSpacing[ d ];
			max[ d ] = rai.max( d ) * voxelSpacing[ d ] - margin * voxelSpacing[ d ];
		}

		d = Y;

		min[ d ] = rai.min( d ) * voxelSpacing[ d ] + margin * voxelSpacing[ d ] ;
		max[ d ] = rai.max( d ) * voxelSpacing[ d ] - margin * voxelSpacing[ d ];

		d = Z;

		min[ d ] = 0;
		max[ d ] = rai.dimension( d ) * voxelSpacing[ d ];

		final FinalRealInterval interval = new FinalRealInterval( min, max );

		ModifiableRealInterval modifiableRealInterval = new ModifiableRealInterval( interval );

		final TransformedBoxOverlay transformedBoxOverlay =
				new TransformedBoxOverlay( new TransformedBox()
				{
					@Override
					public void getTransform( final AffineTransform3D transform )
					{
						transform.set( new AffineTransform3D() );
//						viewer.getBdvStackSource().getSources().get( 0 )
//								.getSpimSource().getSourceTransform( 0, 0, transform );
					}

					@Override
					public Interval getInterval()
					{
						final Interval interval = Intervals.largestContainedInterval( modifiableRealInterval );
						return interval;
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

	private String getCenterName( int d, int c )
	{
		final String[] xy = { "X", "Y" };
		return "Channel " + c + ", Centre " + xy[ d ];
	}

	private String getSpanName( int d )
	{
		final String[] spanNames = { "Width", "Height" };
		return spanNames[ d ];
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

			for ( int c = 0; c < 2; c++ )
				updateInterval( c );

			viewer.repaint();
		}

		public void updateInterval( int c )
		{
			final ModifiableRealInterval interval = intervals.get( c );

			// get the new interval in real units
			//
			final double[] min = new double[ 3 ];
			final double[] max = new double[ 3 ];

			for ( int d = 0; d < 2; d++ )
			{
				final double centre = boundedValues.get( getCenterName( d, c ) ).getCurrentValue();
				final double span = boundedValues.get( getSpanName( d ) ).getCurrentValue();
				min[ d ] = centre - span / 2.0;
				max[ d ] = centre + span / 2.0;
			}

			final RandomAccessibleInterval< R > rai = viewer.getImage().getRai();
			final double[] voxelSpacing = viewer.getImage().getVoxelSpacing();

			min[ Z ] = rai.min( Z ) * voxelSpacing[ Z ];
			max[ Z ] = rai.max( Z ) * voxelSpacing[ Z ];

			// transform the interval to voxel units for drawing the overlay
			//
//			final AffineTransform3D transform = new AffineTransform3D();
//			viewer.getBdvStackSource().getSources().get( 0 )
//					.getSpimSource().getSourceTransform( 0, 0, transform );
//
//			final double[] minVoxels = new double[ 3 ];
//			transform.inverse().apply( min, minVoxels );
//
//			final double[] maxVoxels = new double[ 3 ];
//			transform.inverse().apply( max, maxVoxels );

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
