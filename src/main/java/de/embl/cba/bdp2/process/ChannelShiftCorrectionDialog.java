package de.embl.cba.bdp2.process;

import bdv.tools.brightness.SliderPanel;
import bdv.util.BoundedValue;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ChannelShiftCorrectionDialog < T extends RealType< T > & NativeType< T > >
{

	private final BdvImageViewer< T > imageViewer;
	private ArrayList< BoundedValue > boundedValues;
	private ArrayList< SliderPanel > sliderPanels;
	private ChromaticShiftUpdateListener updateListener;
	private JPanel panel;
	private final ChannelShifter channelShifter;
	private final long numChannels;

	public ChannelShiftCorrectionDialog( final BdvImageViewer< T > imageViewer  )
	{
		this.imageViewer = imageViewer;

		channelShifter = new ChannelShifter( imageViewer.getImage().getRai() );
		numChannels = channelShifter.getNumChannels();

		showChromaticShiftCorrectionDialog();
	}


	private void showChromaticShiftCorrectionDialog()
	{
		panel = new JPanel();
		panel.setLayout( new BoxLayout( panel, BoxLayout.PAGE_AXIS ) );

		boundedValues = new ArrayList<>();
		sliderPanels = new ArrayList<>();
		updateListener = new ChromaticShiftUpdateListener();

		final String[] xyz = { "X", "Y", "Z" };

		for ( int c = 0; c < channelShifter.getNumChannels(); c++ )
			for ( String axis : xyz )
				createValueAndSlider( c, axis );

		showFrame( panel );
	}

	private void showFrame( JPanel panel )
	{
		final JFrame frame = new JFrame( "Chromatic Shift Correction [Pixels]" );
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
			final ArrayList< long[] > translations = getTranslations();

			if ( ! isTranslationsChanged( translations ) ) return;

			updateSliders();

			final RandomAccessibleInterval< T > correctedRAI =
					channelShifter.getChannelShiftedRAI( translations );

			imageViewer.replaceImage( imageViewer.getImage().newImage( correctedRAI ) );
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
