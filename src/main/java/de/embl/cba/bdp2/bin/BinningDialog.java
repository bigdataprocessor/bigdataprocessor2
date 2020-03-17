package de.embl.cba.bdp2.bin;

import bdv.tools.brightness.SliderPanel;
import bdv.util.BoundedValue;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.record.MacroRecorder;
import de.embl.cba.bdp2.ui.AbstractOkCancelDialog;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * TODO: make only one slider for binning in X and Y
 *
 */
public class BinningDialog< T extends RealType< T > & NativeType< T > > extends AbstractOkCancelDialog
{
	private final BdvImageViewer< T > viewer;
	private final Image< T > inputImage;
	private Image< T > outputImage;
	private long[] span;

	public BinningDialog( final BdvImageViewer< T > viewer )
	{
		this.inputImage = viewer.getImage();
		this.viewer = viewer;

		Logger.info( "Image size [GB]: "
				+ Utils.getSizeGB( this.inputImage.getRai() ) );

		showBinningAdjustmentDialog();
	}

	@Override
	protected void ok()
	{
		recordMacro();
		Logger.info( "Binning was applied." );
		setVisible( false );
	}

	@Override
	protected void cancel()
	{
		viewer.replaceImage( inputImage, true );
		Logger.info( "Binning was cancelled." );
		setVisible( false );
	}

	private void recordMacro()
	{
		final MacroRecorder recorder = new MacroRecorder( "BDP2_Bin...", inputImage, outputImage, false );

		recorder.addOption( "binWidthXPixels",  span[ 0 ] );
		recorder.addOption( "binWidthYPixels",  span[ 0 ] );
		recorder.addOption( "binWidthZPixels",  span[ 0 ] );

		recorder.record();
	}

	private void showBinningAdjustmentDialog()
	{
//		final JFrame frame = new JFrame( "Binning" );
//		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

		JPanel panel = new JPanel();
		panel.setLayout( new BoxLayout( panel, BoxLayout.PAGE_AXIS ) );

		final ArrayList< BoundedValue > boundedValues = new ArrayList<>();
		final ArrayList< SliderPanel > sliderPanels = new ArrayList<>();

		final String[] xyz = { "X", "Y", "Z" };

		for ( int d = 0; d < 3; d++ )
		{
			boundedValues.add(
					new BoundedValue(1,21,1 ) );

			sliderPanels.add(
					new SliderPanel(
							"Binning " + xyz[ d ] ,
								boundedValues.get( d ),
								1 ));
		}

		class UpdateListener implements BoundedValue.UpdateListener
		{
			private long[] previousSpan;

			@Override
			public synchronized void update()
			{
				span = getNewSpan();

				if ( ! isSpanChanged( span ) ) return;

				previousSpan = span;

				outputImage = Binner.bin( inputImage, span );
				outputImage.setName( inputImage.getName() + "-binned" );
				viewer.replaceImage( outputImage, true );

				Logger.info( "Binning: "
						+ span[ 0 ] + " , "
						+ span[ 1 ] + " , "
						+ span[ 2 ] + " [Pixels]" +
						"; Size [GB]: " + Utils.getSizeGB( outputImage.getRai() ) );
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
				Arrays.fill( span, 1 );
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

		getContentPane().add( panel, BorderLayout.CENTER  );
		this.setBounds(
				MouseInfo.getPointerInfo().getLocation().x,
				MouseInfo.getPointerInfo().getLocation().y,
				120, 10);
		this.setResizable( false );
		this.pack();
		this.setVisible( true );
	}

}
