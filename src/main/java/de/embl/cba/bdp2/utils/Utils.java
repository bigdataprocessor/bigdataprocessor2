/*
 * #%L
 * Data streaming, track and cropping tools
 * %%
 * Copyright (C) 2017 Christian Tischer
 *
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

import bdv.util.Bdv;
import bdv.viewer.animate.SimilarityTransformAnimator;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.files.FileInfos;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.splitviewmerge.SplitViewMerger;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.dialog.DisplaySettings;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.plugin.Binner;
import ij.plugin.Duplicator;
import ij.process.ImageProcessor;
import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.FinalRealInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgView;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Util;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Created by tischi on 06/11/16.
 */

public class Utils {

    public static boolean verbose = false;

	public static ImagePlus getDataCube(ImagePlus image, int c, int t, int[] binning )
	{
        ImagePlus dataCube; //= getFullStackFromInfo(c,t,files);

//        if( image.getStack() instanceof VirtualStackOfStacks )
//        {
//            VirtualStackOfStacks vss = (VirtualStackOfStacks) image.getStack();
//            dataCube = vss.getFullFrame( c, t, 1 );
//        }
//        else
//        {
        dataCube = new Duplicator().run( image, c + 1, c + 1, 1, image.getNSlices(), t + 1, t + 1 );

		if ( binning[ 0 ] > 1 || binning[ 1 ] > 1 || binning[ 2 ] > 1 ){
			Binner binner = new Binner();
			dataCube = binner.shrink( dataCube, binning[0], binning[1], binning[2], binner.AVERAGE );
		}
		return dataCube;
	}

	public static double getSizeGB( RandomAccessibleInterval rai )
	{
		final int n = rai.numDimensions();
		final long[] dims = new long[ n ];
		rai.dimensions( dims );

		double sizeGB = 1;

		for ( int d = 0; d < n; d++ )
			sizeGB *= dims[ d ];

		final Object type = Util.getTypeFromInterval( rai );
		if ( type instanceof UnsignedByteType )
			sizeGB *= 8;
		else if ( type instanceof UnsignedShortType )
			sizeGB *= 16;

		sizeGB /= 8.0 * 1000000000L;
		return sizeGB;
	}

	public static void createFilePathParentDirectories( String filePath )
	{
		try
		{
			Files.createDirectories(
				Paths.get( new File( filePath ).getParent() ) );
		} catch ( IOException e )
		{
            System.err.println( e );
		}
	}

    public static void sleepMillis( int millis )
    {
        try
        {
            Thread.sleep( millis );
        } catch ( InterruptedException e )
        {
            e.printStackTrace();
        }
    }

    public static < R extends RealType< R > & NativeType< R > >
    Image< R > openMergedImageFromLuxendoChannelFolders(
			String voxelUnit,
			double voxelSpacingMicrometerX,
			double voxelSpacingMicrometerY,
			double voxelSpacingMicrometerZ,
			SplitViewMerger merger,
			File directory )
	{
		final String subFolderPattern = directory.getName().replace( "channel_0", "channel_.*" );
		final String parentFolder = directory.getParent();

		final Image< R > image = BigDataProcessor2.openHdf5Image(
				parentFolder,
				FileInfos.LOAD_CHANNELS_FROM_FOLDERS,
				subFolderPattern + File.separator + FileInfos.PATTERN_LUXENDO ,
				"Data" );

		image.setVoxelUnit( voxelUnit );
		image.setVoxelSpacing(
				voxelSpacingMicrometerX,
				voxelSpacingMicrometerY,
				voxelSpacingMicrometerZ );

		return merger.mergeIntervalsXYC( image );
	}

	public static < R extends RealType< R > & NativeType< R > >
	void showVolumeInImageJ1( RandomAccessibleInterval< R > rai, String title )
	{
		final RandomAccessibleInterval< R > permute = Views.permute(
				Views.addDimension( rai, 0, 0 ),
				2, 3 );

		ImageJFunctions.wrap( permute, title ).show();
	}

