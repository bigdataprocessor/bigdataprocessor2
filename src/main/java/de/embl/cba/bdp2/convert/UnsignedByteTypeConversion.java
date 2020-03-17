package de.embl.cba.bdp2.convert;

import bdv.tools.brightness.SliderPanelDouble;
import bdv.util.BoundedValueDouble;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.logging.Logger;
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
import java.awt.*;

public class UnsignedByteTypeConversion < R extends RealType< R > & NativeType< R > >
{
	public UnsignedByteTypeConversion( final BdvImageViewer imageViewer  )
	{
		final Image image = imageViewer.getImage();
		final RandomAccessibleInterval rai = image.getRai();

		if ( ( Util.getTypeFromInterval( rai ) instanceof UnsignedByteType) )
		{
			IJ.showMessage("This image is already 8-bit.");
			return;
		}

		final double mapTo0 =
				imageViewer.getAutoContrastDisplaySettings( 0 ).getDisplayRangeMin();
		final double mapTo255 =
				imageViewer.getAutoContrastDisplaySettings( 0 ).getDisplayRangeMax();

		final RealUnsignedByteConverter< R > converter =
				new RealUnsignedByteConverter<>(
						mapTo0,
						mapTo255 );

		final RandomAccessibleInterval< R > convertedRai =
				Converters.convert(
					rai,
					converter,
					new UnsignedByteType() );

		final Image< R > convertedImage = image.newImage( convertedRai );


		imageViewer.replaceImage( convertedImage, autoContrast );

		for ( int c = 0; c < imageViewer.getImage().numChannels(); c++ )
			imageViewer.setDisplayRange( 0, 255, c );

		Logger.info( "8-bit view size [GB]: " + Utils.getSizeGB( convertedRai ) );

		showConversionAdjustmentDialog( converter, mapTo0, mapTo255, imageViewer );

	}

	private void showConversionAdjustmentDialog(
			RealUnsignedByteConverter< R > converter,
			double currentMin,
			double currentMax,
			BdvImageViewer imageViewer)
	{
		final double rangeMin = 0;
		final double rangeMax = 65535;

		final JFrame frame = new JFrame( "8-bit conversion" );

		frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );

		final BoundedValueDouble min = new BoundedValueDouble(
				rangeMin,
				rangeMax,
				currentMin );

		final BoundedValueDouble max = new BoundedValueDouble(
				rangeMin,
				rangeMax,
				currentMax );

		JPanel panel = new JPanel();
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
				converter.setMin( min.getCurrentValue() );
				converter.setMax( max.getCurrentValue() );
				minSlider.update();
				maxSlider.update();
				imageViewer.repaint();
			}
		}

		final UpdateListener updateListener = new UpdateListener();
		min.setUpdateListener( updateListener );
		max.setUpdateListener( updateListener );

		panel.add( minSlider );
		panel.add( maxSlider );

		frame.setContentPane( panel );
		frame.setBounds(
				MouseInfo.getPointerInfo().getLocation().x,
				MouseInfo.getPointerInfo().getLocation().y,
				120, 10);
		frame.setResizable( false );
		frame.pack();
		frame.setVisible( true );
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
