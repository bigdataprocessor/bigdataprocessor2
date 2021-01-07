package de.embl.cba.bdp2.process.cache;

import de.embl.cba.bdp2.BigDataProcessor2Menu;
import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.process.AbstractImageProcessingCommand;
import de.embl.cba.bdp2.service.ImageService;
import de.embl.cba.bdp2.viewer.ImageViewer;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.Arrays;

import static de.embl.cba.bdp2.process.cache.ConfigureLazyLoadingCommand.COMMAND_NAME;

@Plugin(type = AbstractImageProcessingCommand.class, name = COMMAND_NAME,  menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractImageProcessingCommand.COMMAND_PATH + ConfigureLazyLoadingCommand.COMMAND_FULL_NAME )
public class ConfigureLazyLoadingCommand< R extends RealType< R > & NativeType< R > > extends AbstractImageProcessingCommand< R >
{
    public static final String COMMAND_NAME = "Configure Lazy Loading...";
    public static final String COMMAND_FULL_NAME = BigDataProcessor2Menu.COMMAND_BDP2_PREFIX + COMMAND_NAME;

    @Parameter(label = "Cache dimensions x,y,z,c,t")
    String chacheDimensions = "100,100,1,1,1"; // TODO: pre-fill with current
    public static String CACHE_DIMENSIONS = "chacheDimensions";

    // TODO: Make a choice
    private DiskCachedCellImgOptions.CacheType cacheType = DiskCachedCellImgOptions.CacheType.SOFTREF;

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
        outputImage.setCache( cacheDims, DiskCachedCellImgOptions.CacheType.SOFTREF, 0  );
        outputImage.setName( outputImageName );

        Logger.info( "\n# " + ConfigureLazyLoadingCommand.COMMAND_NAME );
        Logger.info( "Image: " + inputImage.getName() );
        Logger.info( "Cell dimensions: " + Arrays.toString( cacheDims ) );
        Logger.info( "Type: " + cacheType.toString() );
    }

    @Override
    public void showDialog( ImageViewer< R > imageViewer )
    {
        new ConfigureLazyLoadingDialog<>( imageViewer ).showDialog();
    }
}
