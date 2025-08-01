/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2025 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package de.embl.cba.bdp2.utils;

import bdv.SpimSource;
import bdv.viewer.Interpolation;
import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.measure.Calibration;
import itc.utilities.IntervalUtils;
import itc.utilities.VectorUtils;
import mpicbg.spim.data.SpimData;
import mpicbg.spim.data.SpimDataException;
import mpicbg.spim.data.XmlIoSpimData;
import mpicbg.spim.data.sequence.VoxelDimensions;
import net.imglib2.*;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.RealViews;
import net.imglib2.realtransform.Scale3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.Arrays;

public class BdvToVoxelGridImageConverter< T extends RealType< T > & NativeType< T > >
{
	private RealRandomAccessible< T > interpolatedSource;
	private String voxelUnit;
	private final String imageTitle;
	private RealInterval targetRealInterval;
	private double[] targetVoxelSpacing;
	private double[] sourceVoxelSpacing;
	private Interval transformedSourceInterval;
	private AffineTransform3D sourceTransform;
	private Scale3D scaleToVoxelGrid;

	public enum FileFormat
	{
		Tiff,
		Bdv
	}

	public enum InterpolationType
	{
		NearestNeighbor,
		NLinear
	}

	public BdvToVoxelGridImageConverter(
			String referenceBdvFilePath,
			String sourceBdvFilePath,
			InterpolationType interpolationType )
	{
		setTargetVoxelSpacingAndRealInterval( referenceBdvFilePath );
		setSourceImageAndTransform( sourceBdvFilePath, interpolationType );

		imageTitle = "image";
	}

	public void run( FileFormat fileFormat, String pathWithoutExtension )
	{
		IJ.log( "Target voxel spacing [" + voxelUnit + "]: " + VectorUtils.toString( targetVoxelSpacing ) );
		IJ.log( "Target interval [" + voxelUnit + "]: " + IntervalUtils.toString( targetRealInterval ) );
		IJ.log( "Source voxel spacing [" + voxelUnit + "]: " + VectorUtils.toString( sourceVoxelSpacing ) );
		IJ.log( "Transformed source interval [voxel]: " + IntervalUtils.toString( transformedSourceInterval ) );

		IJ.log( "Creating image..." );
		final RandomAccessibleInterval< T > transformedRai =
				createTransformedRai( interpolatedSource, sourceTransform, transformedSourceInterval );

		save( transformedRai, fileFormat, pathWithoutExtension );
	}

	private void save( RandomAccessibleInterval< T > transformedRai, FileFormat fileFormat, String pathWithoutExtension )
	{
		switch ( fileFormat ){
			case Tiff:
				IJ.log( "Saving as Tiff..." );
				saveAsTiff( transformedRai, imageTitle, targetVoxelSpacing, voxelUnit, pathWithoutExtension );
				break;
			case Bdv:
				IJ.log( "Saving as Bdv..." );
				saveAsBdv( transformedRai, imageTitle, targetVoxelSpacing, voxelUnit, pathWithoutExtension );
				break;
			default:
		}
	}

	private void setSourceImageAndTransform( String sourceBdvFilePath, InterpolationType interpolationType )
	{
		final SpimData spimData = openSpimData( sourceBdvFilePath );

		final SpimSource< T > spimSource = new SpimSource< >( spimData, 0, "" );

		int level = getClosestSourceLevel( targetVoxelSpacing, spimData );

		sourceVoxelSpacing = BdvUtils.getVoxelSpacings( spimData, 0 ).get( level );

		interpolatedSource = spimSource.getInterpolatedSource( 0, level, getBdvInterpolation( interpolationType ) );

		sourceTransform = new AffineTransform3D();
		spimSource.getSourceTransform( 0, level, sourceTransform );

		final double[] scalingFactors = Arrays.stream( targetVoxelSpacing ).map( x -> 1.0 / x ).toArray();

		scaleToVoxelGrid = new Scale3D( scalingFactors );

		transformedSourceInterval = Intervals.largestContainedInterval(
				IntervalUtils.scale( targetRealInterval, scalingFactors ) );

		sourceTransform.preConcatenate( scaleToVoxelGrid );
	}

	private Interpolation getBdvInterpolation( InterpolationType interpolationType )
	{
		Interpolation interpolation = null;
		switch ( interpolationType )
		{
			case NearestNeighbor:
				interpolation = Interpolation.NEARESTNEIGHBOR;
				break;
			case NLinear:
				interpolation = Interpolation.NLINEAR;
				break;
		}
		return interpolation;
	}

	private void setTargetVoxelSpacingAndRealInterval( String xmlFilename )
	{
		SpimData targetSpimData = openSpimData( xmlFilename );

		final int setupId = 0;
		final Interval targetInterval = targetSpimData.getSequenceDescription()
				.getImgLoader().getSetupImgLoader( setupId )
				.getImage( 0 );

		// TODO: replace this by sourceTransform?!
		setTargetVoxelSpacing( targetSpimData, setupId );
		targetRealInterval = IntervalUtils.toCalibratedRealInterval( targetInterval, targetVoxelSpacing );
	}

