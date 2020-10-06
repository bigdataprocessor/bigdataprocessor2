package develop;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.scijava.Services;
import de.embl.cba.bdp2.process.track.TrackCreator;
import de.embl.cba.bdp2.viewers.ImageViewer;
import net.imagej.ImageJ;

import static de.embl.cba.bdp2.open.core.NamingSchemes.MULTI_CHANNEL_VOLUMES;
import static de.embl.cba.bdp2.open.core.NamingSchemes.TIF;

public class DevelopManualDriftCorrection
{
    public static void main(String[] args)
    {
        final ImageJ imageJ = new ImageJ();
        imageJ.ui().showUI();
        Services.commandService = imageJ.command();

        final String directory = "/Users/tischer/Documents/bigdataprocessor2/src/test/resources/test/tiff-nc2-nt6";

        final Image image = BigDataProcessor2.openImage(
                directory,
                MULTI_CHANNEL_VOLUMES + TIF,
                ".*"
        );

        final ImageViewer viewer = BigDataProcessor2.showImage( image, true );

        final TrackCreator trackCreator = new TrackCreator( viewer, "drift" );
    }
}
