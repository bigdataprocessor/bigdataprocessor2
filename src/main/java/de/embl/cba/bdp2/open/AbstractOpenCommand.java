package de.embl.cba.bdp2.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.process.calibrate.CalibrationChecker;
import de.embl.cba.bdp2.scijava.Services;
import de.embl.cba.bdp2.service.ImageService;
import de.embl.cba.bdp2.viewer.ImageViewer;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

public abstract class AbstractOpenCommand< R extends RealType< R > & NativeType< R > > implements Command, OpenCommand
{
    public static final String COMMAND_OPEN_PATH = "Commands>Open>";
    public static final String DO_NOT_SHOW = "Do not show";
    public static final String SHOW_IN_CURRENT_VIEWER = "Show in current viewer";
    public static final String SHOW_IN_NEW_VIEWER = "Show in new viewer";

    // TODO: this is not concurrency save
    public static ImageViewer parentViewer = null;

    @Parameter
    protected UIService uiService;

    @Parameter
    protected Context context;

    @Parameter(label = "Output image handling", choices = {
            SHOW_IN_CURRENT_VIEWER,
            SHOW_IN_NEW_VIEWER,
            DO_NOT_SHOW })

    protected String viewingModality = SHOW_IN_NEW_VIEWER; // Note: this must be the same variable name as in AbstractProcessingCommand
    public static final String[] VIEWING_CHOICES = new String[]{
            SHOW_IN_CURRENT_VIEWER,
            SHOW_IN_NEW_VIEWER,
            DO_NOT_SHOW };

    @Parameter(label = "Enable arbitrary plane slicing", required = false)
    protected boolean enableArbitraryPlaneSlicing = false;
    public static String ARBITRARY_PLANE_SLICING_PARAMETER = "enableArbitraryPlaneSlicing";

    protected boolean autoContrast = true;

    protected Image< R > outputImage;

    protected void handleOutputImage( boolean autoContrast, boolean keepViewerTransform )
    {
        Services.setUiService( uiService );
        Services.setContext( context );

        ImageService.imageNameToImage.put( outputImage.getName(), outputImage );

        if ( viewingModality.equals( DO_NOT_SHOW ) )
        {
            return;
        }
        else
        {
            CalibrationChecker.correctCalibrationViaDialogIfNecessary( outputImage );
            showInViewer( autoContrast, keepViewerTransform );
        }
    }

    protected ImageViewer showInViewer( boolean autoContrast, boolean keepViewerTransform )
    {
        if ( viewingModality.equals( SHOW_IN_NEW_VIEWER ) || parentViewer == null )
        {
            return BigDataProcessor2.showImage( outputImage, autoContrast, enableArbitraryPlaneSlicing );
        }
        else if ( viewingModality.equals( SHOW_IN_CURRENT_VIEWER ) )
        {
            parentViewer.replaceImage( outputImage, autoContrast, keepViewerTransform );
            return parentViewer;
        }
        else
        {
            return null;
        }
    }
}
