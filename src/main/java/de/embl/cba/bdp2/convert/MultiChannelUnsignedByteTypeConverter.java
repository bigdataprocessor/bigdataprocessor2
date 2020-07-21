package de.embl.cba.bdp2.convert;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.utils.DimensionOrder;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.converter.RealUnsignedByteConverter;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.List;

public class MultiChannelUnsignedByteTypeConverter< R extends RealType< R > & NativeType< R > >
{
	private final Image< R > inputImage;
	private final List< RealUnsignedByteConverter< R > > converters;

	public MultiChannelUnsignedByteTypeConverter( Image< R > inputImage, List< double[] > initialMappings )
	{
		this.inputImage = inputImage;
		this.converters = new ArrayList<>(  );
		for ( double[] mapping : initialMappings )
		{
			converters.add( new RealUnsignedByteConverter<>( mapping[ 0 ], mapping[ 1] ) );
		}
	}

	public Image< R > getConvertedImage()
	{
		final ArrayList< RandomAccessibleInterval< R > > convertedChannelRais = new ArrayList<>();

		for ( int c = 0; c < inputImage.numChannels(); c++ )
		{
			final IntervalView< R > channel = Views.hyperSlice( inputImage.getRai(), DimensionOrder.C, c );
			final RandomAccessibleInterval< R > convertedRai =
					Converters.convert(
							( RandomAccessibleInterval ) channel,
							converters.get( c ),
							new UnsignedByteType() );
			convertedChannelRais.add( convertedRai );
		}

		final IntervalView< R > convertedRai = Views.permute( Views.stack( convertedChannelRais ), 3, 4 );

		final Image< R > outputImage = inputImage.newImage( convertedRai );
		outputImage.setName( inputImage.getName() + "-8bit" );

		return outputImage;
	}

	public List< RealUnsignedByteConverter< R > >  getConverters()
	{
		return converters;
	}
}
