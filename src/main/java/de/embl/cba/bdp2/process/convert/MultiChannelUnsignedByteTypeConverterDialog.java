package de.embl.cba.bdp2.process.convert;

import bdv.tools.brightness.SliderPanelDouble;
import bdv.util.BoundedValueDouble;
import de.embl.cba.bdp2.dialog.AbstractProcessingDialog;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.record.ScriptRecorder;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewer.ImageViewer;
import ij.IJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.converter.RealUnsignedByteConverter;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Util;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MultiChannelUnsignedByteTypeConverterDialog< R extends RealType< R > & NativeType< R > > extends AbstractProcessingDialog< R >
{
	private List< double[] > contrastLimits; // to be mapped onto 0 and 255
	private List< RealUnsignedByteConverter< R > > converters;

	public MultiChannelUnsignedByteTypeConverterDialog( final ImageViewer< R > viewer )
	{
		this.viewer = viewer;
		this.inputImage = viewer.getImage();
		final RandomAccessibleInterval< R > rai = inputImage.getRai();

		if ( ( Util.getTypeFromInterval( rai ) instanceof UnsignedByteType) )
		{
			IJ.showMessage("This image is already of unsigned byte type.");
			return;
		}

		initContrastLimits( viewer );
		showConvertedImage( viewer );
		createPanel();
	}

	public void showConvertedImage( ImageViewer< R > viewer )
	{
		final MultiChannelUnsignedByteTypeConverter< R > byteTypeConverter = new MultiChannelUnsignedByteTypeConverter<>( inputImage, contrastLimits );

		converters = byteTypeConverter.getConverters();
		outputImage = byteTypeConverter.getConvertedImage();

		viewer.replaceImage( outputImage, true, true );

		for ( int c = 0; c < viewer.getImage().getNumChannels(); c++ )
			viewer.setDisplaySettings( 0, 255, null, c );

		Logger.info( "8-bit view size [GB]: " + Utils.getSizeGB( outputImage.getRai() ) );
	}

	public void initContrastLimits( ImageViewer< R > viewer )
	{
		contrastLimits = new ArrayList<>(  );
		for ( int c = 0; c < inputImage.getNumChannels(); c++ )
		{
			contrastLimits.add( new double[]{ viewer.getDisplaySettings().get( c ).getDisplayRangeMin(), viewer.getDisplaySettings().get( c ).getDisplayRangeMax()});
		}
	}

	@Override
	protected void recordMacro()
	{
		final ScriptRecorder recorder = new ScriptRecorder( MultiChannelUnsignedByteTypeConverterCommand.COMMAND_FULL_NAME, inputImage, outputImage);

		recorder.addCommandParameter( "mapTo0", contrastLimits.stream().map( x -> "" + x[ 0 ] ).collect( Collectors.joining( ",") ) );
		recorder.addCommandParameter( "mapTo255", contrastLimits.stream().map( x -> "" + x[ 1 ] ).collect( Collectors.joining( ",") ));

		// Image< R > convertToUnsignedByteType( Image< R > image, double[] min, double[] max )
		double[] min = contrastLimits.stream().mapToDouble( x -> x[ 0 ] ).toArray();
		double[] max = contrastLimits.stream().mapToDouble( x -> x[ 1 ] ).toArray();
		recorder.setBDP2FunctionName( "convertToUnsignedByteType" );
		recorder.addAPIFunctionPrequelComment(  MultiChannelUnsignedByteTypeConverterCommand.COMMAND_NAME );
		recorder.addAPIFunctionParameter( min );
		recorder.addAPIFunctionParameter( max );

		recorder.record();
	}
	
	protected void createPanel()
	{
		panel = new JPanel();
		panel.setLayout( new BoxLayout( panel, BoxLayout.PAGE_AXIS ) );

		for ( int c = 0; c < inputImage.getNumChannels(); c++ )
		{
			final int channel = c;
			final double rangeMin = 0;
			final double rangeMax = 65535;

			final BoundedValueDouble min = new BoundedValueDouble(
					rangeMin,
					rangeMax,
					contrastLimits.get( c )[ 0 ] );

			final BoundedValueDouble max = new BoundedValueDouble(
					rangeMin,
					rangeMax,
					contrastLimits.get( c )[ 1 ] );

			final SliderPanelDouble minSlider =
					new SliderPanelDouble( " 0    <= ", min, 1 );
			final SliderPanelDouble maxSlider =
					new SliderPanelDouble( " 255  <= ", max, 1 );

			class UpdateListener implements BoundedValueDouble.UpdateListener
			{
				@Override
				public void update()
				{
					//mappings.get( channel )[ 0 ] = min.getCurrentValue(); // is this needed?
					converters.get( channel ).setMin( min.getCurrentValue() );
					converters.get( channel ).setMax( max.getCurrentValue() );
					minSlider.update();
					maxSlider.update();
					viewer.replaceImage( outputImage, false, true );
				}
			}

			final UpdateListener updateListener = new UpdateListener();
			min.setUpdateListener( updateListener );
			max.setUpdateListener( updateListener );

			panel.add( new JLabel( "Channel " + channel + ": " + inputImage.getChannelNames()[ channel ] ));
			panel.add( minSlider );
			panel.add( maxSlider );
		}

		setTitle( "8-bit conversion" );
	}

	public static < R extends RealType< R > & NativeType< R > >
	Image< R > convert( Image< R > image, double mapTo0, double mapTo255 )
	{
		final RealUnsignedByteConverter converter =
				new RealUnsignedByteConverter<>(
						mapTo0,
						mapTo255 );

		final RandomAccessibleInterval< R > convertedRai =
				Converters.convert(
						image.getRai(),
						converter,
						new UnsignedByteType() );

		final Image< R > convertedImage = new Image<>( image );
		convertedImage.setRai( convertedRai );

		return convertedImage;
	}
}
