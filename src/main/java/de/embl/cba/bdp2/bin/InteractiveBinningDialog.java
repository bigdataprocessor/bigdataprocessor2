package de.embl.cba.bdp2.bin;

import bdv.tools.brightness.SliderPanel;
import bdv.util.BoundedValue;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import ij.plugin.frame.Recorder;
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
 *
 *
 * @param <T>
 */
public class InteractiveBinningDialog< T extends RealType< T > & NativeType< T > > extends JDialog
{
	protected final InteractiveBinningDialog.OkCancelPanel buttons;
	private final BdvImageViewer< T > viewer;
	private final Image< T > inputImage;
	private Image< T > outputImage;
	private long[] span;

	public InteractiveBinningDialog( final BdvImageViewer< T > viewer )
	{
		buttons = new InteractiveBinningDialog.OkCancelPanel();
		getContentPane().add( buttons, BorderLayout.SOUTH );
		setDefaultCloseOperation( WindowConstants.HIDE_ON_CLOSE );
		addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing( final WindowEvent e )
			{
				cancel();
			}
		} );

		buttons.onOk( this::ok );
		buttons.onCancel( this::cancel );

		this.inputImage = viewer.getImage();
		this.viewer = viewer;

		Logger.info( "Image size [GB]: "
				+ Utils.getSizeGB( this.inputImage.getRai() ) );

		showBinningAdjustmentDialog();
	}

	private void ok()
	{
		recordAsMacro();
		Logger.info( "Binning was applied." );
		setVisible( false );
	}

	private void recordAsMacro()
	{
		Recorder recorder =  Recorder.getInstance();
		if( recorder != null )
		{
			if ( ! Recorder.scriptMode() )
			{
				String options = "";
				options += "inputImage=[" + inputImage.getName() + "] ";
				options += "outputImageName=[" + outputImage.getName() + "] ";
				options += "newViewer=" + true + " ";
				options += "binWidthXPixels=" + span[ 0 ];
				options += "binWidthYPixels=" + span[ 1 ];
				options += "binWidthZPixels=" + span[ 2 ];

				Recorder.record( "run", "BDP2_Bin...", options );
			}
		}
	}

	private void cancel()
	{
		viewer.replaceImage( inputImage, true );
		Logger.info( "Binning was cancelled." );
		setVisible( false );
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

	/**
	 * A panel containing OK and Cancel buttons, and callback lists for both.
	 */
	public static class OkCancelPanel extends JPanel
	{
		private final ArrayList< Runnable > runOnOk = new ArrayList<>();

		private final ArrayList< Runnable > runOnCancel = new ArrayList<>();

		public OkCancelPanel()
		{
			final JButton cancelButton = new JButton( "Cancel" );
			cancelButton.addActionListener( e -> runOnCancel.forEach( Runnable::run ) );

			final JButton okButton = new JButton( "OK" );
			okButton.addActionListener( e -> runOnOk.forEach( Runnable::run ) );

			setLayout( new BoxLayout( this, BoxLayout.LINE_AXIS ) );
			add( Box.createHorizontalGlue() );
			add( cancelButton );
			add( okButton );
		}

		public synchronized void onOk( final Runnable runnable )
		{
			runOnOk.add( runnable );
		}

		public synchronized void onCancel( final Runnable runnable )
		{
			runOnCancel.add( runnable );
		}
	}

}