	private SpimData openSpimData( String xmlFilename )
	{
		SpimData targetSpimData = null;
		try
		{
			targetSpimData = new XmlIoSpimData().load( xmlFilename );
		} catch ( SpimDataException e )
		{
			e.printStackTrace();
		}
		return targetSpimData;
	}

	private static < T extends RealType< T > & NativeType< T > >
	void saveAsBdv(
			RandomAccessibleInterval< T > rai,
			String imageTitle,
			double[] voxelSpacing,
			String voxelUnit,
			String pathWithoutExtension )
	{
		final RandomAccessibleInterval< T > raiXYZCT =
				Views.addDimension(
						Views.addDimension( rai, 0, 0 ),
						0, 0 );

		new BdvRaiXYZCTExporter< T >().export(
				raiXYZCT,
				imageTitle,
				pathWithoutExtension,
				voxelSpacing,
				voxelUnit,
				new double[]{0,0,0});
	}

	private static < T extends RealType< T > & NativeType< T > >
	void saveAsTiff( RandomAccessibleInterval< T > rai,
					 String imageTitle,
					 double[] voxelSpacing,
					 String voxelUnit,
					 String pathWithoutExtension )
	{
		final ImagePlus imagePlus = asImagePlus( rai, imageTitle, voxelUnit, voxelSpacing );
		new FileSaver( imagePlus ).saveAsTiff( pathWithoutExtension + ".tif" );
	}

	private static < T extends RealType< T > & NativeType< T > >
	RandomAccessibleInterval< T > createTransformedRai(
			RealRandomAccessible< T > source,
			AffineTransform3D transform,
			Interval interval )
	{
		final RealRandomAccessible< T > transformed
				= RealViews.transform( source, transform );

		final RandomAccessible< T > transformedRastered = Views.raster( transformed );

		final RandomAccessibleInterval< T > transformedRasteredInterval =
				Views.interval( transformedRastered, interval );

		return CopyUtils.copyVolumeRaiMultiThreaded( transformedRasteredInterval, 4 );
	}

	public static ImagePlus asImagePlus( RandomAccessibleInterval raiXYZ,
										 String title,
										 String voxelUnit,
										 double[] voxelSpacing )
	{
		final ImagePlus imp = ImageJFunctions.wrap(
				Views.permute(
						Views.addDimension( raiXYZ, 0, 0 ),
						2, 3 ), title );

		final Calibration calibration = new Calibration();
		calibration.pixelWidth = voxelSpacing[ 0 ];
		calibration.pixelHeight = voxelSpacing[ 1 ];
		calibration.pixelDepth = voxelSpacing[ 2 ];
		calibration.setUnit( voxelUnit );

		imp.setCalibration( calibration );

		return imp;
	}

	private void setTargetVoxelSpacing( SpimData medData, int setupId )
	{
		final VoxelDimensions targetVoxelDimensions
				= medData.getSequenceDescription()
				.getViewSetupsOrdered().get( setupId ).getVoxelSize();

		voxelUnit = targetVoxelDimensions.unit();

		targetVoxelSpacing = new double[ targetVoxelDimensions.numDimensions() ];
		for ( int d = 0; d < targetVoxelSpacing.length; d++ )
			targetVoxelSpacing[ d ] = targetVoxelDimensions.dimension( d );

	}

	private static double[] createScalingFactors( double[] targetVoxelSpacing,
												 double[] sourceVoxelSpacing )
	{
		final double[] scales = new double[ 3 ];
		for ( int d = 0; d < 3; ++d )
			scales[ d ] = targetVoxelSpacing[ d ] / sourceVoxelSpacing[ d ];

		return scales;
	}

	private static int getClosestSourceLevel( double[] targetVoxelSpacing, SpimData sourceData )
	{
		final ArrayList< double[] > voxelSpacings = BdvUtils.getVoxelSpacings( sourceData, 0 );

		int level = 0;
		for ( ; level < voxelSpacings.size(); level++ )
		{
			if ( voxelSpacings.get( level )[ 0 ] > targetVoxelSpacing[ 0 ] )
				break;
		}
		if ( level > 0 ) level -= 1;

		return level;
	}

	/**
	 *
	 * @param args
	 * @param <T>
	 */
	public static < T extends RealType< T > & NativeType< T > > void main( String[] args )
	{
		final BdvToVoxelGridImageConverter< T > converter
				= new BdvToVoxelGridImageConverter< T >( args[ 0 ], args[ 1 ], InterpolationType.valueOf( args[ 2 ] ) );

		converter.run( FileFormat.valueOf( args[ 3 ] ), args[ 4 ]);
	}

}
