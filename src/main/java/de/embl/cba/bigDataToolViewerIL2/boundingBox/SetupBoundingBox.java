package de.embl.cba.bigDataToolViewerIL2.boundingBox;

import net.imglib2.Dimensions;
import net.imglib2.FinalRealInterval;
import net.imglib2.img.Img;
import net.imglib2.realtransform.AffineTransform3D;

// TODO: check if this class is needed at all. Estimate might be taken from FileInfoSource nX,nY,nZ! -- ashis

public class SetupBoundingBox {

    public static BoundingBox estimateBoundingBox(final Img myImg) {
        final double[] minBB = new double[3];
        final double[] maxBB = new double[3];

        for (int d = 0; d < minBB.length; ++d) {
            minBB[d] = Double.MAX_VALUE;
            maxBB[d] = -Double.MAX_VALUE;
        }

        computeMaxBoundingBoxDimensions(myImg, minBB, maxBB);
        final BoundingBox maxsized = new BoundingBox(approximateLowerBound(minBB), approximateUpperBound(maxBB));
        return maxsized;
    }

    public static int[] approximateLowerBound(final double[] min) {
        final int[] lowerBound = new int[min.length];
        for (int d = 0; d < min.length; ++d)
            lowerBound[d] = (int) Math.round(Math.floor(min[d]));
        return lowerBound;
    }

    public static int[] approximateUpperBound(final double[] max) {
        final int[] upperBound = new int[max.length];
        for (int d = 0; d < max.length; ++d) {
            upperBound[d] = (int) Math.round(Math.ceil(max[d]));
        }
        return upperBound;
    }


    public static void computeMaxBoundingBoxDimensions(Dimensions size, final double[] minBB, final double[] maxBB) {
        final double[] min = new double[]{0, 0, 0};
        final double[] max = new double[]{
                size.dimension(0) - 1,
                size.dimension(1) - 1,
                size.dimension(3) - 1};
        final FinalRealInterval interval = new AffineTransform3D().estimateBounds(new FinalRealInterval(min, max));
        for (int d = 0; d < minBB.length; ++d) {
            minBB[d] = Math.min(minBB[d], interval.realMin(d));
            maxBB[d] = Math.max(maxBB[d], interval.realMax(d));
        }
    }

}