	public static void collectFutures( ExecutorService executorService, ArrayList< Future > futures, AtomicBoolean stop )
	{
		for ( Future future : futures )
		{
			try
			{
				future.get();
			} catch ( InterruptedException e )
			{
				e.printStackTrace();
			} catch ( ExecutionException e )
			{
				e.printStackTrace();
			}

			if ( stop.get() )
			{
				for ( Future f : futures )
					f.cancel( true );

				executorService.shutdownNow();
				return;
			}
		}

		executorService.shutdown();
	}

	public static String longsToDelimitedString( ArrayList< long[] > shifts )
	{
		final String collect = shifts.stream().map( t ->
				Arrays.stream( t ).mapToObj( x -> String.valueOf( x ) )
						.collect( Collectors.joining( "," ) ) )
						.collect( Collectors.joining( ";" ) );

		return collect;
	}

	public static List< long[] > delimitedStringToLongs( String string )
	{
		final List< long[] > shifts = Arrays.stream( string.split( ";" ) )
				.map( t -> Arrays.stream( t.split( "," ) )
						.mapToLong( Long::parseLong )
						.toArray()
				).collect( Collectors.toList() );

		return shifts;
	}

	public enum FileType {
        HDF5("Hdf5"),
        HDF5_IMARIS_BDV("Partitioned Imaris"),
        TIFF_STACKS("Tiff stacks"),
        SINGLE_PLANE_TIFF("Single plane Tiff"); //SERIALIZED_HEADERS("Serialized headers");
        private final String text;
        private FileType(String s)
        {
            text = s;
        }
        @Override
        public String toString() {
            return text;
        }
    }


    public enum ImageFilterTypes {
        NONE("None"),
        THRESHOLD("Threshold"),
        VARIANCE("Variance");
        private final String text;
        private ImageFilterTypes(String s) {
            text = s;
        }
        @Override
        public String toString() {
            return text;
        }
    }

    public static FinalInterval asIntegerInterval(FinalRealInterval realInterval ) {
        double[] realMin = new double[ 3 ];
        double[] realMax = new double[ 3 ];
        realInterval.realMin( realMin );
        realInterval.realMax( realMax );
        long[] min = new long[ 3 ];
        long[] max = new long[ 3 ];
        for ( int d = 0; d < 3; ++d ) {
            min[ d ] = (long) realMin[ d ];
            max[ d ] = (long) realMax[ d ];
        }
        return new FinalInterval( min, max );
    }

    public static String[] getNames(Class<? extends Enum<?>> e) {
        return Arrays.stream(e.getEnumConstants()).map(Enum::name).toArray(String[]::new);
    }


    public static void logArrayList(ArrayList<long[]> arrayList )
    {
        for ( long[] entry : arrayList )
        {
            Logger.info( "" + entry[0] + "," + entry[1] + "," + entry[2]);
        }
    }


    public static void centerBdvViewToPosition( double[] position, double scale, Bdv bdv )
    {
        final AffineTransform3D newViewerTransform = getNewViewerTransform( position, scale, bdv, null );

        final double cX = 0; //- bdv.getBdvHandle().getViewerPanel().getDisplay().getWidth() / 2.0;
        final double cY = 0; //- bdv.getBdvHandle().getViewerPanel().getDisplay().getHeight() / 2.0;

        final AffineTransform3D currentViewerTransform = new AffineTransform3D();
        bdv.getBdvHandle().getViewerPanel().getState().getViewerTransform( currentViewerTransform );

        final SimilarityTransformAnimator similarityTransformAnimator =
                new SimilarityTransformAnimator( currentViewerTransform, newViewerTransform, cX ,cY, 3000 );

        bdv.getBdvHandle().getViewerPanel().setTransformAnimator( similarityTransformAnimator );

        bdv.getBdvHandle().getViewerPanel().transformChanged( currentViewerTransform );
    }

