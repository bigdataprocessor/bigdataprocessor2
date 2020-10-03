package de.embl.cba.bdp2.process.convert.singlechannel;

import de.embl.cba.bdp2.image.Image;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.converter.RealUnsignedByteConverter;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;


@Deprecated
public class UnsignedByteTypeConverter < R extends RealType< R > & NativeType< R > >
{
	private final Image< R > inputImage;
	private final RealUnsignedByteConverter< R > converter;

	public UnsignedByteTypeConverter( Image< R > inputImage, double mapTo0, double mapTo255 )
	{
		this.inputImage = inputImage;
		converter = new RealUnsignedByteConverter<>( mapTo0, mapTo255 );
	}

	public Image< R > getConvertedImage()
	{
		final RandomAccessibleInterval< R > convertedRai =
				Converters.convert(
						(RandomAccessibleInterval) inputImage.getRai(),
						converter,
						new UnsignedByteType() );

		final Image< R > outputImage = inputImage.newImage( convertedRai );
		outputImage.setName( inputImage.getName() + "-8bit" );

		return outputImage;
	}

	public RealUnsignedByteConverter< R > getConverter()
	{
		return converter;
	}
}
