package de.embl.cba.bdp2.tracking;

import de.embl.cba.bdp2.Image;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class StaticVolumePhaseCorrelationTracker < R extends RealType< R > & NativeType< R > >
{
	private final Image< R > image;
	private final Settings settings;

	public static class Settings
	{
		public long[] volumeDimensions; // voxels
		public long[] centerStartingPosition;
		public long[] timeInterval;
		public long channel;
	}

	public StaticVolumePhaseCorrelationTracker( Image< R > image, Settings settings )
	{
		this.image = image;
		this.settings = settings;
	}

}
