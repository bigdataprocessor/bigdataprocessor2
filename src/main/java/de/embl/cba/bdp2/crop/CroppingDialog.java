package de.embl.cba.bdp2.crop;

import de.embl.cba.bdp2.image.Image;
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
		FinalInterval interval = viewer.get5DIntervalFromUser( true );

		if ( interval != null )
		{
			final Image< T > image = viewer.getImage();
			Image< T > cropped = Cropper.crop( image, interval );

			Logger.info( "\n# Crop" );
			Logger.info( "Crop interval [" + image.getVoxelUnit() +"]: " + interval.toString() );
			Logger.info( "Crop view size [GB]: " + Utils.getSizeGB( cropped.getRai() ) );

			final BdvImageViewer< T > newViewer = new BdvImageViewer<>( cropped );
			newViewer.setDisplaySettings( viewer.getDisplaySettings() );
		}
	}
}
