package de.embl.cba.bdp2.process.transform;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.BigDataProcessor2Menu;
import de.embl.cba.bdp2.dialog.DialogUtils;
import de.embl.cba.bdp2.process.AbstractImageProcessingCommand;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewer.ImageViewer;
import net.imglib2.interpolation.InterpolatorFactory;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = AbstractImageProcessingCommand.class, name = TransformCommand.COMMAND_NAME, menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractImageProcessingCommand.COMMAND_PROCESS_PATH + TransformCommand.COMMAND_FULL_NAME )
public class TransformCommand< R extends RealType< R > & NativeType< R > > extends AbstractImageProcessingCommand< R > implements Command
{
    public static final String COMMAND_NAME = "Transform...";
    public static final String COMMAND_FULL_NAME = BigDataProcessor2Menu.COMMAND_BDP2_PREFIX + COMMAND_NAME;
    public static final String NEAREST = "Nearest";
    public static final String LINEAR = "Linear";
    public static final String AFFINE_LABEL = "Affine transform [m00,..,m03,m10,..,m13,m20,..,m23]";

    @Parameter(label = AFFINE_LABEL )
    String affineTransformCSV = "1,0,0,0,0,1,0,0,0,0,1,0";
    public static final String AFFINE_STRING_PARAMETER = "affineTransformCSV";

    @Parameter(label = "Interpolation", choices = { NEAREST, LINEAR })
    String interpolation = NEAREST;
    public static final String INTERPOLATION_PARAMETER = "interpolation";

    @Override
    public void run()
    {
        process();
        handleOutputImage( false, false );
    }

    private void process()
    {
        final AffineTransform3D affineTransform3D = getAffineTransform3D( affineTransformCSV );
        final InterpolatorFactory interpolatorFactory = Utils.getInterpolator( interpolation );
        outputImage = BigDataProcessor2.transform( inputImage, affineTransform3D, interpolatorFactory );
    }

    public static AffineTransform3D getAffineTransform3D( String affineTransform )
    {
        final double[] doubles = Utils.delimitedStringToDoubleArray( affineTransform, "," );
        final AffineTransform3D transform3D = new AffineTransform3D();
        transform3D.set( doubles );
        return transform3D;
    }

    @Override
    public void showDialog( ImageViewer< R > imageViewer )
    {

    }
}
