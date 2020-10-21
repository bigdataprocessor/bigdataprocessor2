package de.embl.cba.bdp2.process.bin;

import bdv.tools.brightness.SliderPanel;
import bdv.util.BoundedValue;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.macro.MacroRecorder;
import de.embl.cba.bdp2.dialog.AbstractProcessingDialog;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewer.ImageViewer;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;

public class BinDialog< R extends RealType< R > & NativeType< R > > extends AbstractProcessingDialog< R >
{
	private long[] span;

	public BinDialog( final ImageViewer< R > viewer )
	{
		this.inputImage = viewer.getImage();
		this.viewer = viewer;

		Logger.info( "Image size [GB]: " + Utils.getSizeGB( this.inputImage.getRai() ) );
		createPanel();
	}

	@Override
	protected void recordMacro()
	{
		final MacroRecorder recorder = new MacroRecorder( BinCommand.COMMAND_FULL_NAME, inputImage, outputImage );

		recorder.addOption( "binWidthXPixels",  span[ 0 ] );
		recorder.addOption( "binWidthYPixels",  span[ 1 ] );
		recorder.addOption( "binWidthZPixels",  span[ 2 ] );

		// Image< R > bin( Image< R > image, long[] spanXYZCT )
		recorder.setAPIFunction( "bin" );
		recorder.addAPIFunctionParameter( span );

		recorder.record();
	}

	@Override
	protected void createPanel()
	{
		panel = new JPanel();
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
							"  Binning " + xyz[ d ] ,
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
				viewer.replaceImage( outputImage, false, true );

				for ( SliderPanel sliderPanel : sliderPanels )
				{
					sliderPanel.update();
				}

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
	}
}
