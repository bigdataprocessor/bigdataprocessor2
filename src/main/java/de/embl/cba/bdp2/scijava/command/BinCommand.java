package de.embl.cba.bdp2.scijava.command;

import de.embl.cba.bdp2.bin.Binner;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.image.ImageService;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.command.DynamicCommand;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import javax.swing.*;

@Plugin(type = Command.class, menuPath = "Plugins>BigDataProcessor2>Bin...")
public class BinCommand< R extends RealType< R > & NativeType< R > > extends DynamicCommand
{
    @Parameter(label = "Input Image")
    Image image = ImageService.nameToImage.values().iterator().next();

    @Parameter(label = "Bin width X&Y [pixels]", persist = false)
    int binWidthXYPixels = 1;

    @Parameter(label = "Bin width Z [pixels]", persist = false)
    int binWidthZPixels = 1;

    @Parameter(label = "Output image name", persist = false)
    String outputImageName = ImageService.nameToImage.keySet().iterator().next() + "Binned";

    private BdvImageViewer< R > viewer;
    private Image< R > binned;

    @Override
    public void run()
    {
        SwingUtilities.invokeLater( () ->  {
            bin();
            ImageService.nameToImage.put( binned.getName(), binned );
        });
    }

    private void bin()
    {
        binned = Binner.bin( image, new long[]{ binWidthXYPixels, binWidthXYPixels, binWidthZPixels, 1, 1});
        binned.setName( outputImageName );
        showImage( binned );
    }

    private void showImage( Image< R > binned )
    {
        if ( viewer == null )
        {
            viewer = new BdvImageViewer<>( binned );
        }
        else
        {
            viewer.replaceImage( binned );
        }
    }

    @Override
    public void preview()
    {
        SwingUtilities.invokeLater( () -> {
            bin();
        } );
    }
}
