package de.embl.cba.bdp2.process.splitviewmerge;

import bdv.tools.boundingbox.TransformedBox;
import bdv.tools.boundingbox.TransformedBoxOverlay;
import bdv.tools.brightness.SliderPanel;
import bdv.util.BoundedValue;
import bdv.util.ModifiableInterval;
import bdv.viewer.ViewerPanel;
import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import de.embl.cba.bdp2.viewers.ImageViewer;
import net.imglib2.*;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static de.embl.cba.bdp2.process.splitviewmerge.RegionOptimiser.adjustModifiableInterval;

public class SplitViewMergingDialog< R extends RealType< R > & NativeType< R > >
{
	public static final int X = 0;
	public static final int Y = 1;
	public static final int Z = 2;

	private final BdvImageViewer< R > viewer;
	private Map< String, BoundedValue > boundedValues;
	private ArrayList< SliderPanel > sliderPanels;
	private SelectionUpdateListener updateListener;
	private JPanel panel;
	private ImageViewer newImageViewer;
	private ArrayList< ModifiableInterval > intervals3D;
	private Image< R > image;

	public SplitViewMergingDialog( final BdvImageViewer< R > viewer )
	{
		this.viewer = viewer;
		this.image = viewer.getImage();

		initSelectedRegions();
		showRegionSelectionDialog();
	}

	public void initSelectedRegions( )
	{
		intervals3D = new ArrayList<>();
		final int margin = ( int ) ( viewer.getImage().getRai().dimension( 0 ) * 0.01 );
		for ( int c = 0; c < 2; c++ )
			intervals3D.add( showRegionSelectionOverlay( c, margin ) );
	}


	private void showRegionSelectionDialog( )
	{
		panel = new JPanel();
		panel.setLayout( new BoxLayout( panel, BoxLayout.PAGE_AXIS ) );

		addRegionSliders();

		final JButton showMerge = new JButton( "Show/Update Merge" );
		panel.add( showMerge );
		showMerge.addActionListener( e -> {
			showOrUpdateMerge( );
		} );


//		final JButton optimise = new JButton( "Optimise Region Centres" );
//		panel.add( optimise );
//		optimise.addActionListener( e -> {
//			optimise();
//			showOrUpdateMerge();
//		} );


		showFrame( panel );
	}

	private void optimise()
	{
		final double[] shift = RegionOptimiser.optimiseIntervals(
				image,
				intervals3D );

		adjustModifiableInterval( shift, intervals3D.get( 1 ) );

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

	private void showOrUpdateMerge( )
	{
		final RandomAccessibleInterval< R > merge =
				SplitViewMerger.merge(
						image.getRai(),
						intervals3D );

		if ( newImageViewer == null )
			newImageViewer = viewer.newImageViewer();

		final Image< R > image = new Image<>(
				merge,
				this.image.getName() + "_merge",
				this.image.getVoxelSpacing(),
				this.image.getVoxelUnit()
		);

		newImageViewer.show( image, true );
	}

	private void addRegionSliders( )
	{
		sliderPanels = new ArrayList<>(  );
		boundedValues = new HashMap<>(  );
		updateListener = new SelectionUpdateListener( intervals3D );

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
						getSpan( intervals3D.get( 0 ), d ) ) );
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
							(int) intervals3D.get( c ).min( d ) ) );

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

	private ModifiableInterval showRegionSelectionOverlay( int c, int margin )
	{
		final RandomAccessibleInterval rai = viewer.getImage().getRai();

		final FinalInterval interval3D = getInitial3DInterval( rai, c, margin );

		ModifiableInterval modifiableInterval3D = new ModifiableInterval( interval3D );

		final TransformedBoxOverlay transformedBoxOverlay =
				new TransformedBoxOverlay( new TransformedBox()
				{
					@Override
					public void getTransform( final AffineTransform3D transform )
					{
						viewer.getBdvStackSource()
								.getSources().get( 0 )
								.getSpimSource()
								.getSourceTransform( 0, 0, transform );
					}

					@Override
					public Interval getInterval()
					{
						return modifiableInterval3D;
					}

				} );

		transformedBoxOverlay.boxDisplayMode().set(
				TransformedBoxOverlay.BoxDisplayMode.SECTION );

		final ViewerPanel viewerPanel = viewer.getBdvStackSource()
				.getBdvHandle().getViewerPanel();
		viewerPanel.getDisplay().addOverlayRenderer(transformedBoxOverlay);
		viewerPanel.addRenderTransformListener(transformedBoxOverlay);

		return modifiableInterval3D;

	}

	private FinalInterval getInitial3DInterval(
			RandomAccessibleInterval rai,
			int c,
			int margin )
	{
		final long[] min = new long[ 3 ];
		final long[] max = new long[ 3 ];
		
		int d = X;

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

		d = Z;

		min[ d ] = 0;
		max[ d ] = rai.dimension( d );

		return new FinalInterval( min, max );
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
