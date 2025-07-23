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

import bdv.export.*;
import bdv.ij.util.PluginHelper;
import bdv.img.hdf5.Hdf5ImageLoader;
import bdv.img.hdf5.Partition;
import bdv.spimdata.SequenceDescriptionMinimal;
import bdv.spimdata.SpimDataMinimal;
import bdv.spimdata.XmlIoSpimDataMinimal;
import mpicbg.spim.data.generic.sequence.BasicImgLoader;
import mpicbg.spim.data.generic.sequence.BasicSetupImgLoader;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.generic.sequence.ImgLoaderHint;
import mpicbg.spim.data.registration.ViewRegistration;
import mpicbg.spim.data.registration.ViewRegistrations;
import mpicbg.spim.data.sequence.*;
import net.imglib2.Dimensions;
import net.imglib2.FinalDimensions;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BdvRaiXYZCTExporter< T extends RealType< T >  & NativeType< T > >
{
	public static final int CHANNEL_DIM = 3;
	public static final int TIME_DIM = 4;

	private ProgressWriter progressWriter = new ProgressWriterBdv();

	public void setProgressWriter( ProgressWriter progressWriter )
	{
		this.progressWriter = progressWriter;
	}

	public void export(
			RandomAccessibleInterval< T > raiXYZCT,
			String name,
			String filePathWithoutExtension,
			double[] calibration,
			String calibrationUnit,
			double[] translation // TODO: replace by AffineTransform3D
	)
	{
		raiXYZCT = Views.zeroMin( raiXYZCT ); // below code does not save pixels at negative coordinates....

		final File hdf5File = new File( filePathWithoutExtension + ".h5" );
		final File xmlFile = new File( filePathWithoutExtension + ".xml" );

		// set up calibration
		String pixelUnit = getPixelUnit( calibrationUnit );
		final FinalVoxelDimensions voxelSize = new FinalVoxelDimensions( pixelUnit, calibration );
		final FinalDimensions imageSize = getFinalDimensions( raiXYZCT );

		// propose reasonable mipmap settings
		final ExportMipmapInfo autoMipmapSettings =
				ProposeMipmaps.proposeMipmaps(
						new BasicViewSetup( 0, "", imageSize, voxelSize ) );

		progressWriter.out().println( "Starting export..." );

		final BasicImgLoader imgLoader = new RaiXYZCTLoader( raiXYZCT, calibration, calibrationUnit );

		final int numTimePoints = (int) raiXYZCT.dimension( TIME_DIM );
		final int numChannels = (int) raiXYZCT.dimension( CHANNEL_DIM );

		final AffineTransform3D sourceTransform =
				getSourceTransform3D( calibration, translation );

		// write hdf5
		final HashMap< Integer, BasicViewSetup > setups = new HashMap<>( numChannels );
		for ( int channelIndex = 0; channelIndex < numChannels; ++channelIndex )
		{
			final BasicViewSetup setup = new BasicViewSetup(
					channelIndex, name + String.format( "_ch%d", channelIndex ), imageSize, voxelSize );
			setup.setAttribute( new Channel( channelIndex ) );
			setups.put( channelIndex, setup );
		}

		final ArrayList< TimePoint > timePoints = new ArrayList<>( numTimePoints );

		for ( int t = 0; t < numTimePoints; ++t )
			timePoints.add( new TimePoint( t ) );

		final SequenceDescriptionMinimal seq =
				new SequenceDescriptionMinimal(
						new TimePoints( timePoints ), setups, imgLoader, null );

		Map< Integer, ExportMipmapInfo > perSetupExportMipmapInfo;
		perSetupExportMipmapInfo = new HashMap<>();
		final ExportMipmapInfo mipmapInfo =
				new ExportMipmapInfo(
						autoMipmapSettings.getExportResolutions(),
						autoMipmapSettings.getSubdivisions() );

		for ( final BasicViewSetup setup : seq.getViewSetupsOrdered() )
			perSetupExportMipmapInfo.put( setup.getId(), mipmapInfo );

		final int numCellCreatorThreads = Math.max( 1, PluginHelper.numThreads() - 1 );
		ExportScalePyramid.LoopbackHeuristic loopbackHeuristic =
				( originalImg,
				  factorsToOriginalImg,
				  previousLevel,
				  factorsToPreviousLevel,
				  chunkSize ) ->
		{
			if ( previousLevel < 0 )
				return false;

			if ( Intervals.numElements( factorsToOriginalImg ) / Intervals.numElements( factorsToPreviousLevel ) >= 8 )
				return true;

			return false;
		};

		final ExportScalePyramid.AfterEachPlane afterEachPlane = usedLoopBack ->
		{ };

		final ArrayList< Partition > partitions;
		partitions = null;
		WriteSequenceToHdf5.writeHdf5File(
				seq,
				perSetupExportMipmapInfo,
				true,
				hdf5File,
				loopbackHeuristic,
				afterEachPlane,
				numCellCreatorThreads,
				new SubTaskProgressWriter( progressWriter, 0, 0.95 ) );


		writeXml( hdf5File, xmlFile, progressWriter,
				numTimePoints, numChannels, sourceTransform, seq, partitions );


		progressWriter.out().println( "done" );
	}

	private AffineTransform3D getSourceTransform3D(
			double[] calibration,
			double[] translation )
	{
		final AffineTransform3D sourceTransform = new AffineTransform3D();

		sourceTransform.set(
				calibration[ 0 ], 0, 0, 0,
				0, calibration[ 1 ], 0, 0,
				0, 0, calibration[ 2 ], 0 );

		sourceTransform.translate( translation );
		return sourceTransform;
	}

	private void writeXml(
			File hdf5File,
			File xmlFile,
			ProgressWriter progressWriter,
			int numTimepoints,
			int numChannels,
			AffineTransform3D sourceTransform,
			SequenceDescriptionMinimal seq,
			ArrayList< Partition > partitions )
	{
		// write xml sequence description
		final Hdf5ImageLoader hdf5Loader = new Hdf5ImageLoader(
				hdf5File, partitions, null, false );
		final SequenceDescriptionMinimal seqh5 = new SequenceDescriptionMinimal( seq, hdf5Loader );

		final ArrayList< ViewRegistration > registrations = new ArrayList<>();
		for ( int t = 0; t < numTimepoints; ++t )
			for ( int s = 0; s < numChannels; ++s )
				registrations.add( new ViewRegistration( t, s, sourceTransform ) );

		final File basePath = xmlFile.getParentFile();
		final SpimDataMinimal spimData =
				new SpimDataMinimal( basePath, seqh5, new ViewRegistrations( registrations ) );

		try
		{
			new XmlIoSpimDataMinimal().save( spimData, xmlFile.getAbsolutePath() );
			progressWriter.setProgress( 1.0 );
		}
		catch ( final Exception e )
		{
			throw new RuntimeException( e );
		}
	}

	private String getPixelUnit( String calibrationUnit )
	{
		String punit = calibrationUnit;
		if ( punit == null || punit.isEmpty() ) punit = "px";
		return punit;
	}

	private FinalDimensions getFinalDimensions( RandomAccessibleInterval< T > rai )
	{
		long[] dimensions = new long[ rai.numDimensions() ];
		rai.dimensions( dimensions );
		return new FinalDimensions( dimensions );
	}

	class RaiXYZCTLoader implements BasicImgLoader
	{
		final RandomAccessibleInterval< ? > raiXYZCT;
		final double[] calibration;
		final String calibrationUnit;

		public RaiXYZCTLoader( RandomAccessibleInterval< ? > raiXYZCT,
							   double[] calibration,
							   String calibrationUnit )
		{
			this.raiXYZCT = raiXYZCT;
			this.calibration = calibration;
			this.calibrationUnit = calibrationUnit;
		}

		@Override
		public BasicSetupImgLoader< UnsignedShortType > getSetupImgLoader( int setupId )
		{
			return new SetupImgLoader< UnsignedShortType >()
			{

				@Override
				public RandomAccessibleInterval< FloatType > getFloatImage(
						int timepointId, boolean normalize, ImgLoaderHint... hints )
				{
					return null;
				}

				@Override
				public Dimensions getImageSize( int timepointId )
				{
					return new FinalDimensions(
									raiXYZCT.dimension( 0 ),
									raiXYZCT.dimension( 1 ),
									raiXYZCT.dimension( 2 ) );
				}

				@Override
				public VoxelDimensions getVoxelSize( int timepointId )
				{
					return new VoxelDimensions()
					{
						@Override
						public String unit()
						{
							return calibrationUnit;
						}

						@Override
						public void dimensions( double[] dimensions )
						{
							for ( int d = 0; d < dimensions.length; ++d )
								dimensions[ d ] = calibration[ d ];
						}

						@Override
						public double dimension( int d )
						{
							return calibration[ d ];
						}

						@Override
						public int numDimensions()
						{
							return 3;
						}
					};
				}

				@Override
				public RandomAccessibleInterval< UnsignedShortType >
				getImage( int timepointId, ImgLoaderHint... hints )
				{

					final RandomAccessibleInterval< ? > raiXYZ
							= Views.hyperSlice(
									Views.hyperSlice( raiXYZCT, TIME_DIM, timepointId ),
										CHANNEL_DIM, setupId );


					if ( Util.getTypeFromInterval( raiXYZ ) instanceof UnsignedShortType )
					{
						return (RandomAccessibleInterval) raiXYZ;
					}
					else if ( Util.getTypeFromInterval( raiXYZ ) instanceof UnsignedByteType )
					{
						return Converters.convert(
								raiXYZ,
								( i, o ) -> o.setInteger( ((UnsignedByteType) i).get() ),
								new UnsignedShortType( ) );
					}
					else
					{
						return null;
					}
				}

				@Override
				public UnsignedShortType getImageType()
				{
					return new UnsignedShortType();
				}
			};
		}
	}

}
