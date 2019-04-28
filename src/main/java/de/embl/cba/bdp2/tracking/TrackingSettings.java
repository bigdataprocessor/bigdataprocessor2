package de.embl.cba.bdp2.tracking;

import javafx.geometry.Point3D;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.RealType;

public class TrackingSettings< T extends RealType< T > >
{

    public static final String CENTER_OF_MASS = "Center of Mass";
    public static final String CORRELATION = "Correlation";
    public RandomAccessibleInterval< T > rai;
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

}
