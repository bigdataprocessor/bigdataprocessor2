package de.embl.cba.bdp2.process;

import bdv.tools.brightness.SliderPanel;
import bdv.util.BoundedValue;
import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.ui.BdvMenus;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.ImageViewer;
import de.embl.cba.lazyalgorithm.LazyDownsampler;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import static de.embl.cba.bdp2.ui.BigDataProcessorCommand.logger;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Binning< T extends RealType< T > & NativeType< T > >
{

	public Binning( final ImageViewer< T > imageViewer  )
	{
		final Image< T > inputImage = imageViewer.getImage();
		ImageViewer newImageViewer = imageViewer.newImageViewer();
		newImageViewer.show( imageViewer.getImage(), true );

		logger.info( "Image size without binning [GB]: "
				+ Utils.getSizeGB( imageViewer.getImage().getRai() ) );
		newImageViewer.addMenus( new BdvMenus() );

		showBinningAdjustmentDialog( newImageViewer, inputImage );
	}

	private static double[] getBinnedVoxelSize( long[] span, double[] voxelSpacing )
	{
		final double[] newVoxelSize = new double[ voxelSpacing.length ];

		for ( int d = 0; d < 3; d++ )
			newVoxelSize[ d ] = voxelSpacing[ d ] * ( 2 * span[ d ] + 1 );

		return newVoxelSize;
	}

	private void showBinningAdjustmentDialog( ImageViewer imageViewer, Image< T > inputImage )
	{
		final JFrame frame = new JFrame( "Binning" );
		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

		JPanel panel = new JPanel();
		panel.setLayout( new BoxLayout( panel, BoxLayout.PAGE_AXIS ) );

		final ArrayList< BoundedValue > boundedValues = new ArrayList<>();
		final ArrayList< SliderPanel > sliderPanels = new ArrayList<>();

		final String[] xyz = { "X", "Y", "Z" };

		for ( int d = 0; d < 3; d++ )
		{
			boundedValues.add(
					new BoundedValue(1,11,1 ) );

			sliderPanels.add(
					new SliderPanel(
							"Binning " + xyz[ d ] ,
								boundedValues.get( d ),
								2 ));
		}

		class UpdateListener implements BoundedValue.UpdateListener
		{
			private long[] previousSpan;

			@Override
			public synchronized void update()
			{
				final long[] span = getNewSpan();

				if ( ! isSpanChanged( span ) ) return;

				previousSpan = span;

				final Image< T > binned = bin( inputImage, span );

				imageViewer.show( binned, true );

				logger.info( "Binned view size [GB]: "
						+ Utils.getSizeGB( binned.getRai() ) );
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
				for ( int d = 0; d < 3; d++ )
					span[ d ] = ( boundedValues.get( d ).getCurrentValue() - 1 ) / 2;
				return span;
			}
		}

		final UpdateListener updateListener = new UpdateListener();

		for ( int d = 0; d < 3; d++ )
		{
			boundedValues.get( d ).setUpdateListener( updateListener );
			panel.add( sliderPanels.get( d ) );
		}

		frame.setContentPane( panel );
		frame.setBounds(
				MouseInfo.getPointerInfo().getLocation().x,
				MouseInfo.getPointerInfo().getLocation().y,
				120, 10);
		frame.setResizable( false );
		frame.pack();
		frame.setVisible( true );
	}

	public static < T extends RealType< T > & NativeType< T > >
	Image< T > bin( Image< T > inputImage, long[] span )
	{
		final RandomAccessibleInterval< T > downSampleView =
				new LazyDownsampler<>( inputImage.getRai(), span ).getDownsampledView();

		return ( Image< T > ) new Image(
				downSampleView,
				inputImage.getName() + "_bin",
				getBinnedVoxelSize(
						span,
						inputImage.getVoxelSpacing() ),
				inputImage.getVoxelUnit()
		);
	}


}
