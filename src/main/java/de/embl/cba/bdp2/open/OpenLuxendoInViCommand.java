package de.embl.cba.bdp2.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.swing.*;

import static de.embl.cba.bdp2.read.NamingScheme.SINGLE_CHANNEL_TIFF_VOLUMES;
import static de.embl.cba.bdp2.utils.Utils.COMMAND_BDP_PREFIX;

@Plugin(type = Command.class, menuPath = "Plugins>BigDataProcessor2>" + AbstractOpenCommand.COMMAND_OPEN_PATH + OpenLuxendoInViCommand.COMMAND_FULL_NAME )
public class OpenLuxendoInViCommand< R extends RealType< R > & NativeType< R > > extends AbstractOpenCommand< R >
{
    public static final String COMMAND_NAME = "Open Luxendo InVi...";
    public static final String COMMAND_FULL_NAME = COMMAND_BDP_PREFIX + COMMAND_NAME;
    public static final String LONG = "Long";
    public static final String SHORT = "Short";
    public static final String BOTH = "Both";

    @Parameter(label = "Stack index"  )
    protected int stackIndex = 0;

    @Parameter(label = "Camera", choices = {
            LONG,
            SHORT,
            BOTH
    })
    protected String camera = BOTH;

    public void run()
    {
        SwingUtilities.invokeLater( () ->  {

            String regExp = SINGLE_CHANNEL_TIFF_VOLUMES.replace( "STACK", "" + stackIndex );

            if ( camera.equals( LONG ) )
                regExp = regExp.replace( "?<C2>.*", "?<C2>Long" );
            if ( camera.equals( SHORT ) )
                regExp = regExp.replace( "?<C2>.*", "?<C2>Short" );


            outputImage =
                        BigDataProcessor2.openImageFromHdf5(
                                directory.toString(),
                                regExp,
                                regExp,
                                "Data" );

            handleOutputImage( true, false );

        });
    }
}
