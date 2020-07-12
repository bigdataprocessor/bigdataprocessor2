package develop;

import de.embl.cba.bdp2.data.UnZipper;

import java.io.File;

public class DevelopUnzipping
{
	public static void main( String[] args )
	{
		final File unzip = UnZipper.unzip( new File( "/Users/tischer/Downloads/mouse-volumes.zip" ) );
	}
}
