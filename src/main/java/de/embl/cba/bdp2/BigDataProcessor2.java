/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2023 EMBL
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
package de.embl.cba.bdp2;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.log.progress.LoggingProgressListener;
import de.embl.cba.bdp2.log.progress.Progress;
import de.embl.cba.bdp2.log.progress.ProgressListener;
import de.embl.cba.bdp2.open.bioformats.BioFormatsCachedCellImgCreator;
import de.embl.cba.bdp2.open.fileseries.FileInfos;
import de.embl.cba.bdp2.open.fileseries.FileSeriesCachedCellImgCreator;
import de.embl.cba.bdp2.process.align.channelshift.ChannelShifter;
import de.embl.cba.bdp2.process.align.splitchip.SplitChipCommand;
import de.embl.cba.bdp2.process.align.splitchip.SplitChipMerger;
import de.embl.cba.bdp2.process.bin.BinCommand;
import de.embl.cba.bdp2.process.bin.Binner;
import de.embl.cba.bdp2.process.calibrate.CalibrationChecker;
import de.embl.cba.bdp2.process.convert.MultiChannelUnsignedByteTypeConverter;
import de.embl.cba.bdp2.process.crop.CropCommand;
import de.embl.cba.bdp2.process.crop.Cropper;
import de.embl.cba.bdp2.process.transform.ImageTransformer;
import de.embl.cba.bdp2.save.ImageSaver;
import de.embl.cba.bdp2.save.ImageSaverFactory;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.service.ImageViewerService;
import de.embl.cba.bdp2.track.Track;
import de.embl.cba.bdp2.track.TrackApplier;
import de.embl.cba.bdp2.track.Tracks;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewer.ImageViewer;
import loci.common.DebugTools;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import ome.units.quantity.Length;
import ome.units.unit.Unit;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BigDataProcessor2
{
    public static ExecutorService threadPool = Executors.newFixedThreadPool( Runtime.getRuntime().availableProcessors() * 2  );
    public static int MAX_THREAD_LIMIT = Runtime.getRuntime().availableProcessors() * 2;

    public static < R extends RealType< R > & NativeType< R > >
    Image< R > bin( Image< R > image, long[] spanXYZCT )
    {
        Logger.info( "# " + BinCommand.COMMAND_NAME );
        Logger.info( "Binning: " + Arrays.toString( spanXYZCT ) );
        return Binner.bin( image, spanXYZCT );
    }

    public static < R extends RealType< R > & NativeType< R > >
    Image< R > openBioFormats( String filePath, int seriesIndex )
    {
        DebugTools.setRootLevel( "OFF" ); // Bio-Formats
        BioFormatsCachedCellImgCreator< R > cellImgCreator = new BioFormatsCachedCellImgCreator<>( filePath, seriesIndex );

        int seriesCount = cellImgCreator.getSeriesCount();

        if ( seriesIndex > seriesCount - 1 )
        {
            throw new RuntimeException( "Cannot open series index " + seriesIndex + " (zero-based) because the file only contains " + seriesCount + " image series" );
        }

        if ( seriesCount > 1 )
        {
            Logger.info( "File contains " + seriesCount + " image series" );
            Logger.info( "Now, opening series index " + seriesIndex );
            Logger.info( "To open another series please select an index from 0 - " + (seriesCount - 1) );
        }

        Image< R > image = new Image<>( cellImgCreator );
        image.supportsMultiThreadedReading( false ); // TODO: ??
        return image;
    }

    public static < R extends RealType< R > & NativeType< R > >
    Image< R > openTIFFSeries( File directory, String regExp )
    {
        return openTIFFSeries( directory.getAbsolutePath(), regExp );
    }

    public static < R extends RealType< R > & NativeType< R > >
    Image< R > openTIFFSeries( String directory, String regExp )
    {
        FileInfos fileInfos = new FileInfos( directory, regExp );
        FileSeriesCachedCellImgCreator< R > cachedCellImgCreator = new FileSeriesCachedCellImgCreator( fileInfos );
        Image< R > image = new Image( cachedCellImgCreator );
        image.supportsMultiThreadedReading( true );
        return image;
    }

    public static < R extends RealType< R > & NativeType< R > >
    Image< R > openTIFFSeries( String directory, String regularExpression, String[] channelSubset )
    {
        FileInfos fileInfos = new FileInfos( directory, regularExpression, channelSubset );
        FileSeriesCachedCellImgCreator< R > cachedCellImgCreator = new FileSeriesCachedCellImgCreator( fileInfos );
        Image< R > image = new Image( cachedCellImgCreator );
        image.supportsMultiThreadedReading( true );
        return image;
    }

    public static < R extends RealType< R > & NativeType< R > >
    Image< R > openTIFFSeries( String directory, String[] relativeFilePaths, String regularExpression, String[] channelSubset )
    {
        FileInfos fileInfos = new FileInfos( directory, regularExpression, null, channelSubset, relativeFilePaths );
        FileSeriesCachedCellImgCreator< R > cachedCellImgCreator = new FileSeriesCachedCellImgCreator( fileInfos );
        Image< R > image = new Image( cachedCellImgCreator );
        image.supportsMultiThreadedReading( true );
        return image;
    }

    public static < R extends RealType< R > & NativeType< R > >
    Image< R > crop( Image< R > image, Interval intervalXYZCT )
    {
        Logger.info( "# " + CropCommand.COMMAND_NAME );
        Logger.info( "Crop: " + intervalXYZCT );
        return Cropper.crop5D( image, intervalXYZCT );
    }

    public static < R extends RealType< R > & NativeType< R > > Image< R > crop( Image< R > image, long[] minMax )
    {
        return crop( image, Intervals.createMinMax( minMax ) );
    }

    public static < R extends RealType< R > & NativeType< R > >
    Image< R > openHDF5Series( String directory, String regularExpression, String hdf5DataSetName )
    {
        FileInfos fileInfos = new FileInfos( directory, regularExpression, hdf5DataSetName );
        FileSeriesCachedCellImgCreator< R > cachedCellImgCreator = new FileSeriesCachedCellImgCreator( fileInfos );
        Image< R > image = new Image( cachedCellImgCreator );
        image.supportsMultiThreadedReading( false );
        return image;
    }

    public static < R extends RealType< R > & NativeType< R > >
    Image< R > openHDF5Series( String directory, String[] relativeFilePaths, String regExp, String hdf5DataSetPath, String[] channelSubset )
    {
        FileInfos fileInfos = new FileInfos( directory, regExp, hdf5DataSetPath, channelSubset, relativeFilePaths );
        FileSeriesCachedCellImgCreator< R > cachedCellImgCreator = new FileSeriesCachedCellImgCreator( fileInfos );
        Image< R > image = new Image( cachedCellImgCreator );
        image.supportsMultiThreadedReading( false );
        return image;
    }

    public static < R extends RealType< R > & NativeType< R > >
    Image< R > openHDF5Series( String directory, String regExp, String hdf5DataSetPath, String[] channelSubset )
    {
        FileInfos fileInfos = new FileInfos( directory, regExp, hdf5DataSetPath, channelSubset );
        FileSeriesCachedCellImgCreator< R > cachedCellImgCreator = new FileSeriesCachedCellImgCreator( fileInfos );
        Image< R > image = new Image( cachedCellImgCreator );
        image.supportsMultiThreadedReading( false );
        return image;
    }

    public static < R extends RealType< R > & NativeType< R > >
    ImageViewer showImage( Image< R > image, boolean autoContrast, boolean enableArbitraryPlaneSlicing )
    {
        return new ImageViewer( image, autoContrast, enableArbitraryPlaneSlicing );
    }

    public static < R extends RealType< R > & NativeType< R > >
    ImageViewer showImage( Image< R > image, boolean autoContrast )
    {
        return new ImageViewer( image, autoContrast, ImageViewer.enableArbitraryPlaneSlicing );
    }

    public static < R extends RealType< R > & NativeType< R > >
    ImageViewer showImage( Image< R > image )
    {
        return new ImageViewer( image, true, ImageViewer.enableArbitraryPlaneSlicing );
    }

    public static< R extends RealType< R > & NativeType< R > > void saveImageAndWaitUntilDone( Image< R > image, SavingSettings savingSettings )
    {
        final LoggingProgressListener progressListener = new LoggingProgressListener( "Progress" );
        saveImage( image, savingSettings, progressListener );
        Logger.log( "Saving: " + savingSettings.volumesFilePathStump );
        Progress.waitUntilDone( progressListener, 1000 );
        Logger.log("Saving: Done." );
    }

    public static < R extends RealType< R > & NativeType< R > > ImageSaver saveImage( Image< R > image, SavingSettings settings, ProgressListener progressListener )
    {
        Logger.info( "\n# Save" );
        Logger.info( "I/O threads: " + settings.numIOThreads );
        Logger.info( "Processing threads: " + settings.numProcessingThreads );
        Logger.info( "File type: " + settings.fileType );
        Logger.info( "Save volumes as: " + settings.volumesFilePathStump + "*" );
        if ( settings.saveProjections )
        Logger.info( "Save projections to: " + settings.projectionsFilePathStump );

        if ( ! CalibrationChecker.checkVoxelUnit( image.getVoxelUnit() ) )
            throw new RuntimeException( "Voxel unit not set; please set using image.setVoxelUnit( ... )" );

        if ( ! CalibrationChecker.checkVoxelDimension( image.getVoxelDimensions() ) )
            throw new RuntimeException( "Voxel dimension not set; please set using image.setVoxelDimension( ... )" );

        final ImageSaver saver = new ImageSaverFactory().getSaver( image, settings );
        saver.addProgressListener( progressListener );
        saver.createOutputDirectories( settings );
        saver.startSave();

        return saver;
    }

    public static < R extends RealType< R > & NativeType< R > >
    Image< R > convertToUnsignedByteType( Image< R > image, List< double[] > contrastLimits )
    {
        MultiChannelUnsignedByteTypeConverter< R > converter = new MultiChannelUnsignedByteTypeConverter<>( image, contrastLimits );
        return converter.getConvertedImage();
    }

    public static < R extends RealType< R > & NativeType< R > >
    Image< R > convertToUnsignedByteType( Image< R > image, double[] min, double[] max )
    {
        ArrayList< double[] > contrastLimits = new ArrayList<>();
        for ( int c = 0; c < min.length; c++ )
            contrastLimits.add( new double[]{ min[c], max[c] });

        return convertToUnsignedByteType( image, contrastLimits );
    }

    public static < R extends RealType< R > & NativeType< R > > Image< R > setVoxelSize( Image< R > image, double[] voxelSizes, String voxelUnit )
    {
        Image< R > outputImage = new Image<>( image );
        outputImage.setVoxelDimensions( voxelSizes );
        // TODO: convert this to a Unit< Length >
        outputImage.setVoxelUnit( voxelUnit );
        return outputImage;
    }

    public static < R extends RealType< R > & NativeType< R > > Image< R > setVoxelSize( Image< R > image, double[] voxelSizes, Unit< Length > voxelUnit  )
    {
        Image< R > outputImage = new Image<>( image );
        outputImage.setVoxelDimensions( voxelSizes );
        outputImage.setVoxelUnit( voxelUnit );
        return outputImage;
    }

    public static < R extends RealType< R > & NativeType< R > > Image< R > alignChannels( Image< R > image, List< long[] > shifts )
    {
        final ChannelShifter< R > shifter = new ChannelShifter< >( image.getRai() );
        RandomAccessibleInterval< R > shiftedRai = shifter.getShiftedRai( shifts );

        Image< R > outputImage = new Image( image );
        outputImage.setRai( shiftedRai );

        return outputImage;
    }

    public static < R extends RealType< R > & NativeType< R > >
    Image< R > mergeRegionsXYC( Image< R > image, List< long [] > regionsXYminXYdimC )
    {
        Logger.info( "# " + SplitChipCommand.COMMAND_NAME );
        final Image< R > outputImage = SplitChipMerger.mergeRegionsXYC( image, regionsXYminXYdimC );
        return outputImage;
    }

    public static < R extends RealType< R > & NativeType< R > > Image< R > rename( Image< R > image, String name, String[] channelNames )
    {
        Image< R > outputImage = new Image<>( image );
        outputImage.setName( name );
        outputImage.setChannelNames( channelNames );
        return outputImage;
    }

    public static < R extends RealType< R > & NativeType< R > > Image< R > transform( Image< R > image, AffineTransform3D transform3D, InterpolatorFactory interpolatorFactory )
    {
        final ImageTransformer< R > transformer = new ImageTransformer<>( image, transform3D, interpolatorFactory );
        return transformer.transform();
    }

    public static < R extends RealType< R > & NativeType< R > > Image< R > transform( Image< R > image, double[] affineTransformValues, String interpolationMode )
    {
        InterpolatorFactory interpolator = Utils.getInterpolator( interpolationMode );
        final AffineTransform3D affineTransform3D = new AffineTransform3D();
        affineTransform3D.set( affineTransformValues );

        return transform( image, affineTransform3D, interpolator );
    }

    public static < R extends RealType< R > & NativeType< R > > Image< R > applyTrack( File file, Image< R > image, Boolean centerImage )
	{
		final Track track = Tracks.fromJson( file );
		final TrackApplier< R > trackApplier = new TrackApplier<>( image );

		Image outputImage = trackApplier.applyTrack( track );
		if ( ! centerImage )
			outputImage.setRai( Views.zeroMin( outputImage.getRai() ) );

		return outputImage;
	}

    public static < R extends RealType< R > & NativeType< R > > ImageViewer showImage( Image< R > image, Image< R > parentImage )
    {
        final ImageViewer viewer = showImage( image, false );

        // fetch display settings of the parentImage and apply them
        final ImageViewer inputImageViewer = ImageViewerService.imageNameToBdvImageViewer.get( parentImage.getName() );
        if ( inputImageViewer != null )
                viewer.setDisplaySettings( inputImageViewer.getDisplaySettings() );

        return viewer;
    }
}
