package de.embl.cba.bdp2.tools;

import de.embl.cba.bdv.utils.viewer.MultipleImageViewer;
import ij.IJ;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Plugin(type = Command.class, menuPath = "Plugins>BigDataViewer>Open Multiple XML/HDF5" )
public class OpenMultipleImagesInBdvCommand implements Command
{
	@Parameter ( visibility = ItemVisibility.MESSAGE  )
	String help = "HELP: Press F1 and F2 within BigDataViewer.";

	@Parameter ( label = "Choose input files" )
	public File[] inputFiles;

	@Parameter ( label = "Only consider files matching" )
	public String regExp = ".*.xml";

	@Parameter ( label = "Blending mode", choices = { "Avg", "Sum" } )
	public String blendingMode = "Avg";

	public void run()
	{
		final ArrayList< String > validPaths = getValidPaths( regExp, inputFiles );
		final long start = System.currentTimeMillis();
		IJ.log( "Opening " + validPaths.size() + " files...");
		final MultipleImageViewer viewer = new MultipleImageViewer( validPaths );
		viewer.showImages( MultipleImageViewer.BlendingMode.valueOf( blendingMode ) );
		IJ.log( "...done in " + ( System.currentTimeMillis() - start ) + " ms.");
	}

	private ArrayList< String > getValidPaths( String regExp, File[] paths )
	{
		final ArrayList< String > validPaths = new ArrayList<>();

		for( File file : paths )
		{
			final Matcher matcher = Pattern.compile( regExp ).matcher( file.getName() );
			if ( matcher.matches() ) validPaths.add( file.getAbsolutePath() );
		}
		return validPaths;
	}
}
