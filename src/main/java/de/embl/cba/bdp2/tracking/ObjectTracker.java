package de.embl.cba.bdp2.tracking;

import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.ui.BigDataProcessor2;
import de.embl.cba.bdp2.utils.DimensionOrder;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.utils.Point3D;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

public class ObjectTracker < R extends RealType< R > & NativeType< R > > extends AbstractObjectTracker{
    private final TrackingSettings< R > trackingSettings;
    private Point3D pMin, pMax;
    private final int width;
    private final int height;
    private final int depth;
    private final int channel;
    private final int timeFrames;
    private final int trackId;
    private int gateIntensityMin;
    private int gateGateIntensityMax;
    private AtomicBoolean stop;

    public ObjectTracker( TrackingSettings< R > trackingSettings, AtomicBoolean stop ) {
        this.trackingSettings = trackingSettings;
        this.pMin = trackingSettings.pMin;
        this.pMax = trackingSettings.pMax;
        this.width = (int) trackingSettings.rai.dimension( DimensionOrder.X );
        this.height = (int) trackingSettings.rai.dimension( DimensionOrder.Y );
        this.depth = (int) trackingSettings.rai.dimension( DimensionOrder.Z );
        this.channel = trackingSettings.channel;
        this.timeFrames =  trackingSettings.nt ==-1 ?
                (int)trackingSettings.rai.dimension( DimensionOrder.T ) : trackingSettings.nt+ trackingSettings.tStart;
        this.trackId  = 9999;
        this.stop = stop;
    }

    @Override
    public OldTrack computeTrack()
    {
        if( TrackingSettings.CENTER_OF_MASS.equalsIgnoreCase( trackingSettings.trackingMethod ) )
                return doCenterOfMassTracking();
        else if(  TrackingSettings.PHASE_CORRELATION.equalsIgnoreCase( trackingSettings.trackingMethod ) )
                return doPhaseCorrelationTracking();
        else
            return null;
    }

    @Override
    public void stopTrack() {
        stop.set(true);
    }

    private OldTrack doCenterOfMassTracking(){
        final Point3D boxDim = pMax.subtract(pMin);
        RandomAccess< R > randomAccess = trackingSettings.rai.randomAccess();
        int tStart = trackingSettings.tStart;
        Point3D pCentroid;
        OldTrack trackingResults = new OldTrack( this.trackingSettings, trackId) ;
        boolean gateIntensity = isIntensityGated(trackingSettings.intensityGate);
        double trackingFactor = trackingSettings.trackingFactor;
        int iterations = trackingSettings.iterationsCenterOfMass;
        long startTime = System.currentTimeMillis();
        for (int t = tStart; t < timeFrames; ++t) {
            progressListener.progress((t-tStart),(timeFrames-1-tStart));
            Logger.info("Current time tracked " + t);
            randomAccess.setPosition(t, DimensionOrder.T );
            double trackingFraction;
            // compute stack center and tracking radii
            // at each iteration, the center of mass is only computed for a subset of the data cube
            // this subset iteratively shifts every iteration according to the results of the center of mass computation
            for(int i=0; i<iterations; i++) {
                // trackingFraction = 1/trackingFactor is the user selected object size, because we are loading
                // a portion of the data, which is trackingFactor times larger than the object size
                // below formula makes the region in which the center of mass is compute go from 1 to 1/trackingfactor
                trackingFraction = 1.0 - Math.pow(1.0*(i+1)/iterations,1.0/4.0)*(1.0-1.0/trackingFactor);
                pCentroid = computeCenterOfMass(
                        randomAccess,
                        pMin.subtract(trackingSettings.maxDisplacement),
                        pMax.add(trackingSettings.maxDisplacement), gateIntensity);
                if (stop.get()) {
                    break;
                }
                pMin = pCentroid.subtract(boxDim.multiply(trackingFraction/2));
                pMax = pCentroid.add(boxDim.multiply(trackingFraction/2));
            }
            if (stop.get()) {
                break;
            }
            trackingResults.addLocation(t, new Point3D[]{pMin,pMax});
        }
        Logger.info("Time elapsed " +(System.currentTimeMillis() - startTime));
        return trackingResults;
    }


