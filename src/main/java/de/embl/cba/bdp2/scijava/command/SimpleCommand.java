package de.embl.cba.bdp2.scijava.command;

import ij.plugin.frame.Recorder;
import net.imagej.ImageJ;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

@Plugin(type = Command.class, menuPath = "Bin...")
public class SimpleCommand< R extends RealType< R > & NativeType< R > > implements Command
{
    @Parameter(label = "Bin width X&Y [pixels]", min = "1", persist = false)
    int binWidthXYPixels = 1;

    @Override
    public void run()
    {
    }

    public static void main( String[] args )
    {
        final ImageJ imageJ = new ImageJ();
        imageJ.ui().showUI();

        new Recorder();

        imageJ.command().run( SimpleCommand.class, true, "binWidthXYPixels", 3 );
    }
}
