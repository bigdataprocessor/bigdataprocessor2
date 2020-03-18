package de.embl.cba.bdp2.register;

import bdv.tools.brightness.SliderPanel;
import bdv.util.BoundedValue;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.record.MacroRecorder;
import de.embl.cba.bdp2.ui.AbstractOkCancelDialog;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ChannelShiftCorrectionDialog < T extends RealType< T > & NativeType< T > > extends AbstractOkCancelDialog
{
	private final BdvImageViewer< T > viewer;
	private final Image< T > inputImage;
	private ArrayList< BoundedValue > boundedValues;
	private ArrayList< SliderPanel > sliderPanels;
	private ChromaticShiftUpdateListener updateListener;
	private JPanel panel;
	private final ChannelShifter channelShifter;
	private final long numChannels;
	private Image< T > outputImage;
	private ArrayList< long[] > translations;

	public ChannelShiftCorrectionDialog( final BdvImageViewer< T > viewer  )
	{
		this.viewer = viewer;
		this.inputImage = viewer.getImage();

		channelShifter = new ChannelShifter( inputImage.getRai() );
		numChannels = inputImage.getRai().dimension( DimensionOrder.C );

		showDialog();
	}

	@Override
	protected void ok()
	{

	}

	@Override
	protected void cancel()
	{
		viewer.replaceImage( inputImage, true );
		Logger.info( "Chromatic shift correction was cancelled." );
		setVisible( false );
	}

	private void recordMacro()
	{
		final MacroRecorder recorder = new MacroRecorder( "BDP2_ShiftChannels...", inputImage, outputImage, false );

//		recorder.addOption( "translations",  span[ 0 ] );
//		recorder.addOption( "binWidthYPixels",  span[ 0 ] );
//		recorder.addOption( "binWidthZPixels",  span[ 0 ] );
//
//		recorder.record();
	}

	private void showDialog()
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

		getContentPane().add( panel, BorderLayout.CENTER  );
		setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

		setBounds(
				MouseInfo.getPointerInfo().getLocation().x,
				MouseInfo.getPointerInfo().getLocation().y,
				120, 10);
		setTitle( "Chromatic Shift Correction [Pixels]" );
		setResizable( false );
		pack();
		setVisible( true );
	}

	private void createValueAndSlider( int c, String axis )
	{
		final BoundedValue boundedValue
				= new BoundedValue(
				-200, // TODO: how much?
				200,
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
			translations = getTranslations();

			if ( ! isTranslationsChanged( translations ) ) return;

			updateSliders();

			final RandomAccessibleInterval< T > correctedRAI =
					channelShifter.getShiftedRai( translations );

			outputImage = inputImage.newImage( correctedRAI );

			viewer.replaceImage( outputImage, false );
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
}
