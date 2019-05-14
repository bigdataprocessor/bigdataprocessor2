package de.embl.cba.bdp2.process;

import bdv.tools.brightness.SliderPanel;
import bdv.util.BoundedValue;
import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.ui.BdvMenus;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.ImageViewer;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class BinningDialog< T extends RealType< T > & NativeType< T > >
{

	public BinningDialog( final ImageViewer< T > imageViewer  )
	{
//		final Image< T > inputImage = imageViewer.getImage();
//		ImageViewer newImageViewer = imageViewer.newImageViewer();
//		newImageViewer.show( imageViewer.getImage(), true );
//		newImageViewer.addMenus( new BdvMenus() );

		Logger.info( "Image size without binning [GB]: "
				+ Utils.getSizeGB( imageViewer.getImage().getRai() ) );

		showBinningAdjustmentDialog( imageViewer, imageViewer.getImage() );
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

				final Image< T > binned = Binner.bin( inputImage, span );

				imageViewer.show( binned, true );

				Logger.info( "Binned view size [GB]: "
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


}
