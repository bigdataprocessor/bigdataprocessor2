package de.embl.cba.bdp2.open.command;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.dialog.HelpDialog;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.service.ImageService;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;

import javax.swing.*;
import java.io.File;

import static de.embl.cba.bdp2.scijava.command.AbstractProcessingCommand.DO_NOT_SHOW;
import static de.embl.cba.bdp2.scijava.command.AbstractProcessingCommand.SHOW_IN_NEW_VIEWER;

public abstract class AbstractOpenCommand< R extends RealType< R > & NativeType< R > > implements Command {

    @Parameter(label = "Image data directory", style = "directory")
    File directory;
    public static final String INPUT_DIRECTORY_PARAMETER = "directory";

    @Parameter(label = "Output image handling", choices = {
            // REPLACE_IN_VIEWER,
            SHOW_IN_NEW_VIEWER,
            DO_NOT_SHOW })
    protected String viewingModality = SHOW_IN_NEW_VIEWER; // Note: this must be the same variable name as in AbstractProcessingCommand

    @Parameter(label = "Disable arbitrary plane slicing")
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
