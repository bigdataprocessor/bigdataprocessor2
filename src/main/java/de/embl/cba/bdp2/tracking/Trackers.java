package de.embl.cba.bdp2.tracking;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.phasecorrelation.PhaseCorrelation2;
import net.imglib2.algorithm.phasecorrelation.PhaseCorrelationPeak2;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.complex.ComplexFloatType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import java.util.concurrent.ExecutorService;

public class Trackers
{

	public static < T extends RealType<T> >
	double[] getPhaseCorrelationShift(
			RandomAccessibleInterval< T > currentFrame,
			RandomAccessibleInterval< T > nextFrame,
			ExecutorService executorService )
	{
		RandomAccessibleInterval< FloatType > pcm =
				PhaseCorrelation2.calculatePCM(
						Views.zeroMin(currentFrame),
						Views.zeroMin(nextFrame),
						new CellImgFactory(new FloatType()),
						new FloatType(),
						new CellImgFactory(new ComplexFloatType()),
						new ComplexFloatType(),
						executorService );


		ImageJFunctions.show( pcm, "corr" );

		PhaseCorrelationPeak2 shiftPeak =
				PhaseCorrelation2.getShift(
						pcm,
						Views.zeroMin(currentFrame),
						Views.zeroMin(nextFrame),
						2,
						(long) ( 0.5 * currentFrame.dimension( 0 ) * currentFrame.dimension( 1 ) ),
						true,
						false,
						executorService );

		double[] shift = new double[currentFrame.numDimensions()];
		shiftPeak.getSubpixelShift().localize(shift);

		return shift;
	}
}
