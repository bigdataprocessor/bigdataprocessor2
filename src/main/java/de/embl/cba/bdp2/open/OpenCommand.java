package de.embl.cba.bdp2.open;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public interface OpenCommand< R extends RealType< R > & NativeType< R > >
{
    void recordJythonCall();
}
