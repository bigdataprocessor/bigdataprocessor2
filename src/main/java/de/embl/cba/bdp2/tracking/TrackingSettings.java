package de.embl.cba.bdp2.tracking;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.utils.Point3D;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class TrackingSettings < R extends RealType< R > & NativeType< R > >
{

    public static final String CENTER_OF_MASS = "Center of Mass";
    public static final String PHASE_CORRELATION = "Phase Correlation";

    public RandomAccessibleInterval< R > rai;
    public String trackingMethod;

    public Point3D objectSize;
    public Point3D maxDisplacement;
    public Point3D pMin;
    public Point3D pMax;
    public Point3D subSamplingXYZ;
    public int subSamplingT;
    public int iterationsCenterOfMass;
    public int tStart;
    public int nt;
    public int channel;
    public double trackingFactor;
    public int[] intensityGate =new int[2];
    public int viewFirstNProcessedRegions;
    public String imageFeatureEnhancement;
    public double[] voxelSpacing;
    public Integer trackId;

}
