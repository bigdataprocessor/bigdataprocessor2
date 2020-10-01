package de.embl.cba.bdp2.open.ui;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.dialog.Utils;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.LuxendoInteractiveChannelSubsetter;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.swing.*;

import java.io.File;

import static de.embl.cba.bdp2.open.core.NamingSchemes.LUXENDO_REGEXP;
import static de.embl.cba.bdp2.utils.Utils.COMMAND_BDP2_PREFIX;

@Plugin(type = Command.class, menuPath = Utils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractOpenCommand.COMMAND_OPEN_PATH + OpenLuxendoCommand.COMMAND_FULL_NAME )
public class OpenLuxendoCommand< R extends RealType< R > & NativeType< R > > extends AbstractOpenCommand< R >
{
    public static final String COMMAND_NAME = "Open Luxendo Hdf5...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP2_PREFIX + COMMAND_NAME;

    @Parameter( label = "Stack index"  )
    protected int stackIndex = 0;
    public static String STACK_INDEX_PARAMETER = "stackIndex";

    public void run()
    {
        SwingUtilities.invokeLater( () ->  {
            String regExp = LUXENDO_REGEXP.replace( "STACK", "" + stackIndex );

            if ( directory.getName().contains( "stack_" ) )
            {
                // In case the user mistakenly clicked one level too deep
                directory = new File( directory.getParent() );
            }

            final LuxendoInteractiveChannelSubsetter channelSubsetter =
                    new LuxendoInteractiveChannelSubsetter(
                            directory,
                            viewingModality,
                            enableArbitraryPlaneSlicing,
                            stackIndex );

            outputImage = BigDataProcessor2.openImageFromHdf5( directory.toString(), regExp, regExp, "Data", channelSubsetter );

            handleOutputImage( true, false );
        });
    }
}
