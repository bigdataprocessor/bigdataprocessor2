package de.embl.cba.bdp2.open.ui;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.dialog.Utils;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.swing.*;

import java.io.File;

import static de.embl.cba.bdp2.utils.Utils.COMMAND_BDP2_PREFIX;

@Plugin(type = Command.class, menuPath = Utils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractOpenCommand.COMMAND_OPEN_PATH + OpenWithBioformatsBdvBridgeCommand.COMMAND_FULL_NAME )
public class OpenWithBioformatsBdvBridgeCommand< R extends RealType< R > & NativeType< R >> extends AbstractOpenCommand< R > {

    public static final String COMMAND_NAME = "Open With Bioformats...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP2_PREFIX + COMMAND_NAME;

    @Parameter
    File file;

    @Parameter( label = "Series index"  )
    protected int seriesIndex = 0;

    @Override
    public void run() {
        SwingUtilities.invokeLater( () ->  {
            outputImage = BigDataProcessor2.openImageWithBioformats( directory.toString(), file.getAbsolutePath(), seriesIndex );

            handleOutputImage( true, false );
        });
    }
}