    private static AffineTransform3D getNewViewerTransform( double[] position, double scale, Bdv bdv, AffineTransform3D currentViewerTransform )
    {
        final AffineTransform3D newViewerTransform = new AffineTransform3D();

//		bdv.getBdvHandle().getViewerPanel().getState().getViewerTransform( viewerTransform );
        int[] bdvWindowDimensions = new int[ 3 ];
        bdvWindowDimensions[ 0 ] = bdv.getBdvHandle().getViewerPanel().getWidth();
        bdvWindowDimensions[ 1 ] = bdv.getBdvHandle().getViewerPanel().getHeight();

        double[] translation = new double[ 3 ];
        for( int d = 0; d < 3; ++d ){
//			final double center = ( realInterval.realMin( d ) + realInterval.realMax( d ) ) / 2.0;
            translation[ d ] = - position[ d ];
        }
        newViewerTransform.setTranslation( translation );
        newViewerTransform.scale( scale );

        double[] translation2 = new double[ 3 ];

        for( int d = 0; d < 3; ++d ){
//			final double center = ( realInterval.realMin( d ) + realInterval.realMax( d ) ) / 2.0;
            translation2[ d ] = + bdvWindowDimensions[ d ] / 2.0;
        }

        newViewerTransform.translate( translation2 );

        return newViewerTransform;
    }


    private static < T extends RealType< T > & NativeType< T >> DisplaySettings computeDisplayRange(RandomAccessibleInterval< T > rai) {
        IntervalView<T> ts = Views.hyperSlice(rai, 2, (rai.max(2) - rai.min(2)) / 2 + rai.min(2)); //z is 2 for this rai.
        Cursor<T> cursor = Views.iterable(ts).cursor();

        double min = Double.MAX_VALUE;
        double max = - Double.MAX_VALUE;
        double value;
        while ( cursor.hasNext()){
            value = cursor.next().getRealDouble();
            if ( value < min ) min = value;
            if ( value > max ) max = value;
        }
        return new DisplaySettings( min, max, null );
    }

//    public static NativeType getNativeTypeFromRAI(RandomAccessibleInterval rai){ Method already exists in Imglib2 Util.getTypeFromInterval
//        NativeType nativeType = null;
//        try {
//            if( rai instanceof Img) {
//                nativeType = (NativeType) ((Img) rai).firstElement().getClass().newInstance();
//            }else if (rai instanceof IntervalView){
//                nativeType = (NativeType)((IntervalView) rai).firstElement().getClass().newInstance();
//            }
//        } catch (InstantiationException e1) {
//            e1.printStackTrace();
//        } catch (IllegalAccessException e1) {
//            e1.printStackTrace();
//        }
//        return nativeType;
//    }

    public static ImagePlus wrap5DRaiToCalibratedImagePlus(
            RandomAccessibleInterval raiXYZCT,
            double[] voxelSpacing,
            String unit,
            String name )
    {
        ImagePlus imp = ImageJFunctions.wrap(
                Views.permute( raiXYZCT, DimensionOrder.Z, DimensionOrder.C ), name);

        final Calibration calibration = new Calibration();
        calibration.setUnit( unit );
        calibration.pixelWidth = voxelSpacing[ 0 ];
        calibration.pixelHeight = voxelSpacing[ 1 ];
        calibration.pixelDepth = voxelSpacing[ 2 ];
        imp.setCalibration( calibration );

        return imp;
    }


    public static ImagePlus wrap3DRaiToCalibratedImagePlus(
            RandomAccessibleInterval raiXYZ,
            double[] voxelSpacing,
            String unit,
            String name )
    {
        final IntervalView viewXYZCT =
                Views.addDimension(
                        Views.addDimension( raiXYZ,
                                0, 0 ),
                        0, 0 );

        final IntervalView viewXYCZT =
                Views.permute( viewXYZCT, DimensionOrder.Z, DimensionOrder.C );

        ImagePlus imp = ImageJFunctions.wrap( viewXYCZT, name);

        final Calibration calibration = new Calibration();
        calibration.setUnit( unit );
        calibration.pixelWidth = voxelSpacing[ 0 ];
        calibration.pixelHeight = voxelSpacing[ 1 ];
        calibration.pixelDepth = voxelSpacing[ 2 ];
        imp.setCalibration( calibration );

        return imp;
    }

