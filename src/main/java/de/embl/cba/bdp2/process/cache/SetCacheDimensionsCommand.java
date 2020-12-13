package de.embl.cba.bdp2.process.cache;

import de.embl.cba.bdp2.BigDataProcessor2Menu;
import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.AbstractOpenFileSeriesCommand;
import de.embl.cba.bdp2.process.AbstractImageProcessingCommand;
import de.embl.cba.bdp2.scijava.Services;
import de.embl.cba.bdp2.service.ImageService;
import de.embl.cba.bdp2.viewer.ImageViewer;
import ij.VirtualStack;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Arrays;

import static de.embl.cba.bdp2.process.cache.SetCacheDimensionsCommand.COMMAND_NAME;

@Plugin(type = AbstractImageProcessingCommand.class, name = COMMAND_NAME,  menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractImageProcessingCommand.COMMAND_PATH + SetCacheDimensionsCommand.COMMAND_FULL_NAME )
public class SetCacheDimensionsCommand< R extends RealType< R > & NativeType< R > > extends AbstractImageProcessingCommand< R >
{
    public static final String COMMAND_NAME = "Set Cache Dimensions...";
    public static final String COMMAND_FULL_NAME = BigDataProcessor2Menu.COMMAND_BDP2_PREFIX + COMMAND_NAME;

    @Parameter(label = "Cache dimensions x,y,z,c,t")
    String chacheDimensions = "100,100,1,1,1"; // TODO: prefill with current
    public static String CACHE_DIMENSIONS = "chacheDimensions";

    @Override
    public void run()
    {
        process();
        handleOutputImage( false, true );
        ImageService.imageNameToImage.put( outputImage.getName(), outputImage );
    }

    private void process()
    {
        final int[] cacheDims = Arrays.stream( chacheDimensions.split( "," ) ).mapToInt( i -> Integer.parseInt( i ) ).toArray();

        outputImage = new Image<>( inputImage );
        outputImage.setCache( cacheDims, DiskCachedCellImgOptions.CacheType.BOUNDED, 100  );
        outputImage.setName( outputImageName );
    }

    @Override
    public void showDialog( ImageViewer< R > imageViewer )
    {
        new SetCacheDimensionsDialog<>( imageViewer ).showDialog();
    }
}
