package playground;

import de.embl.cba.bdp2.track.TrackingSettings;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import de.embl.cba.bdp2.utils.Point3D;

public class TestBigDataTracker {

    public static void main(String[] args) {

//        String imageDirectory = "src/test/resources/tiff-nc2-nt6-track/";
//        final FileInfos fileInfos = new FileInfos(imageDirectory, NamingScheme.LOAD_CHANNELS_FROM_FOLDERS,
//                ".*", "");
//        CachedCellImg cachedCellImg = CachedCellImgReader.createCachedCellImg( fileInfos );
//
//        final Image< UnsignedShortType > rImage = (Image< UnsignedShortType >)
//                new Image(
//                        cachedCellImg,
//                        "name",
//                        new String[]{ "channel1", "channel2" },
//                        new double[]{ 1.0, 1.0, 1.0 },
//                "pixel",
//                        fileInfos );
//
//        BdvImageViewer imageViewer = new BdvImageViewer< UnsignedShortType >(
//                cachedCellImg,
//                "input",
//                new double[]{1.0, 1.0, 1.0},
//                "pixel");
//        imageViewer.show( true );
//
//        BigDataTracker bdt = new BigDataTracker();
//        TrackingSettings< ? > trackingSettings = createTrackingSettings(imageViewer);
//        //Test for CROSS_CORRELATION track
//        trackingSettings.trackingMethod = TrackingSettings.PHASE_CORRELATION;
//        bdt.trackObject(trackingSettings, imageViewer);
        //Test for CENTER of MASS track
//        trackingSettings.trackingMethod = TrackingSettings.CENTER_OF_MASS;
//        bdt.trackObject(trackingSettings, imageViewer);
    }

    private static TrackingSettings< ? > createTrackingSettings(BdvImageViewer imageViewer) {
        Point3D maxDisplacement = new Point3D(20, 20, 1);
        TrackingSettings< ? > trackingSettings = new TrackingSettings<>();
        trackingSettings.rai = imageViewer.getImage().getRai();
        trackingSettings.maxDisplacement = maxDisplacement;
        trackingSettings.objectSize = new Point3D(200, 200, 10);
        trackingSettings.trackingFactor = 1.0 + 2.0 * maxDisplacement.getX() /
                trackingSettings.objectSize.getX();
        trackingSettings.iterationsCenterOfMass = (int) Math.ceil(Math.pow(trackingSettings.trackingFactor, 2));
        trackingSettings.pMin = new Point3D(5, 7, 15);
        trackingSettings.pMax = new Point3D(30, 30, 36);
        trackingSettings.tStart = 0;
        trackingSettings.intensityGate = new int[]{75, -1};
        trackingSettings.imageFeatureEnhancement = Utils.ImageFilterTypes.NONE.toString();
        trackingSettings.nt = -1;
        trackingSettings.channel=0;
        return trackingSettings;
    }
}
