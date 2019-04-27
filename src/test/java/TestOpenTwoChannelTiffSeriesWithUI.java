import bdv.util.*;
import de.embl.cba.bdp2.loading.CachedCellImageCreator;
import de.embl.cba.bdp2.files.FileInfoConstants;
import de.embl.cba.bdp2.files.FileInfos;
import de.embl.cba.bdp2.viewers.BdvTransformEventHandler;
import net.imglib2.cache.img.CachedCellImg;

public class TestOpenTwoChannelTiffSeriesWithUI
{

    public static void main(String[] args)
    {

        final String directory = "src\\test\\resources\\tiff-nc2-nt2\\";
        final FileInfos fileInfos = new FileInfos(directory, FileInfoConstants.LOAD_CHANNELS_FROM_FOLDERS,".*","");
        CachedCellImg myImg = new CachedCellImageCreator().create( fileInfos );
        double [] voxelSize = new double[]{0,0};
        final BdvStackSource bdvStackSource = BdvFunctions.show(myImg,"stream", BdvOptions.options().axisOrder(AxisOrder.XYCZT)
                .transformEventHandlerFactory(new BdvTransformEventHandler.BehaviourTransformEventHandler3DFactory( voxelSize )));

    }

}
