package de.embl.cba.bdp2;

import bdv.viewer.animate.TextOverlayAnimator;
import de.embl.cba.bdp2.crop.CropDialog;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.scijava.Services;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import ij.IJ;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.swing.*;


/**
 * TODO: How to add a HELP button for the regular expression without screwing up the macro recording?
 *
 *
 * @param <R>
 */
@Plugin(type = Command.class, menuPath = "Plugins>BigDataProcessor2>" + BigDataProcessor2Command.COMMAND_FULL_NAME )
public class BigDataProcessor2Command< R extends RealType< R > & NativeType< R > > implements Command
{
    @Parameter
    CommandService cs;

    public static final String COMMAND_NAME = "BigDataProcessor2";
    public static final String COMMAND_FULL_NAME = "" + COMMAND_NAME;

    public void run()
    {
        Services.commandService = cs;
        CropDialog.askForUnitsChoice = true;

        SwingUtilities.invokeLater( () -> {

            BigDataProcessor2UI.showUI();

//            ArrayImgs.unsignedShorts( 10, 10, 10, 1, 1 );
//            final Image< UnsignedShortType > image = new Image<>( ArrayImgs.unsignedShorts( 10, 10, 10, 1, 1 ),
//                    "Welcome!",
//					new String[]{"channel 0"},
//                    new double[]{ 1, 1, 1 },
//                    "micrometer",
//                    null );
//
//            final BdvImageViewer viewer = BigDataProcessor2.showImage( image );
//
//            new Thread( () ->  {
//                IJ.wait( 2000 );
//                viewer.getBdvHandle().getViewerPanel().addOverlayAnimator ( new TextOverlayAnimator( "Go to Menu > BigDataProcessor2 > Open", 10000, TextOverlayAnimator.TextPosition.CENTER ) );
//            }).start();

        } );
    }
}
