package de.embl.cba.bdp2.devel;

import de.embl.cba.bdp2.utils.Point3D;
import net.imglib2.interpolation.InterpolatorFactory;

public class ShearingSettings {

    public boolean useObliqueAngle=false;
    public double shearingFactorX = 0.0, shearingFactorY = 0.0;
    public double cameraPixelsize = 6.5;
    public boolean viewLeft=false,backwardStackAcquisition=false;
    public double magnification = 40;
    public double stepSize= 1;
    public double objectiveAngle=45;
    public boolean useYshear=false;
    public int useObliqueButtonvalue=1;
    public int currentZ=1;
    public int nZ=1;
    public Point3D offset= new Point3D(0,0,0);
    public InterpolatorFactory interpolationFactory;

}