    private OldTrack doPhaseCorrelationTracking()
    {
        OldTrack trackingResults = new OldTrack( this.trackingSettings, trackId );
        boolean gateIntensity = isIntensityGated( trackingSettings.intensityGate );
        Point3D pShift;
        int tStart = trackingSettings.tStart;
        long startTime = System.currentTimeMillis();
        trackingResults.addLocation(tStart, new Point3D[]{pMin,pMax});//For the first time stamp.
        for (int t = tStart; t < timeFrames-1; ++t)
        { // last time stamp not considered here.

            progressListener.progress((t-tStart),(timeFrames-2-tStart));

            Logger.info("Current time tracked " + t);

            long[] range = {(long) pMin.getX(),
                    (long) pMin.getY(),
                    (long) pMin.getZ(),
                    channel,
                    tStart,
                    (long) pMax.getX(),
                    (long) pMax.getY(),
                    (long) pMax.getZ(),
                    channel,
                    timeFrames-1};

            FinalInterval trackedInterval = Intervals.createMinMax(range);
            RandomAccessibleInterval< R > rai =
                    Views.interval(
                            Views.extendZero( trackingSettings.rai ) ,
                            trackedInterval );

            RandomAccessibleInterval< R > currentFrame = Views.hyperSlice( rai, 4, t);
            RandomAccessibleInterval< R > nextFrame = Views.hyperSlice( rai, 4, t + 1);
            if (this.stop.get()) {
                break;
            }
            //Intensity gating

            if(gateIntensity) {
                // TODO: apply some preprocessing if needed
                // process( currentFrame, nextFrame );
            }

            final double[] shift = PhaseCorrelationTranslationComputer.computeShift(
                    currentFrame,
                    nextFrame,
                    BigDataProcessor2.trackerThreadPool );

            if ( shift == null ) break;

            pShift = new Point3D(
                    shift[ DimensionOrder.X ],
                    shift[ DimensionOrder.Y ],
                    shift[ DimensionOrder.Z ]);

            pMin = pMin.subtract(pShift);
            pMax = pMax.subtract(pShift);
            if (this.stop.get()) {
                break;
            }
            trackingResults.addLocation(t+1, new Point3D[]{pMin,pMax});
        }
        Logger.info("Time elapsed " +(System.currentTimeMillis() - startTime));
        return trackingResults;
    }

