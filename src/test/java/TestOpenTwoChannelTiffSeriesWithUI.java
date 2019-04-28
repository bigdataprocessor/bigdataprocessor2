import bdv.util.*;
import de.embl.cba.bdp2.loading.CachedCellImgReader;
import de.embl.cba.bdp2.loading.files.FileInfos;
import de.embl.cba.bdp2.viewers.BdvTransformEventHandler;
import net.imglib2.cache.img.CachedCellImg;

public class TestOpenTwoChannelTiffSeriesWithUI
{

    public static void main(String[] args)
    {

        final String directory = "src\\test\\resources\\tiff-nc2-nt2\\";
        final FileInfos fileInfos = new FileInfos(directory, FileInfos.LOAD_CHANNELS_FROM_FOLDERS,".*","");
        CachedCellImg myImg = new CachedCellImgReader().asCachedCellImg( fileInfos );
        double [] voxelSpacing = new double[]{0,0};
        final BdvStackSource bdvStackSource = BdvFunctions.show(myImg,"stream", BdvOptions.options().axisOrder(AxisOrder.XYCZT)
                .transformEventHandlerFactory(new BdvTransformEventHandler.BehaviourTransformEventHandler3DFactory( voxelSpacing )));

    }

}
