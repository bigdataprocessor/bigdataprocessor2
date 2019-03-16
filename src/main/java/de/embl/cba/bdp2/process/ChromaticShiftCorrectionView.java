package de.embl.cba.bdp2.process;

import bdv.tools.brightness.SliderPanel;
import bdv.util.BoundedValue;
import de.embl.cba.bdp2.logging.ImageJLogger;
import de.embl.cba.bdp2.ui.BdvMenus;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.ImageViewer;
import de.embl.cba.lazyalgorithm.LazyDownsampler;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ChromaticShiftCorrectionView< T extends RealType< T > & NativeType< T > > extends JFrame
{

	private final ImageViewer< T > imageViewer;
	private ArrayList< BoundedValue > boundedValues;
	private ArrayList< SliderPanel > sliderPanels;
	private final ImageViewer newImageViewer;

	public ChromaticShiftCorrectionView( final ImageViewer< T > imageViewer  )
	{
		this.imageViewer = imageViewer;

		final RandomAccessibleInterval< T > rai = imageViewer.getRai();

		long[] span = new long[]{0,0,0,0,0};

		final RandomAccessibleInterval< T > downSampledView =
				new LazyDownsampler<>( rai, span ).getDownsampledView();

		newImageViewer = imageViewer.newImageViewer();

		final double[] originalVoxelSize = imageViewer.getVoxelSize();

		newImageViewer.show(
				downSampledView,
				"binned view",
				getBinnedVoxelSize( span, originalVoxelSize ),
				imageViewer.getCalibrationUnit(),
				true);

		ImageJLogger.info( "Binned view size [GB]: "
				+ Utils.getSizeGB( downSampledView ) );

		newImageViewer.addMenus( new BdvMenus() );

		showChromaticShiftCorrectionDialog( rai, originalVoxelSize, newImageViewer, span );

	}

	private double[] getBinnedVoxelSize( long[] span, double[] voxelSize )
	{
		final double[] newVoxelSize = new double[ voxelSize.length ];

		for ( int d = 0; d < 3; d++ )
			newVoxelSize[ d ] = voxelSize[ d ] * ( 2 * span[ d ] + 1 );

		return newVoxelSize;
	}

	private void showChromaticShiftCorrectionDialog(
			RandomAccessibleInterval< T > rai,
			double[] originalVoxelSize,
			ImageViewer imageViewer,
			long[] span )
	{
		JPanel panel = new JPanel();
		panel.setLayout( new BoxLayout( panel, BoxLayout.PAGE_AXIS ) );

		boundedValues = new ArrayList<>();
		sliderPanels = new ArrayList<>();

		final long numChannels = rai.dimension( DimensionOrder.C );

		final String[] xyz = { "X", "Y", "Z" };

		for ( int c = 0; c < numChannels; c++ )
		{
			for ( String axis : xyz )
			{
				addValueAndSlider( c, axis );
			}
		}



		final UpdateListener updateListener = new UpdateListener();

		for ( int d = 0; d < 3; d++ )
		{
			boundedValues.get( d ).setUpdateListener( updateListener );
			panel.add( sliderPanels.get( d ) );
		}

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

	private void addValueAndSlider( int c, String axis )
	{
		final BoundedValue boundedValue
				= new BoundedValue(
				0,
				50, // TODO
				0 );

		boundedValues.add( boundedValue );

		sliderPanels.add(
				new SliderPanel(
						"Channel " + c + ", " + axis,
						boundedValue,
						1 ) );
	}

	class UpdateListener implements BoundedValue.UpdateListener
	{
		private long[] previousSpan;

		@Override
		public synchronized void update()
		{
			final long[] span = new long[ 5 ];

			for ( int d = 0; d < 3; d++ )
				span[ d ] = ( boundedValues.get( d ).getCurrentValue() - 1 ) / 2;

			boolean spanChanged = false;
			if ( previousSpan != null )
			{
				for ( int d = 0; d < 3; d++ )
				{
					if ( span[ d ] != previousSpan[ d ] )
					{
						sliderPanels.get( d ).update();
						previousSpan[ d ] = span[ d ];
						spanChanged = true;
					}
				}
			}
			else
			{
				spanChanged = true;
			}

			if ( ! spanChanged ) return;

			final RandomAccessibleInterval< T > downSampleView =
					new LazyDownsampler<>( rai, span ).getDownsampledView();

			final double[] binnedVoxelSize = getBinnedVoxelSize( span, originalVoxelSize );

			final AffineTransform3D transform3D = newImageViewer.getViewerTransform().copy();

			newImageViewer.show(
					downSampleView,
					imageViewer.getImageName(),
					binnedVoxelSize,
					imageViewer.getCalibrationUnit(),
					true );

			ImageJLogger.info( "Binned view size [GB]: "
					+ Utils.getSizeGB( downSampleView ) );

		}
	}



}
