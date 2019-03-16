package de.embl.cba.bdp2.process;

import bdv.tools.brightness.SliderPanel;
import bdv.util.BoundedValue;
import de.embl.cba.bdp2.logging.ImageJLogger;
import de.embl.cba.bdp2.ui.BdvMenus;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.ImageViewer;
import de.embl.cba.lazyalgorithm.LazyDownsampler;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ChromaticShiftCorrectionView< T extends RealType< T > & NativeType< T > > extends JFrame
{

	private final ImageViewer< T > imageViewer;
	private final RandomAccessibleInterval< T > rai;
	private ArrayList< BoundedValue > boundedValues;
	private ArrayList< SliderPanel > sliderPanels;
	private final ImageViewer newImageViewer;
	private final long numChannels;
	private final ArrayList< RandomAccessibleInterval< T > > channelRAIs;
	private RandomAccessibleInterval< T > correctedRai;
	private UpdateListener updateListener;
	private JPanel panel;
	private ArrayList< RandomAccessibleInterval< T > > correctedChannelRAIs;

	public ChromaticShiftCorrectionView( final ImageViewer< T > imageViewer  )
	{
		this.imageViewer = imageViewer;
		this.rai = imageViewer.getRai();
		numChannels = rai.dimension( DimensionOrder.C );

		channelRAIs = getChannelRAIs();
		correctedChannelRAIs = channelRAIs;
		setCorrectedRai();

		newImageViewer = createNewImageViewer( imageViewer );

		showChromaticShiftCorrectionDialog();
	}

	private ImageViewer< T > createNewImageViewer( ImageViewer< T > imageViewer )
	{
		ImageViewer newImageViewer = imageViewer.newImageViewer();

		newImageViewer.show(
				correctedRai,
				"chromatic shift corrected view",
				imageViewer.getVoxelSize(),
				imageViewer.getCalibrationUnit(),
				true);

		newImageViewer.addMenus( new BdvMenus() );

		return imageViewer;
	}

	private void setCorrectedRai()
	{
		correctedRai = Views.permute(
				Views.stack( correctedChannelRAIs ),
				DimensionOrder.C, DimensionOrder.T );
	}

	private ArrayList< RandomAccessibleInterval< T > > getChannelRAIs()
	{
		ArrayList< RandomAccessibleInterval< T > > channelRais = new ArrayList<>();

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
		updateListener = new UpdateListener();

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
				50, // TODO: how much?
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

	class UpdateListener implements BoundedValue.UpdateListener
	{

		private ArrayList< long[] > previousTranslations;

		@Override
		public synchronized void update()
		{
			correctedChannelRAIs = new ArrayList<>(  );

			final ArrayList< long[] > translations = getTranslations();

			if ( ! isTranslationsChanged( translations ) ) return;

			previousTranslations = translations;

			updateSliders();

			for ( int c = 0; c < numChannels; c++ )
				correctedChannelRAIs.add( Views.translate( channelRAIs.get( c ), translations.get( c ) ) );

			Interval intersect = correctedChannelRAIs.get( 0 );
			for ( int c = 1; c < numChannels; c++ )
				intersect = Intervals.intersect( intersect, correctedChannelRAIs.get( c ) );

			final ArrayList< RandomAccessibleInterval< T > > cropped = new ArrayList<>();
			for ( int c = 0; c < numChannels; c++ )
				cropped.add( Views.interval( correctedChannelRAIs.get( c ), intersect ) );

			correctedChannelRAIs = cropped;
			setCorrectedRai();
			showCorrectedRai();
		}

		private boolean isTranslationsChanged( ArrayList< long[] > translations )
		{

			if ( previousTranslations != null )
				for ( int c = 0; c < numChannels; c++ )
					for ( int d = 0; d < 3; d++ )
						if ( translations.get( c )[ d ] != previousTranslations.get( c )[ d ] )
							return true;

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