    public static String fixDirectoryFormat(String directory){
        directory = directory.trim();
	    char last = directory.charAt(directory.length()-1);
	    if(last != File.separatorChar){
            directory= directory + File.separator;
        }
        return directory;
	}

    public static < T extends RealType< T > & NativeType< T >> void applyIntensityGate (Img< T > rai, int[] gate) {
        int min = gate[0];
        int max = gate[1];
        if(!(min == -1 && max == -1)) {
            Cursor<T> cursor = rai.cursor();
            T val = rai.firstElement();
            if (val instanceof UnsignedShortType) {
                if (max == -1) {
                    max = Short.MAX_VALUE * 2 + 1;
                }
            } else if (val instanceof UnsignedByteType) {
                if (max == -1) {
                    max = Byte.MAX_VALUE * 2 + 1;
                }
            } else {
                return;
            }
            double value;
            while (cursor.hasNext()) {
                value = cursor.next().getRealDouble();
                if (value > max || value < min) {
                    cursor.get().setZero();
                } else {
                    value -= min;
                    cursor.get().setReal(value);
                }
            }
        }
    }

    public static boolean checkRange(ImagePlus imp, int min, int max, String dimension) {
        // setup
        //

        int Min = 0, Max = 0;

        if ( dimension.equals("z") )
        {
            Min = 1;
            Max = imp.getNSlices();
        }
        else if ( dimension.equals("t") )
        {
            Min = 1;
            Max = imp.getNFrames();
        }

        // check
        //

        if (min < Min)
        {
            Logger.error(""+dimension+" minimum must be >= " + Min + "; please change the value.");
            return false;
        }

        if (max > Max)
        {
            Logger.error(""+dimension+" maximum must be <= " + Max + "; please change the value.");
            return false;
        }


        return true;
    }


