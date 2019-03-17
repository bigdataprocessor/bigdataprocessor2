package de.embl.cba.bdp2.process;

import bdv.tools.brightness.SliderPanel;
import bdv.util.BoundedValue;
import de.embl.cba.bdp2.ui.BdvMenus;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.viewers.ImageViewer;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.Volatile;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ChromaticShiftCorrectionView< R extends RealType< R > & NativeType< R > > extends JFrame
{

	private final ImageViewer< R > imageViewer;
	private final RandomAccessibleInterval< Volatile< R > > rai;
	private ArrayList< BoundedValue > boundedValues;
	private ArrayList< SliderPanel > sliderPanels;
	private final ImageViewer newImageViewer;
	private final long numChannels;
	private final ArrayList< RandomAccessibleInterval< Volatile< R > > > channelRAIs;
	private RandomAccessibleInterval< Volatile< R > > correctedRai;
	private ChromaticShiftUpdateListener updateListener;
	private JPanel panel;
	private ArrayList< RandomAccessibleInterval< Volatile< R >> > shiftedChannelRAIs;

	public ChromaticShiftCorrectionView( final ImageViewer< R > imageViewer  )
	{
		this.imageViewer = imageViewer;
		this.rai = imageViewer.getVolatileRai();
		numChannels = rai.dimension( DimensionOrder.C );

		channelRAIs = getChannelRAIs();
		shiftedChannelRAIs = channelRAIs;
		setCorrectedRai();

		newImageViewer = createNewImageViewer( imageViewer );

		showChromaticShiftCorrectionDialog();
	}

	private ImageViewer< R > createNewImageViewer( ImageViewer< R > imageViewer )
	{
		ImageViewer newImageViewer = imageViewer.newImageViewer();

		newImageViewer.show(
				correctedRai,
				"chromatic shift corrected view",
				imageViewer.getVoxelSize(),
				imageViewer.getCalibrationUnit(),
				true);

		newImageViewer.addMenus( new BdvMenus() );

		return newImageViewer;
	}

	private void setCorrectedRai()
	{
		final RandomAccessibleInterval< Volatile< R > > stack = Views.stack( shiftedChannelRAIs );

		correctedRai = Views.permute( stack, DimensionOrder.C, DimensionOrder.T );
	}

	private ArrayList< RandomAccessibleInterval< Volatile< R > > > getChannelRAIs()
	{
		ArrayList< RandomAccessibleInterval< Volatile< R > > > channelRais = new ArrayList<>();

		for ( int c = 0; c < numChannels; c++ )
			channelRais.add( Views.hyperSlice( rai, DimensionOrder.C, c ) );

		return channelRais;
	}

	private double[] getBinnedVoxelSize( long[] span, double[] voxelSize )
	{
		final double[] newVoxelSize = new double[ voxelSize.length ];

		for ( int d = 0; d < 3; d++ )
			newVoxelSize[ d ] = voxelSize[ d ] * ( 2 * span[ d ] + 1 );

		return newVoxelSize;
	}

	private void showChromaticShiftCorrectionDialog()
	{
		panel = new JPanel();
		panel.setLayout( new BoxLayout( panel, BoxLayout.PAGE_AXIS ) );

		boundedValues = new ArrayList<>();
		sliderPanels = new ArrayList<>();
		updateListener = new ChromaticShiftUpdateListener();

		final String[] xyz = { "X", "Y", "Z" };

		for ( int c = 0; c < numChannels; c++ )
			for ( String axis : xyz )
				createValueAndSlider( c, axis );

		showFrame( panel );
	}

	private void showFrame( JPanel panel )
	{
		final JFrame frame = new JFrame( "Chromatic Shift Correction" );
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

	private void createValueAndSlider( int c, String axis )
	{
		final BoundedValue boundedValue
				= new BoundedValue(
				0,
				500, // TODO: how much?
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
			final ArrayList< long[] > translations = getTranslations();

			if ( ! isTranslationsChanged( translations ) ) return;

			updateSliders();

			shiftedChannelRAIs = new ArrayList<>();
			for ( int c = 0; c < numChannels; c++ )
				shiftedChannelRAIs.add( Views.translate( channelRAIs.get( c ), translations.get( c ) ) );

			Interval intersect = shiftedChannelRAIs.get( 0 );
			for ( int c = 1; c < numChannels; c++ )
				intersect = Intervals.intersect( intersect, shiftedChannelRAIs.get( c ) );

			shiftedChannelRAIs = getCroppedRAIs( intersect );
			setCorrectedRai();
			showCorrectedRai();
		}

		private ArrayList< RandomAccessibleInterval< Volatile< R > > > getCroppedRAIs( Interval intersect )
		{
			final ArrayList< RandomAccessibleInterval< Volatile< R > > > cropped
					= new ArrayList<>();

			for ( int c = 0; c < numChannels; c++ )
			{
				final IntervalView< Volatile< R > > crop
						= Views.interval( shiftedChannelRAIs.get( c ), intersect );
				cropped.add(  crop );
			}
			return cropped;
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
			int i = 0;
			for ( int c = 0; c < numChannels; c++ )
				for ( int d = 0; d < 3; d++ )
					sliderPanels.get( i++ ).update();
		}
	}

	private void showCorrectedRai()
	{
		newImageViewer.show(
				correctedRai,
				imageViewer.getImageName(),
				imageViewer.getVoxelSize(),
				imageViewer.getCalibrationUnit(),
				true );
	}


}
