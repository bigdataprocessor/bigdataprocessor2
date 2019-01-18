package de.embl.cba.bigDataTools2.bigDataTrackerGUI;

import de.embl.cba.bigDataTools2.dataStreamingGUI.BigDataConverter;
import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoConstants;
import de.embl.cba.bigDataTools2.utils.Utils;
import de.embl.cba.bigDataTools2.viewers.BdvImageViewer;
import de.embl.cba.bigDataTools2.viewers.ImageViewer;
import javafx.geometry.Point3D;
import net.imagej.ImageJ;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessibleInterval;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.command.DynamicCommand;
import org.scijava.command.Interactive;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.Button;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Plugin(type = Command.class)
@Deprecated
public class BigDataTrackerUICommand extends DynamicCommand implements Interactive {

    @Parameter
    private LogService logService;

    @Parameter(visibility = ItemVisibility.MESSAGE)
    private String textTracking = "TRACKING";

    @Parameter(label = "Select ROI", callback = "selectROI")
    private Button selectROI;

    @Parameter(label = "Length [frames]")
    private Integer length = -1; //default value

    @Parameter(label = "Intensity Gating [min,max]")
    private String gate = "-1,-1"; //default value

    @Parameter(label = "Tracking Method", choices = {FileInfoConstants.CENTER_OF_MASS, FileInfoConstants.CROSS_CORRELATION})
    String trackMethod = FileInfoConstants.CENTER_OF_MASS; //default value

    @Parameter(label = "Track Selected Object", callback = "doTracking")
    private Button track;

    @Parameter(label = "Stop", callback = "interruptTracking")
    private Button stop;

    @Parameter(visibility = ItemVisibility.MESSAGE)
    private String textStream = "VIEW AS NEW STREAM";

    @Parameter(label = "Resize Regions by Factor")
    private Double resizeFactor = 1.35;//default value

    @Parameter(label = "Stream", callback = "showTrackedObject")
    private Button textAsStream;

    @Parameter(visibility = ItemVisibility.MESSAGE, label = "MISCELLANEOUS")
    private String textMISC = "";

    @Parameter(label = "Report Issue", callback = "reportIssue")
    private Button reportIssue;


    Point3D maxDisplacement = new Point3D(20, 20, 1);
    final BigDataTracker bigDataTracker = new BigDataTracker();
    TrackingSettings trackingSettings = new TrackingSettings();
    private RandomAccessibleInterval image;

    @Parameter(visibility = ItemVisibility.INVISIBLE)
    public ImageViewer imageViewer = null;


    @Override
    public void run() {
        System.out.println(imageViewer.getImageName());
        this.image = imageViewer.getRai();
        String[] imageFilters = new String[Utils.ImageFilterTypes.values().length];
        for (int i = 0; i < imageFilters.length; i++) {
            imageFilters[i] = Utils.ImageFilterTypes.values()[i].toString();
        }
        trackingSettings.objectSize = new Point3D(200, 200, 30);
        trackingSettings.maxDisplacement = maxDisplacement;//new Point3D( 15, 15, 1);
        trackingSettings.subSamplingXYZ = new Point3D(3, 3, 1);
        trackingSettings.subSamplingT = 1;
        trackingSettings.intensityGate = new int[]{-1, -1};
        trackingSettings.viewFirstNProcessedRegions = 0;
        trackingSettings.imageFeatureEnhancement = Utils.ImageFilterTypes.NONE.toString();
    }

    private void selectROI() {
        System.out.println("select");
        BigDataConverter.trackerThreadPool.submit(() -> {
            FinalInterval interval = imageViewer.get5DIntervalFromUser();
            trackingSettings.pMin = new Point3D((int) interval.min(FileInfoConstants.X_AXIS_POSITION),
                    (int) interval.min(FileInfoConstants.Y_AXIS_POSITION),
                    (int) interval.min(FileInfoConstants.Z_AXIS_POSITION));

            trackingSettings.pMax = new Point3D((int) interval.max(FileInfoConstants.X_AXIS_POSITION),
                    (int) interval.max(FileInfoConstants.Y_AXIS_POSITION),
                    (int) interval.max(FileInfoConstants.Z_AXIS_POSITION));
        });
        trackingSettings.tStart = imageViewer.getCurrentTimePoint();
    }

    private void doTracking() {
        // configure tracking
        trackingSettings.imageRAI = image;
        if (length < -1) {
            length = -1;
        }
        trackingSettings.nt = length;
        trackingSettings.trackingMethod = trackMethod;
        trackingSettings.intensityGate = Utils.delimitedStringToIntegerArray(gate, ",");
        // TODO: think about below:
        trackingSettings.trackingFactor = 1.0 + 2.0 * maxDisplacement.getX() / trackingSettings.objectSize.getX();
        trackingSettings.iterationsCenterOfMass =
                (int) Math.ceil(Math.pow(trackingSettings.trackingFactor, 2));
        BigDataConverter.trackerThreadPool.submit(() -> {
            bigDataTracker.trackObject(trackingSettings, imageViewer);
        });
    }

    private void interruptTracking() {
        // Cancel Tracking
        bigDataTracker.cancelTracking();
    }

    private void showTrackedObject() {
        bigDataTracker.showTrackedObjects(imageViewer);
    }

    private void reportIssue() {
        if (Desktop.isDesktopSupported()) {
            try {
                final URI uri = new URI("https://github.com/tischi/imagej-open-stacks-as-virtualstack/issues"); //TODO : change URL --ashis
                Desktop.getDesktop().browse(uri);
            } catch (URISyntaxException uriEx) {
                logService.error(uriEx.toString());
            } catch (IOException ioEx) {
                logService.error(ioEx.toString());
            }
        } else { /* TODO: error handling */ }
    }

    public static void main(final String... args) throws Exception {
        // Test using Dummy ImageViewer
        final ImageJ ij = new ImageJ();
        ij.launch(args);
        double [] voxelSize = new double[]{0,0};
        ImageViewer img = new BdvImageViewer(null, "dummy", voxelSize );
        ij.command().run(BigDataTrackerUICommand.class, true, "imageViewer", img);
    }
}
