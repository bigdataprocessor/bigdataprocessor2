package develop;

import bdv.util.AxisOrder;
import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import de.embl.cba.bdp2.open.core.CachedCellImgCreator;
import de.embl.cba.bdp2.open.core.FileInfos;
import de.embl.cba.bdp2.open.core.NamingSchemes;
import net.imglib2.img.Img;

public class TestOpenEmTiffSlices {
    public static void main(String[] args) {
        final String directory = "src\\test\\resources\\em-tiff-slices\\";
        FileInfos fileInfos = new FileInfos(directory, NamingSchemes.TIFF_SLICES, ".*.tif", "");
        System.out.println( fileInfos.nZ);
        System.out.println( fileInfos.nT);
        System.out.println( fileInfos.nC);
        System.out.println( fileInfos.nX);
        Img myImg = new CachedCellImgCreator().createCachedCellImg( fileInfos );
        BdvFunctions.show(myImg,"stream", BdvOptions.options().axisOrder( AxisOrder.XYCZT));

    }
}
