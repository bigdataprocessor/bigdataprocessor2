package de.embl.cba.bdp2.open;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;

import java.io.File;

public abstract class AbstractOpenFileSeriesCommand< R extends RealType< R > & NativeType< R > > extends AbstractOpenCommand< R >
{
    @Parameter(label = "Image data directory", style = "directory" )
    protected File directory;
    public static String DIRECTORY_PARAMETER = "directory";
}
