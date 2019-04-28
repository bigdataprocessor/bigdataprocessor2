package de.embl.cba.bdp2.tracking;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import net.imagej.ImageJ;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.algorithm.phasecorrelation.PhaseCorrelation2;
import net.imglib2.algorithm.phasecorrelation.PhaseCorrelationPeak2;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.complex.ComplexFloatType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import java.util.concurrent.ExecutorService;

import static de.embl.cba.transforms.utils.ImageCreators.createEmptyArrayImg;

public class Trackers
{

	public static < T extends RealType < T > & NativeType< T > >
	double[] getPhaseCorrelationShift(
			RandomAccessibleInterval< T > im0,
			RandomAccessibleInterval< T > im1,
			ExecutorService executorService )
	{

		final double[] sigmas = {5,5};

		RandomAccessibleInterval< T > blur0 = createEmptyArrayImg( im0 );
		Gauss3.gauss( sigmas, Views.extendBorder( im0 ), blur0 ) ;

		RandomAccessibleInterval< T > blur1 = createEmptyArrayImg( im1 );
		Gauss3.gauss( sigmas, Views.extendBorder( im1 ), blur1 ) ;

		RandomAccessibleInterval< FloatType > pcm =
				PhaseCorrelation2.calculatePCM(
						Views.zeroMin(blur0),
						Views.zeroMin(blur1),
						new CellImgFactory(new FloatType()),
						new FloatType(),
						new CellImgFactory(new ComplexFloatType()),
						new ComplexFloatType(),
						executorService );


		ImageJFunctions.show( blur0, "" );
		ImageJFunctions.show( blur1, "" );
		ImageJFunctions.show( pcm, "corr" );


		PhaseCorrelationPeak2 shiftPeak =
				PhaseCorrelation2.getShift(
						pcm,
						Views.zeroMin(blur0),
						Views.zeroMin(blur1),
						10,
						(long) ( 0.5 * im0.dimension( 0 ) * im0.dimension( 1 ) ),
						true,
						false,
						executorService );

		double[] shift = new double[im0.numDimensions()];
		shiftPeak.getSubpixelShift().localize(shift);

		return shift;
	}
}
