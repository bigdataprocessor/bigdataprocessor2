package de.embl.cba.bdp2.process;

import de.embl.cba.bdp2.Image;
import de.embl.cba.bdp2.logging.Logger;
import de.embl.cba.bdp2.utils.Utils;
import de.embl.cba.bdp2.viewers.BdvImageViewer;
import net.imglib2.FinalInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class CroppingDialog< T extends RealType< T > & NativeType< T > >
{
	public CroppingDialog( BdvImageViewer< T > viewer )
	{
		Logger.info( "\nCropping..." );
		FinalInterval interval = viewer.get5DIntervalFromUser( true );
		final Image< T > image = viewer.getImage();

		if ( interval != null )
		{
			Image< T > cropped = Cropper.crop( image, interval );

			final BdvImageViewer< T > newViewer = new BdvImageViewer<>( cropped );
			newViewer.setDisplaySettings( viewer.getDisplaySettings() );

			Logger.info( "Cropped view size [GB]: " + Utils.getSizeGB( cropped.getRai() ) );
		}

	}

}
