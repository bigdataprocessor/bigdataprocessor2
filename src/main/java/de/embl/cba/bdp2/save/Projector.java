package de.embl.cba.bdp2.save;

/** ProjectionXYZ.java
 *
 * modified version of XYZ_MaxProject.java, written by Peter Sebastian Masny.
 *
 * === original description ===
 * Makes a composite image with x,y, and z projections.  Comparisons
 * were were made using the getPixelValue() method to compare.
 *
 * Created on July 30, 2005, 2:28 PM
 * ==== End of Original Description
 *
 * distribution URL was (accessed: 20120216)
 * http://www.masny.dk/imagej/max_xyz_project/index.php
 *
 * Modified for using it as a library.
 * - add constructors
 * - methods for input / output resulting images.
 * - scaling of z axis in xz and yz images according to xy scale (default = true)
 * Kota Miura (miura@embl.de), 20120216
 * http://cmci.embl.de
 *
 */

import de.embl.cba.bdp2.log.Logger;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileSaver;
import ij.measure.Calibration;
import ij.process.Blitter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class Projector
{
    public static final String MAX = "max";
    public static final String SUM = "sum";

    final static int FRAME_WIDTH = 0; // frame for boundary between xy - xz and yz
    private ImagePlus imp;
    private boolean doscale = true;
    private double xscale;
    private double yscale;
    private double zscale;
    private double zf = 1;			//zfactor

    public Projector( ImagePlus imp ){
        this.imp = imp;
        getZfactor();
        setDoscale(true);
    }

    public static void saveProjections( ImagePlus imp, int c, int t, String path, String projectionMode )
    {
        Projector Projector = new Projector( imp );
        Projector.setDoscale( true );
        ImagePlus projection = createProjection( projectionMode, Projector );
        saveAsTIFF( c, t, path, projection );
    }

    private static ImagePlus createProjection( String projectionMode, Projector Projector )
    {
        ImagePlus projection = null;
        switch ( projectionMode )
        {
            case MAX:
                projection = Projector.createMaxProjection();
                break;
            case SUM:
                projection = Projector.createSumProjection();
                break;
        }
        return projection;
    }

    public static void maxProjectAndSaveAsTIFF( ImagePlus imp, int c, int t, String path ) {

        Projector Projector = new Projector( imp );
        Projector.setDoscale( true );
        ImagePlus projection = Projector.createMaxProjection();

        saveAsTIFF( c, t, path, projection );
    }

    private static void saveAsTIFF( int c, int t, String path, ImagePlus projection )
    {
        FileSaver fileSaver = new FileSaver( projection );
        String sC = String.format("%1$02d", c );
        String sT = String.format("%1$05d", t );
        String pathCT = path + "-xyz-max" + "--C" + sC + "--T" + sT + ".tif";
        fileSaver.saveAsTiff(pathCT);
    }

    void getZfactor(){
        Calibration calib = imp.getCalibration();
        this.xscale = calib.pixelWidth;
        this.yscale = calib.pixelHeight;
        this.zscale = calib.pixelDepth;
        if (this.xscale != this.yscale){
            //Logger.debug("x and y scale is not same: use only x for calculating z factor.");
        }
        this.zf = this.zscale / this.xscale;
        //Logger.debug("Z factor set to " + Double.toString(this.zf));
    }

    public ImagePlus createMaxProjection()
    {
        // Get image info and setups
        //ImagePlus img = WindowManager.getCurrentImage();
        if (imp.getImageStackSize() < 2){
            Logger.warn("The image is not a stack: aborted XYZ projection");
            return null;
        }
        ImageStack stk = imp.getStack();

        int x = stk.getWidth();
        int y = stk.getHeight();
        int z = stk.getSize();
        //Logger.debug("Z-size: " + Integer.toString(z));
        // Create xy Processor with room for xz and yz
        ImageProcessor outxz = stk.getProcessor(1).createProcessor(x,z);
        ImageProcessor outyz = stk.getProcessor(1).createProcessor(z,y);
        ImageProcessor outxy = stk.getProcessor(1).createProcessor(x,y);

        for (int iz = 1; iz <= z; iz++){
            ImageProcessor currentPlane = stk.getProcessor(iz);
            for (int ix = 0; ix < x; ix++){
                for (int iy = 0; iy < y; iy++){
                    float pixel = currentPlane.getPixelValue(ix,iy);
                    if ((pixel > outxy.getPixelValue(ix,iy)) || (iz ==1)){
                        outxy.putPixel(ix,iy,currentPlane.getPixel(ix,iy));
                    }
                    if ((pixel > outxz.getPixelValue(ix,iz)) || (iy ==0)){
                        outxz.putPixel(ix, iz,currentPlane.getPixel(ix,iy));
                    }
                    if ((pixel > outyz.getPixelValue(iz,iy)) || (ix ==0)) {
                        outyz.putPixel(iz, iy,currentPlane.getPixel(ix,iy));
                    }
                }
            }
        }
        ImagePlus xyzimp = stitch( stk, x, y, outxz, outyz, outxy );
        return xyzimp;
    }


    public ImagePlus createSumProjection()
    {
        // Get image info and setups
        //ImagePlus img = WindowManager.getCurrentImage();
        if (imp.getImageStackSize() < 2){
            Logger.warn("The image is not a stack: aborted XYZ projection");
            return null;
        }
        ImageStack stk = imp.getStack();

        int x = stk.getWidth();
        int y = stk.getHeight();
        int z = stk.getSize();

        ImageProcessor outxz = new FloatProcessor(x,z);
        ImageProcessor outyz = new FloatProcessor(z,y);
        ImageProcessor outxy = new FloatProcessor(x,y);

        for (int iz = 1; iz <= z; iz++){
            ImageProcessor currentPlane = stk.getProcessor(iz);
            for (int ix = 0; ix < x; ix++){
                for (int iy = 0; iy < y; iy++){
                    float value = currentPlane.getPixelValue(ix,iy);
                    outxy.putPixelValue(ix, iy, outxy.getPixelValue(ix, iy) + value);
                    outxz.putPixelValue(ix, iz, outxz.getPixelValue(ix, iz) + value);
                    outyz.putPixelValue(iz, iy, outyz.getPixelValue(iz, iy) + value);
                }
            }
        }

        ImagePlus xyzimp = stitch( stk, x, y, outxz, outyz, outxy );
        return xyzimp;
    }

    public ImagePlus stitch( ImageStack stk, int x, int y, ImageProcessor outxz, ImageProcessor outyz, ImageProcessor outxy )
    {
        if ( doscale )
        {
//            Logger.debug("Z factor used:" + Double.toString(this.zf));
            double newzsize = ( ( double ) outxz.getHeight() ) * this.zf;
//            Logger.debug("...original size:" + Integer.toString(outxz.getHeight()));
//            Logger.debug("...new size:" + Double.toString(newzsize));
            outxz.setInterpolationMethod( ImageProcessor.BILINEAR );
            outxz = outxz.resize( outxz.getWidth(), ( int ) Math.round( newzsize ) );
            outyz.setInterpolationMethod( ImageProcessor.BILINEAR );
//            Logger.debug("YZ width Before Scaling: " + Integer.toString(outyz.getWidth()));
            outyz = outyz.resize( ( int ) Math.round( newzsize ), outyz.getHeight() );
//            Logger.debug("scaled XZ and YZ");
//            Logger.debug("YZ width After Scaling: " + Integer.toString(outyz.getWidth()));
        }
        ImageProcessor output = stk.getProcessor( 1 ).
                createProcessor(
                        x + outyz.getWidth() + FRAME_WIDTH,
                        y + outxz.getHeight() + FRAME_WIDTH );
        output.copyBits( outxy, 0, 0, Blitter.COPY );
        output.copyBits( outxz, 0, y + FRAME_WIDTH, Blitter.COPY );
        output.copyBits( outyz, x + FRAME_WIDTH, 0, Blitter.COPY );
        ImagePlus xyzimp = new ImagePlus( "XYZ_Max_Projection", output );
        Calibration calib = imp.getCalibration();
        xyzimp.setCalibration( calib.copy() );
        return xyzimp;
    }

    /**
     * @param doscale the doscale to set
     */
    public void setDoscale(boolean doscale) {
        this.doscale = doscale;
    }

    /**
     * @return the doscale
     */
    public boolean isDoscale() {
        return doscale;
    }

}