import bdv.util.AxisOrder;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import de.embl.cba.bigDataTools2.CachedCellImageCreator;
import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoConstants;
import de.embl.cba.bigDataTools2.fileInfoSource.FileInfoSource;
import de.embl.cba.bigDataTools2.viewers.BdvTransformEventHandler;
import net.imglib2.img.Img;

/**
 * IMPORTANT NOTE: Adjust Max value to 255 in the Big Data Viewer. (Settings>Brightness and Color>Max)
 */
public class TestOpenLeicaDLS
{
    public static void main(String[] args)
    {
        final String directory = "src\\test\\resources\\leicaDLS\\";
        FileInfoSource fileInfoSource = new FileInfoSource(directory, FileInfoConstants.LEICA_SINGLE_TIFF,".*","");
        System.out.println(fileInfoSource.nT);
        System.out.println(fileInfoSource.nC);
        System.out.println(fileInfoSource.nZ);
        System.out.println("BitDepth "+fileInfoSource.bitDepth);
        Img myImg = new CachedCellImageCreator().create(fileInfoSource,null);

//        AffineTransform3D affineTransform3D = new AffineTransform3D(); // TODO: Play with it later
//        affineTransform3D.set( 1.0, 0, 0);
//        affineTransform3D.set( 1.0, 1, 1);
//        affineTransform3D.set( 1.0, 2, 2);


        BdvFunctions.show(myImg,"LeicaDLS", BdvOptions.options().axisOrder( AxisOrder.XYCZT)
                .doubleBuffered( false )
                .numRenderingThreads(10)
                //.sourceTransform( affineTransform3D )
                .transformEventHandlerFactory( new BdvTransformEventHandler.BehaviourTransformEventHandler3DFactory( voxelSize )));// TODO: Play with it later


    }
}
