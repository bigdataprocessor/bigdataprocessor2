package playground;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.scijava.Services;
import de.embl.cba.bdp2.track.manual.ManualTrackCreator;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imagej.ImageJ;

import static de.embl.cba.bdp2.open.core.NamingScheme.MULTI_CHANNEL_VOLUMES;
import static de.embl.cba.bdp2.open.core.NamingScheme.TIF;

public class DevelopManualDriftCorrection
{
    public static void main(String[] args)
    {
        final ImageJ imageJ = new ImageJ();
        Services.commandService = imageJ.command();

        final String directory = "/Users/tischer/Documents/bigdataprocessor2/src/test/resources/test/tiff-nc2-nt6";

        final Image image = BigDataProcessor2.openImage(
                directory,
                MULTI_CHANNEL_VOLUMES + TIF,
                ".*"
        );

        final BdvImageViewer viewer = BigDataProcessor2.showImage( image, true );

        final ManualTrackCreator manualTrackCreator = new ManualTrackCreator( viewer, "track" );
    }
}
