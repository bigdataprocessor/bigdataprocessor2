package de.embl.cba.bdp2.convert.singlechannel;

import bdv.tools.brightness.SliderPanelDouble;
import bdv.util.BoundedValueDouble;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.macro.MacroRecorder;
import de.embl.cba.bdp2.dialog.AbstractProcessingDialog;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import ij.IJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.converter.RealUnsignedByteConverter;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.util.Util;

import javax.swing.*;

@Deprecated
public class UnsignedByteTypeConversionDialog< R extends RealType< R > & NativeType< R > > extends AbstractProcessingDialog< R >
{
	private double mapTo0;
	private double mapTo255;
	private RealUnsignedByteConverter< R > converter;

	public UnsignedByteTypeConversionDialog( final BdvImageViewer< R > viewer )
	{
		this.viewer = viewer;
		this.inputImage = viewer.getImage();
		final RandomAccessibleInterval< R > rai = inputImage.getRai();

		if ( ( Util.getTypeFromInterval( rai ) instanceof UnsignedByteType) )
		{
			IJ.showMessage("This image is already 8-bit.");
			return;
		}

		mapTo0 = viewer.getDisplaySettings().get( 0 ).getDisplayRangeMin();
		mapTo255 = viewer.getDisplaySettings().get( 0 ).getDisplayRangeMax();

		final UnsignedByteTypeConverter< R > unsignedByteTypeConverter = new UnsignedByteTypeConverter<>( inputImage, mapTo0, mapTo255 );

		converter = unsignedByteTypeConverter.getConverter();
		outputImage = unsignedByteTypeConverter.getConvertedImage();

		viewer.replaceImage( outputImage, true, true );

		for ( int c = 0; c < viewer.getImage().numChannels(); c++ )
			viewer.setDisplaySettings( 0, 255, null, c );

		Logger.info( "8-bit view size [GB]: " + Utils.getSizeGB( outputImage.getRai() ) );

		prepareDialog();
		showDialog( panel );
	}

	@Override
	protected void recordMacro()
	{
		final MacroRecorder recorder = new MacroRecorder( ConvertToUnsignedByteTypeCommand.COMMAND_FULL_NAME, inputImage, outputImage);

		recorder.addOption( "mapTo0", (int) mapTo0 );
		recorder.addOption( "mapTo255",  (int) mapTo255 );

		recorder.record();
	}
	
	protected void prepareDialog()
	{
		final double rangeMin = 0;
		final double rangeMax = 65535;

		final BoundedValueDouble min = new BoundedValueDouble(
				rangeMin,
				rangeMax,
				mapTo0 );

		final BoundedValueDouble max = new BoundedValueDouble(
				rangeMin,
				rangeMax,
				mapTo255 );

		panel = new JPanel();
		panel.setLayout( new BoxLayout( panel, BoxLayout.PAGE_AXIS ) );

		final SliderPanelDouble minSlider =
				new SliderPanelDouble( "0    <= ", min, 1 );
		final SliderPanelDouble maxSlider =
				new SliderPanelDouble( "255  <= ", max, 1 );

		class UpdateListener implements BoundedValueDouble.UpdateListener
		{
			@Override
			public void update()
			{
				mapTo0 = min.getCurrentValue();
				converter.setMin( mapTo0 );
				mapTo255 = max.getCurrentValue();
				converter.setMax( mapTo255 );
				minSlider.update();
				maxSlider.update();
				viewer.replaceImage( outputImage, false, true );
			}
		}

		final UpdateListener updateListener = new UpdateListener();
		min.setUpdateListener( updateListener );
		max.setUpdateListener( updateListener );

		panel.add( minSlider );
		panel.add( maxSlider );

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

		final Image< R > convertedImage = image.newImage( convertedRai );

		return convertedImage;
	}
}
