package de.embl.cba.bdp2.process.align.splitchip;

import de.embl.cba.bdp2.BigDataProcessor2Menu;
import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.process.AbstractImageProcessingCommand;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewer.ImageViewer;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.List;

@Plugin(type = AbstractImageProcessingCommand.class, name = SplitChipCommand.COMMAND_NAME, menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractImageProcessingCommand.COMMAND_PROCESS_PATH + SplitChipCommand.COMMAND_FULL_NAME )
public class SplitChipCommand< R extends RealType< R > & NativeType< R > > extends AbstractImageProcessingCommand< R >
{
    public static final String COMMAND_NAME = "Align Channels Split Chip...";
    public static final String COMMAND_FULL_NAME = BigDataProcessor2Menu.COMMAND_BDP2_PREFIX + COMMAND_NAME;

    @Parameter(label = "Regions [ minX, minY, sizeX, sizeY, channel; minX, ... ]")
    public String intervalsString = "896, 46, 1000, 1000, 0; 22, 643, 1000, 1000, 0";

    public void run()
    {
        process();
        handleOutputImage( true, false );
    }

    public void process()
    {
        final SplitChipMerger merger = new SplitChipMerger();
        final List< long[] > regionsXminYminXdimYdimC = Utils.delimitedStringToLongs( intervalsString, ";" );
        outputImage = merger.mergeRegionsXYC( inputImage, regionsXminYminXdimYdimC );
    }

    @Override
    public void showDialog( ImageViewer< R > imageViewer )
    {
        new SplitChipDialog<>( imageViewer ).showDialog();
    }
}
