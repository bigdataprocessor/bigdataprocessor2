package de.embl.cba.bdp2.process.align.splitchip;

import de.embl.cba.bdp2.process.AbstractImageProcessingCommand;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.ImageViewer;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.util.List;

@Plugin(type = Command.class, menuPath = de.embl.cba.bdp2.dialog.Utils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractImageProcessingCommand.COMMAND_PROCESS_PATH + SplitChipCommand.COMMAND_FULL_NAME )
public class SplitChipCommand< R extends RealType< R > & NativeType< R > > extends AbstractImageProcessingCommand< R >
{
    public static final String COMMAND_NAME = "Align Channels Split Chip...";
    public static final String COMMAND_FULL_NAME = Utils.COMMAND_BDP2_PREFIX + COMMAND_NAME;

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
