package develop;

import bdv.util.AxisOrder;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import de.embl.cba.bdp2.open.core.CachedCellImgReader;
import de.embl.cba.bdp2.open.core.FileInfos;
import de.embl.cba.bdp2.open.core.NamingSchemes;
import de.embl.cba.bdp2.viewers.BdvTransformEventHandler;
import net.imglib2.img.Img;

/**
 * IMPORTANT NOTE: Adjust Max value to 255 in the Big Data Viewer. (Settings>Brightness and Color>Max)
 */
public class TestOpenLeicaDLS
{
    public static void main(String[] args)
    {
        final String directory = "src\\test\\resources\\leicaDLS\\";
        FileInfos fileInfos = new FileInfos(directory, NamingSchemes.LEICA_LIGHT_SHEET_TIFF,".*","");
        System.out.println( fileInfos.nT);
        System.out.println( fileInfos.nC);
        System.out.println( fileInfos.nZ);
        System.out.println("BitDepth "+ fileInfos.bitDepth);
        Img myImg = new CachedCellImgReader().createCachedCellImg( fileInfos );

//        AffineTransform3D affineTransform3D = new AffineTransform3D(); // TODO: Play with it later
//        affineTransform3D.set( 1.0, 0, 0);
//        affineTransform3D.set( 1.0, 1, 1);
//        affineTransform3D.set( 1.0, 2, 2);
        double [] voxelSpacing = new double[]{0,0};

        BdvFunctions.show(myImg,"LeicaDLS", BdvOptions.options().axisOrder( AxisOrder.XYCZT)
                .doubleBuffered( false )
                .numRenderingThreads(10)
                //.sourceTransform( affineTransform3D )
                .transformEventHandlerFactory( new BdvTransformEventHandler.BehaviourTransformEventHandler3DFactory( voxelSpacing )));// TODO: Play with it later


    }
}
