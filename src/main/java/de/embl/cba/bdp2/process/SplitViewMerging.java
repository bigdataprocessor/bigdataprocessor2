package de.embl.cba.bdp2.process;

import bdv.tools.boundingbox.TransformedBox;
import bdv.tools.boundingbox.TransformedBoxOverlay;
import bdv.tools.brightness.SliderPanel;
import bdv.util.BoundedValue;
import bdv.util.ModifiableRealInterval;
import bdv.viewer.ViewerPanel;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imglib2.FinalRealInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.RealInterval;
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

	public static final String CX = "cx";
	public static final String CY = "cy";
	public static final String W = "w";
	public static final String H = "h";
	public static final int X = 0;
	public static final int Y = 1;
	public static final int Z = 2;

	private final BdvImageViewer< R > viewer;
	private Map< String, BoundedValue > boundedValues;
	private ArrayList< SliderPanel > sliderPanels;
	private SelectionUpdateListener updateListener;
	private JPanel panel;
	private ArrayList< RandomAccessibleInterval< R > > shiftedChannelRAIs;
	private int numChannels = 1;

	public SplitViewMerging( final BdvImageViewer< R > viewer )
	{
		this.viewer = viewer;

		final ModifiableRealInterval modifiableRealInterval = showRegionSelectionOverlay();

		showRegionSelectionDialog( modifiableRealInterval );

		//newImageViewer = createNewImageViewer( imageViewer );
	}

	private void showRegionSelectionDialog( ModifiableRealInterval modifiableRealInterval )
	{
		panel = new JPanel();
		panel.setLayout( new BoxLayout( panel, BoxLayout.PAGE_AXIS ) );
		sliderPanels = new ArrayList<>(  );
		boundedValues = new HashMap<>(  );

		updateListener = new SelectionUpdateListener( modifiableRealInterval );

		boundedValues.put( CX, new BoundedValue( 0, 1000, 500 ) );
		createPanel( CX , boundedValues.get( CX ) );

		boundedValues.put( CY, new BoundedValue( 0, 1000, 500 ) );
		createPanel( CY , boundedValues.get( CY ) );

		boundedValues.put( W, new BoundedValue( 0, 1000, 100 ) );
		createPanel( W , boundedValues.get( W ) );

		boundedValues.put( H, new BoundedValue( 0, 1000, 100 ) );
		createPanel( H , boundedValues.get( H ) );

		showFrame( panel );
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
		private final ModifiableRealInterval interval;

		public SelectionUpdateListener( ModifiableRealInterval interval )
		{
			this.interval = interval;
		}

		@Override
		public synchronized void update()
		{
			updateSliders();

			final double[] min = new double[ 3 ];
			final double[] max = new double[ 3 ];

			min[ X ] = boundedValues.get( CX ).getCurrentValue()
					- boundedValues.get( W ).getCurrentValue() / 2.0 ;

			max[ X ] = boundedValues.get( CX ).getCurrentValue()
					+ boundedValues.get( W ).getCurrentValue() / 2.0 ;

			min[ Y ] = boundedValues.get( CY ).getCurrentValue()
					- boundedValues.get( H ).getCurrentValue() / 2.0 ;

			max[ Y ] = boundedValues.get( CY ).getCurrentValue()
					+ boundedValues.get( H ).getCurrentValue() / 2.0 ;

			min[ Z ] = interval.realMin( Z );

			max[ Z ] = interval.realMax( Z );


			interval.set( new FinalRealInterval( min, max ) );


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

		private boolean isTranslationsChanged( ArrayList< long[] > translations )
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

		private ArrayList< long[] > getTranslations()
		{
			final ArrayList< long[] > translations = new ArrayList<>();
			int valueIndex = 0;
			for ( int c = 0; c < numChannels; c++ )
			{
				long[] translation = new long[ 4 ];

				for ( int d = 0; d < 3; d++ )
					translation[ d ] = boundedValues.get( valueIndex++ ).getCurrentValue();

				translations.add( translation );
			}
			return translations;
		}

		private void updateSliders()
		{
			for ( SliderPanel sliderPanel : sliderPanels )
					sliderPanel.update();
		}
	}

}
