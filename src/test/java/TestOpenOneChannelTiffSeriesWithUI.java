import bdv.BehaviourTransformEventHandler3D;
import bdv.util.AxisOrder;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvSource;
import de.embl.cba.bigDataTools2.CachedCellImageCreator;
import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoSource;
import net.imglib2.img.Img;

/**
 * IMPORTANT NOTE: Adjust Max value to 255 in the Big Data Viewer. (Settings>Brightness and Color>Max)
 */

public class TestOpenOneChannelTiffSeriesWithUI
{
    //TODO : write as jUNIT
    public static void main(String[] args)
    {


        final String directory = "src\\test\\resources\\tiff-nc1-nt2-16bit\\";
        final FileInfoSource fileInfoSource = new FileInfoSource(directory,"None",".*","",true);
        System.out.println("BitDepth is "+fileInfoSource.bitDepth);
        Img myImg = new CachedCellImageCreator().create(fileInfoSource,null);
        BdvSource bdvSource = BdvFunctions.show(myImg,"stream", BdvOptions.options().axisOrder( AxisOrder.XYCZT)
                        .doubleBuffered( false )
                        .transformEventHandlerFactory( new BehaviourTransformEventHandler3D.BehaviourTransformEventHandler3DFactory()) );
        if(fileInfoSource.isAutoContrast()) {
            bdvSource.setDisplayRange(fileInfoSource.min_pixel_val, fileInfoSource.max_pixel_val);
        }



    }


}
