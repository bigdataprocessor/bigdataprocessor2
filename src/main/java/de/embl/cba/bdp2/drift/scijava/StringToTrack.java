package de.embl.cba.bdp2.drift.scijava;

import de.embl.cba.bdp2.drift.track.Track;
import de.embl.cba.bdp2.drift.track.TrackManager;
import org.scijava.convert.AbstractConverter;
import org.scijava.plugin.Plugin;

@Plugin(type = org.scijava.convert.Converter.class)
public class StringToTrack<I extends String, O extends Track > extends AbstractConverter<I, O>
{
    @Override
    public <T> T convert(Object src, Class<T> dest) {
        return ( T ) TrackManager.getTracks().get( src );
    }

    @Override
    public Class<O> getOutputType() {
        return (Class<O>) Track.class;
    }

    @Override
    public Class<I> getInputType() {
        return (Class<I>) String.class;
    }
}