    private void process(
            RandomAccessibleInterval< R > currentFrame,
            RandomAccessibleInterval< R > nextFrame )
    {
        Future future1 = BigDataProcessor2.trackerThreadPool.submit(() -> doIntensityGating( Utils.getCellImgFromInterval(currentFrame)));
        Future future2 = BigDataProcessor2.trackerThreadPool.submit(() -> doIntensityGating(Utils.getCellImgFromInterval(nextFrame)));
        try {
            future1.get();
            future2.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch ( ExecutionException e) {
            e.printStackTrace();
        }
    }

    private <T> Point3D computeCenterOfMass(RandomAccess<T> randomAccess, Point3D pMin, Point3D pMax, boolean gateIntensity) {
        double sum = 0.0, xsum = 0.0, ysum = 0.0, zsum = 0.0;
        int xmin = 0 > (int) pMin.getX() ? 0 : (int) pMin.getX();
        int xmax = (width - 1) < (int) pMax.getX() ? (width - 1) : (int) pMax.getX();
        int ymin = 0 > (int) pMin.getY() ? 0 : (int) pMin.getY();
        int ymax = (height - 1) < (int) pMax.getY() ? (height - 1) : (int) pMax.getY();
        int zmin = 0 > (int) pMin.getZ() ? 0 : (int) pMin.getZ();
        int zmax = (depth - 1) < (int) pMax.getZ() ? (depth - 1) : (int) pMax.getZ();

        // compute one-based, otherwise the numbers at x=0,y=0,z=0 are lost for the center of mass
        List<Future> futures = new ArrayList<>();
        for (int z = zmin; z < zmax; z++) {
            randomAccess.setPosition(z, DimensionOrder.Z );
            if(stop.get()){
                return new Point3D(0, 0, 0);
            }
            randomAccess.setPosition(channel, DimensionOrder.C );
            Future<double[]> future = BigDataProcessor2.trackerThreadPool.submit(new ComputeCenterOfMassPerSliceParallel(randomAccess, z,
                        xmin, xmax, ymin, ymax, gateIntensity));
            futures.add(future);
        }
        for (Future<double[]> future : futures) {
            try {
                double[] result = future.get();
                sum  += result[0];
                xsum += result[1];
                ysum += result[2];
                zsum += result[3];
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        double xCenter = (xsum / sum);
        double yCenter = (ysum / sum);
        double zCenter = (zsum / sum);
        return (new Point3D(xCenter, yCenter, zCenter));
    }

    private class ComputeCenterOfMassPerSliceParallel<T> implements Callable<double[]> {
        final RandomAccess<T> randomAccess;
        final int z;
        final int xmin;
        final int xmax;
        final int ymin;
        final int ymax;
        final boolean gateIntensity;

        ComputeCenterOfMassPerSliceParallel(RandomAccess<T> randomAccess, int z, int xmin, int xmax, int ymin, int ymax, boolean gateIntensity) {
            this.gateIntensity = gateIntensity;
            this.randomAccess = randomAccess.copyRandomAccess();
            this.z = z;
            this.xmax = xmax;
            this.xmin = xmin;
            this.ymax = ymax;
            this.ymin = ymin;
        }

        @Override
        public double[] call() {
            double sum = 0.0, xsum = 0.0, ysum = 0.0, zsum = 0.0, v = 0.0;
            for (int x = xmin; x < xmax; x++) {
                randomAccess.setPosition(x, DimensionOrder.X );
                for (int y = ymin; y < ymax; y++) {
                    randomAccess.setPosition(y, DimensionOrder.Y );
                    if(stop.get()){
                        return new double[]{0, 0, 0, 0};
                    }
                    T value = randomAccess.get();
                    if (value instanceof UnsignedByteType) {
                        v = (Integer.valueOf(((UnsignedByteType) value).get()).doubleValue());
                    } else if (value instanceof UnsignedShortType) {
                        v = (Integer.valueOf(((UnsignedShortType) value).get()).doubleValue());
                    } else if (value instanceof FloatType) {
                        v = (Float.valueOf((((FloatType) value).get())).doubleValue());
                    }
                    if (gateIntensity) {
                        v = doIntensityGating(v);
                    }
                    if (v != 0) {
                        sum += v;
                        xsum += x * v;
                        ysum += y * v;
                        zsum += z * v;
                    }
                }
            }
            return new double[]{sum, xsum, ysum, zsum};
        }
    }

    private boolean isIntensityGated(int[] gate) {
        int min = gate[0];
        int max = gate[1];
        if ((min == -1 && max == -1)) {
            return false;
        } else {
            if (max == -1) {
                gateGateIntensityMax = Short.MAX_VALUE * 2 + 1;
            } else {
                gateGateIntensityMax = max;
            }
            if (min == -1) {
                gateIntensityMin = 0;
            } else {
                gateIntensityMin = min;
            }
            return true;
        }
    }

    private void doIntensityGating(Img currentFrame){
        for (Object value : currentFrame) {
            if (value instanceof UnsignedByteType) {
                double v = Integer.valueOf(((UnsignedByteType) value).get()).doubleValue();
                ((UnsignedByteType) value).set(((int) doIntensityGating(v)));
            } else if (value instanceof UnsignedShortType) {
                double v = Integer.valueOf(((UnsignedShortType) value).get()).doubleValue();
                ((UnsignedShortType) value).set(((int) doIntensityGating(v)));
            } else if (value instanceof FloatType) {
                double v = (((FloatType) value).get());
                ((FloatType) value).set(((float) doIntensityGating(v)));
            }
        }
    }

    private double doIntensityGating(double value) {
        if (value > gateGateIntensityMax || value < gateIntensityMin) {
            value = 0;
        } else {
            value -= gateIntensityMin;
        }
        return value;
    }
}
