package users.maxim;

import de.embl.cba.bdv.utils.viewer.MultipleImageViewer;
import net.imagej.ImageJ;

import java.io.File;
import java.util.ArrayList;

import static de.embl.cba.bdv.utils.FileUtils.getFileList;

public class Explore2SourcesOfXRayData
{
	public static void main( String[] args )
	{
		final ImageJ imageJ = new ImageJ();
		imageJ.ui().showUI();

		final String excludeTransformedRegExp = "^(?!.*?(?:transformed)).*xml$";
		final ArrayList< File > paths = getFileList( new File( "/Volumes/cba/exchange/maxim/ver2/2sources" ),
				excludeTransformedRegExp, false );

		final MultipleImageViewer viewer = new MultipleImageViewer( paths );
		viewer.setOpService( imageJ.op() );
		viewer.showImages( MultipleImageViewer.BlendingMode.Avg );
	}
}
