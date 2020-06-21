package develop;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.drift.track.TrackCreator;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.scijava.Services;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imagej.ImageJ;

import static de.embl.cba.bdp2.open.core.NamingScheme.MULTI_CHANNEL_VOLUMES;
import static de.embl.cba.bdp2.open.core.NamingScheme.TIF;

public class DevelopManualDriftCorrectionMicroglia
{
    public static void main(String[] args)
    {
        final ImageJ imageJ = new ImageJ();
        imageJ.ui().showUI();
        Services.commandService = imageJ.command();

        final String directory = "/Users/tischer/Documents/bigdataprocessor2/src/test/resources/test-data/microglia-tracking-nt123/volumes";

        final Image image = BigDataProcessor2.openImage(
                directory,
                MULTI_CHANNEL_VOLUMES + TIF,
                ".*"
        );

        final BdvImageViewer viewer = BigDataProcessor2.showImage( image, true );

        final TrackCreator trackCreator = new TrackCreator( viewer, "drift" );
    }
}
