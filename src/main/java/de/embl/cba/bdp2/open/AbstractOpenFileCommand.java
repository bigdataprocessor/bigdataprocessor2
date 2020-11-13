package de.embl.cba.bdp2.open;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.plugin.Parameter;

import java.io.File;

public abstract class AbstractOpenFileCommand< R extends RealType< R > & NativeType< R > > extends AbstractOpenCommand< R >
{
    @Parameter(label = "Image file", callback = "setFileCallBack")
    protected File file;
    public static String FILE_PARAMETER = "file";

    abstract public void setFileCallBack();
}
