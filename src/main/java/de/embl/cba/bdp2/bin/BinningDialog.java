package de.embl.cba.bdp2.bin;

import bdv.tools.brightness.SliderPanel;
import bdv.util.BoundedValue;
import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class BinningDialog< T extends RealType< T > & NativeType< T > >
{

	public BinningDialog( final BdvImageViewer< T > imageViewer  )
	{
		Logger.info( "Image size [GB]: "
				+ Utils.getSizeGB( imageViewer.getImage().getRai() ) );

		showBinningAdjustmentDialog( imageViewer, imageViewer.getImage() );
	}

	private void showBinningAdjustmentDialog(BdvImageViewer imageViewer, Image< T > inputImage )
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

				final Image< T > binned = Binner.bin( inputImage, asRadii( span ) );

				imageViewer.replaceImage( binned );

				Logger.info( "Binned ( "
						+ span[ 0 ] + " , "
						+ span[ 1 ] + " , "
						+ span[ 2 ] + " ) view size [GB]: "
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


			private long[] asRadii( long[] span )
			{
				final long[] radii = new long[ 5 ];
				for ( int d = 0; d < 3; d++ )
					radii[ d ] = ( span[ d ] - 1 ) / 2;
				return radii;
			}

			private long[] getNewSpan()
			{
				final long[] span = new long[ 5 ];
				for ( int d = 0; d < 3; d++ )
					span[ d ] = boundedValues.get( d ).getCurrentValue() ;
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