    /*
        Use when extracting full image from RandomAccessibleInterval extracted from a Bdv Handle
    */
    public static Img getCellImgFromInterval(RandomAccessibleInterval rai){
       NativeType nativeType =  Util.getTypeFromInterval(rai);
       Img imgTemp = ImgView.wrap(rai,new CellImgFactory<>(nativeType));
       return imgTemp;
    }
    public static void shutdownThreadPack(ExecutorService executorService,int timeOut){
        if(executorService !=null){
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(timeOut, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException ex) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    public static Point3D computeOffsetFromCenterSize(Point3D pCenter, Point3D pSize) {
        return(pCenter.subtract(pSize.subtract(1, 1, 1).multiply(0.5)));
    }

    public static Point3D computeCenterFromOffsetSize(Point3D pOffset, Point3D pSize) {
        // center of width 7 is 0,1,2,*3*,4,5,6
        // center of width 6 is 0,1,2,*2.5*,3,4,5
        return(pOffset.add(pSize.subtract(1, 1, 1).multiply(0.5)));
    }

    public static Point3D multiplyPoint3dComponents(Point3D p0, Point3D p1) {

        double x = p0.getX() * p1.getX();
        double y = p0.getY() * p1.getY();
        double z = p0.getZ() * p1.getZ();

        return (new Point3D(x,y,z));

    }

    public static void show( ImagePlus imp )
    {
        imp.show();
        imp.setPosition(1, imp.getNSlices() / 2, 1); // image.getNSlices() makes the starting slice 25!
        IJ.wait(200);
        imp.resetDisplayRange();
        imp.updateAndDraw();
    }


//
//    public static VirtualStackOfStacks getVirtualStackOfStacks( ImagePlus image) {
//        VirtualStackOfStacks vss = null;
//        try {
//            vss = (VirtualStackOfStacks) image.getStack();
//            return (vss);
//        } catch (Exception e) {
//             Logger.error("This is only implemented for images opened with the Data Streaming Tools plugin.");
//            return (null);
//        }
//    }

    public static double[] delimitedStringToDoubleArray(String s, String delimiter)
	{
        String[] sA = s.split(delimiter);
        double[] nums = new double[sA.length];
        for (int i = 0; i < nums.length; i++) {
            nums[i] = Double.parseDouble(sA[i].trim());
        }

        return nums;
    }

    public static int[] delimitedStringToIntegerArray(String s, String delimiter) {

        String[] sA = s.split(delimiter);
        int[] nums = new int[sA.length];
        for (int i = 0; i < nums.length; i++)
            nums[i] = Integer.parseInt(sA[i].trim());

        return nums;
    }

	public static String[] delimitedStringToStringArray( String s, String delimiter ) {

		s = s.trim();
		if( s.endsWith( delimiter ) )
			s = s.substring( 0, s.length() - 1 );

		String[] sA = s.split( delimiter );
		String[] strings = new String[ sA.length ];
		for (int i = 0; i < strings.length; i++)
			strings[i] = sA[i].trim();

		return strings;
	}

    public static void printMap(Map mp) {
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
              Logger.info("" + pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
    }

//    public static boolean hasVirtualStackOfStacks(ImagePlus image) {
//
//        if( ! (image.getStack() instanceof VirtualStackOfStacks) ) {
//             Logger.error("Wrong image type. " +
//                     "This method is only implemented for images opened via " +
//                     "the Data Streaming Tools plugin.");
//            return false;
//        }
//        else
//        {
//            return true;
//        }
//
//    }

    public static boolean checkMemoryRequirements( ImagePlus imp )
    {
        long numPixels = (long)imp.getWidth()*imp.getHeight()*imp.getNSlices()*imp.getNChannels()*imp.getNFrames();
        boolean ok = checkMemoryRequirements(numPixels, imp.getBitDepth(), 1);
        return(ok);
    }

    public static boolean checkMemoryRequirements(ImagePlus imp, int safetyFactor, int nThreads )
    {
        long numPixels = (long) imp.getWidth()*imp.getHeight()*imp.getNSlices();
        numPixels *= safetyFactor;
        boolean ok = checkMemoryRequirements( numPixels, imp.getBitDepth(), nThreads );
        return(ok);
    }

    public static String getChannelTimeString( int c, int t )
    {
        String s = String.format("--C%02d--T%05d", c, t);
        return ( s );
    }

    public static boolean checkMemoryRequirements(long numPixels, int bitDepth, int nThreads)
    {
        //
        // check that the data cube is not too large for the java indexing
        //
        long maxSize = (1L<<31) - 1;
        if( numPixels > maxSize )
        {
              Logger.info("Warning: " + "The size of one requested data cube is " + numPixels + " (larger than 2^31)\n");
            //Logger.error("The size of one requested data cube is "+numPixels +" (larger than 2^31)\n" +
            //        "and can thus not be loaded as one java array into RAM.\n" +
            //        "Please crop a smaller region.");
            //return(false);
        }

        //
        // check that the data cube(s) fits into the RAM
        //
        double GIGA = 1000000000.0;
        long freeMemory = IJ.maxMemory() - IJ.currentMemory();
        double maxMemoryGB = IJ.maxMemory()/GIGA;
        double freeMemoryGB = freeMemory/GIGA;
        double requestedMemoryGB = numPixels * bitDepth/8 * nThreads / GIGA;

        if( requestedMemoryGB > freeMemoryGB )
        {
            Logger.error("The operation you requested to perform " +
                     "might need up to " + requestedMemoryGB + " GB.\n" +
                     "The current free memory is only " + freeMemoryGB + " GB.\n" +
                     "Please consider cropping a smaller region \n" +
                     "and/or reducing the number of I/O threads \n" +
                     "(you are currently using " + nThreads + ").");
            return(false);
        }
        else
        {
            if( requestedMemoryGB > 0.1 ) {
                //Logger.info("Memory [GB]: Max=" + maxMemoryGB + "; Free=" + freeMemoryGB + "; Requested=" +
                //        requestedMemoryGB);
            }

        }



        return(true);

    }

//    public static ImagePlus getDataCube(ImagePlus image, Region5D region5D, int nThreads )
//    {
//        ImagePlus dataCube = null;
//
//        if( image.getStack() instanceof VirtualStackOfStacks )
//        {
//            VirtualStackOfStacks vss = (VirtualStackOfStacks) image.getStack();
//            dataCube = vss.getDataCube( region5D, nThreads );
//        }
//        else
//        {
//            dataCube = getDataCube( image, region5D );
//        }
//
//        //dataCube.replaceImage();
//
//        return( dataCube );
//    }


    public static ImagePlus bin(ImagePlus imp_, int[] binning_, String binningTitle, String method )
    {
        ImagePlus imp = imp_;
        int[] binning = binning_;
        String title = new String(imp.getTitle());
        Binner binner = new Binner();

        Calibration saveCalibration = imp.getCalibration().copy(); // this is due to a bug in the binner

        ImagePlus impBinned = null;

        switch( method )
        {
            case "OPEN":
                impBinned = binner.shrink(imp, binning[0], binning[1], binning[2], binner.MIN);
                //impBinned = binner.shrink(image, binning[0], binning[1], binning[2], binner.AVERAGE);
                //IJ.merge(impBinned, "Minimum 3D...", "x=1 y=1 z=1");
                //IJ.merge(impBinned, "Maximum 3D...", "x=1 y=1 z=1");
                impBinned.setTitle("Open_" + title);
                break;
            case "CLOSE":
                impBinned = binner.shrink(imp, binning[0], binning[1], binning[2], binner.MAX);
                //impBinned = binner.shrink(image, binning[0], binning[1], binning[2], binner.AVERAGE);
                //IJ.merge(impBinned, "Maximum 3D...", "x=1 y=1 z=1");
                //IJ.merge(impBinned, "Minimum 3D...", "x=1 y=1 z=1");
                impBinned.setTitle("Close_" + title);
                break;
            case "AVERAGE":
                impBinned = binner.shrink(imp, binning[0], binning[1], binning[2], binner.AVERAGE);
                impBinned.setTitle(binningTitle + "_" + title);
                break;
            case "MIN":
                impBinned = binner.shrink(imp, binning[0], binning[1], binning[2], binner.MIN);
                impBinned.setTitle(binningTitle + "_Min_" + title);
                break;
            case "MAX":
                impBinned = binner.shrink(imp, binning[0], binning[1], binning[2], binner.MAX);
                impBinned.setTitle(binningTitle + "_Max_" + title);
                break;
            default:
                IJ.showMessage("Error while binning; method not supported :"+method);
                break;
        }

        // reset calibration of input image
        // necessary due to a bug in the binner
        imp.setCalibration( saveCalibration );

        return ( impBinned );
}

    private Point3D compute16bitCenterOfMass(ImageStack stack, Point3D pMin, Point3D pMax)
    {

        final String centeringMethod = "center of mass";

        //long startTime = System.currentTimeMillis();
        double sum = 0.0, xsum = 0.0, ysum = 0.0, zsum = 0.0;
        int i, v;
        int width = stack.getWidth();
        int height = stack.getHeight();
        int depth = stack.getSize();
        int xmin = 0 > (int) pMin.getX() ? 0 : (int) pMin.getX();
        int xmax = (width-1) < (int) pMax.getX() ? (width-1) : (int) pMax.getX();
        int ymin = 0 > (int) pMin.getY() ? 0 : (int) pMin.getY();
        int ymax = (height-1) < (int) pMax.getY() ? (height-1) : (int) pMax.getY();
        int zmin = 0 > (int) pMin.getZ() ? 0 : (int) pMin.getZ();
        int zmax = (depth-1) < (int) pMax.getZ() ? (depth-1) : (int) pMax.getZ();

        // compute one-based, otherwise the numbers at x=0,y=0,z=0 are lost for the center of mass

        if (centeringMethod.equals("center of mass")) {
            for (int z = zmin + 1; z <= zmax + 1; z++) {
                ImageProcessor ip = stack.getProcessor(z);
                short[] pixels = (short[]) ip.getPixels();
                for (int y = ymin + 1; y <= ymax + 1; y++) {
                    i = (y - 1) * width + xmin; // zero-based location in pixel array
                    for (int x = xmin + 1; x <= xmax + 1; x++) {
                        v = pixels[i] & 0xffff;
                        // v=0 is ignored automatically in below formulas
                        sum += v;
                        xsum += x * v;
                        ysum += y * v;
                        zsum += z * v;
                        i++;
                    }
                }
            }
        }

        if (centeringMethod.equals("centroid")) {
            for (int z = zmin + 1; z <= zmax + 1; z++) {
                ImageProcessor ip = stack.getProcessor(z);
                short[] pixels = (short[]) ip.getPixels();
                for (int y = ymin + 1; y <= ymax + 1; y++) {
                    i = (y - 1) * width + xmin; // zero-based location in pixel array
                    for (int x = xmin + 1; x <= xmax + 1; x++) {
                        v = pixels[i] & 0xffff;
                        if (v > 0) {
                            sum += 1;
                            xsum += x;
                            ysum += y;
                            zsum += z;
                        }
                        i++;
                    }
                }
            }
        }

        // computation is one-based; result should be zero-based
        double xCenter = (xsum / sum) - 1;
        double yCenter = (ysum / sum) - 1;
        double zCenter = (zsum / sum) - 1;

        //long stopTime = System.currentTimeMillis(); long elapsedTime = stopTime - startTime;  Logger.info("center of mass in [ms]: " + elapsedTime);

        return(new Point3D(xCenter,yCenter,zCenter));
    }

    private Point3D compute8bitCenterOfMass(ImageStack stack, Point3D pMin, Point3D pMax)
    {

        final String centeringMethod = "center of mass";

        //long startTime = System.currentTimeMillis();
        double sum = 0.0, xsum = 0.0, ysum = 0.0, zsum = 0.0;
        int i, v;
        int width = stack.getWidth();
        int height = stack.getHeight();
        int depth = stack.getSize();
        int xmin = 0 > (int) pMin.getX() ? 0 : (int) pMin.getX();
        int xmax = (width-1) < (int) pMax.getX() ? (width-1) : (int) pMax.getX();
        int ymin = 0 > (int) pMin.getY() ? 0 : (int) pMin.getY();
        int ymax = (height-1) < (int) pMax.getY() ? (height-1) : (int) pMax.getY();
        int zmin = 0 > (int) pMin.getZ() ? 0 : (int) pMin.getZ();
        int zmax = (depth-1) < (int) pMax.getZ() ? (depth-1) : (int) pMax.getZ();

        // compute one-based, otherwise the numbers at x=0,y=0,z=0 are lost for the center of mass

        if (centeringMethod.equals("center of mass")) {
            for (int z = zmin + 1; z <= zmax + 1; z++) {
                ImageProcessor ip = stack.getProcessor(z);
                byte[] pixels = (byte[]) ip.getPixels();
                for (int y = ymin + 1; y <= ymax + 1; y++) {
                    i = (y - 1) * width + xmin; // zero-based location in pixel array
                    for (int x = xmin + 1; x <= xmax + 1; x++) {
                        v = pixels[i] & 0xff;
                        // v=0 is ignored automatically in below formulas
                        sum += v;
                        xsum += x * v;
                        ysum += y * v;
                        zsum += z * v;
                        i++;
                    }
                }
            }
        }

        if (centeringMethod.equals("centroid")) {
            for (int z = zmin + 1; z <= zmax + 1; z++) {
                ImageProcessor ip = stack.getProcessor(z);
                byte[] pixels = (byte[]) ip.getPixels();
                for (int y = ymin + 1; y <= ymax + 1; y++) {
                    i = (y - 1) * width + xmin; // zero-based location in pixel array
                    for (int x = xmin + 1; x <= xmax + 1; x++) {
                        v = pixels[i] & 0xff;
                        if (v > 0) {
                            sum += 1;
                            xsum += x;
                            ysum += y;
                            zsum += z;
                        }
                        i++;
                    }
                }
            }
        }

        // computation is one-based; result should be zero-based
        double xCenter = (xsum / sum) - 1;
        double yCenter = (ysum / sum) - 1;
        double zCenter = (zsum / sum) - 1;

        //long stopTime = System.currentTimeMillis(); long elapsedTime = stopTime - startTime;  Logger.info("center of mass in [ms]: " + elapsedTime);

        return(new Point3D(xCenter,yCenter,zCenter));
    }


}
