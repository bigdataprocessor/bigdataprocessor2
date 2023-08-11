/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2023 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
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

import static de.embl.cba.bdp2.process.transform.TransformConstants.LINEAR;
import static de.embl.cba.bdp2.process.transform.TransformConstants.NEAREST;

@Plugin(type = AbstractImageProcessingCommand.class, name = AffineTransformCommand.COMMAND_NAME, menuPath = DialogUtils.BIGDATAPROCESSOR2_COMMANDS_MENU_ROOT + AbstractImageProcessingCommand.COMMAND_PATH + AffineTransformCommand.COMMAND_FULL_NAME )
public class AffineTransformCommand< R extends RealType< R > & NativeType< R > > extends AbstractImageProcessingCommand< R > implements Command
{
    public static final String COMMAND_NAME = "Affine Transform...";
    public static final String COMMAND_FULL_NAME = BigDataProcessor2Menu.COMMAND_BDP2_PREFIX + COMMAND_NAME;
    public static final String AFFINE_LABEL = "Affine transform (m00,..,m03,m10,..)";// [m00,..,m03,m10,..,m13,m20,..,m23]";

    @Parameter(label = AFFINE_LABEL )
    public String affineTransformCSV = "1,0,0,0,0,1,0,0,0,0,1,0";
    public static final String AFFINE_STRING_PARAMETER = "affineTransformCSV";

    @Parameter(label = "Interpolation", choices = { NEAREST, LINEAR })
    public String interpolation = NEAREST;
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
        new AffineTransformDialog<>( imageViewer ).show();
    }
}
