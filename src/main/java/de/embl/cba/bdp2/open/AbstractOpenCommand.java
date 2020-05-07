package de.embl.cba.bdp2.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.dialog.HelpDialog;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.service.ImageService;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;

import javax.swing.*;
import java.io.File;

public abstract class AbstractOpenCommand< R extends RealType< R > & NativeType< R > > implements Command {

    public static final String COMMAND_OPEN_PATH = "Commands>Open>";

    public static final String DO_NOT_SHOW = "Do not show image";
    public static final String SHOW_IN_CURRENT_VIEWER = "Show image in current viewer";
    public static final String SHOW_IN_NEW_VIEWER = "Show image in new viewer";

    @Parameter(label = "Image data directory", style = "directory")
    File directory;

    @Parameter(label = "Output image handling", choices = {
            SHOW_IN_CURRENT_VIEWER,
            SHOW_IN_NEW_VIEWER,
            DO_NOT_SHOW })
    protected String viewingModality = SHOW_IN_NEW_VIEWER; // Note: this must be the same variable name as in AbstractProcessingCommand
    public static final String[] VIEWING_CHOICES = new String[]{
            SHOW_IN_CURRENT_VIEWER,
            SHOW_IN_NEW_VIEWER,
            DO_NOT_SHOW };

    @Parameter(label = "Disable arbitrary plane slicing", required = false)
    protected boolean disableArbitraryPlaneSlicing = true;

    protected boolean autoContrast = true;

    protected Image< R > outputImage;

    protected void handleOutputImage( boolean autoContrast, boolean keepViewerTransform )
    {
        ImageService.nameToImage.put( outputImage.getName(), outputImage );

        if ( viewingModality.equals( SHOW_IN_NEW_VIEWER ) )
        {
            BigDataProcessor2.showImage( outputImage, autoContrast, disableArbitraryPlaneSlicing );
        }
//        else if ( outputModality.equals( REPLACE_IN_VIEWER ))
//        {
//            final BdvImageViewer viewer = BdvService.imageNameToBdv.get( inputImage.getName() );
//            viewer.replaceImage( outputImage, autoContrast, keepViewerTransform );
//        }
        else if ( viewingModality.equals( DO_NOT_SHOW ) )
        {
            // do nothing
        }
    }

    protected void showRegExpHelp()
    {
        SwingUtilities.invokeLater( () -> {
            final HelpDialog helpDialog = new HelpDialog( null,
                    AbstractOpenCommand.class.getResource( "/RegExpHelp.html" ) );
            helpDialog.setVisible( true );
        } );
    }
}
