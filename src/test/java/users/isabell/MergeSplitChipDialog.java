package users.isabell;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.core.NamingScheme;
import de.embl.cba.bdp2.align.splitchip.SplitViewMergeDialog;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imagej.ImageJ;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class MergeSplitChipDialog
{
    public static < R extends RealType< R > & NativeType< R > >
    void main( String[] args )
    {
        final ImageJ imageJ = new ImageJ();
        imageJ.ui().showUI();

        final Image< R > image = BigDataProcessor2.openImageFromHdf5(
                "/Users/tischer/Desktop/stack_0_channel_0",
                NamingScheme.SINGLE_CHANNEL_TIMELAPSE,
                ".*.h5",
                "Data" );

        image.setVoxelUnit( "micrometer" );
        image.setVoxelSpacing( new double[]{0.13, 0.13, 1.04} );

        final BdvImageViewer viewer = BigDataProcessor2.showImage( image);
        new SplitViewMergeDialog( viewer );
    }

}
