package de.embl.cba.bdp2.cluster;

import de.embl.cba.bdp2.macro.HeadlessMacroCreator;
import de.embl.cba.bdp2.scijava.Services;
import de.embl.cba.cluster.AbstractClusterSubmitterCommand;
import de.embl.cba.cluster.JobFuture;
import de.embl.cba.cluster.JobSettings;
import de.embl.cba.cluster.JobSubmitter;
import de.embl.cba.morphometry.Utils;
import de.embl.cba.util.PathMapper;
import net.imagej.ImageJ;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Plugin(type = Command.class, menuPath = "Plugins>EMBL>BDP2 Execute Macro on Cluster" )
public class BDP2MacroClusterExecutionCommand extends AbstractClusterSubmitterCommand
{
	public static final String T_START = "tstart=";
	public static final String T_END = "tend=";
	@Parameter ( label = "Macro files" )
	File[] macros;

	@Parameter ( label = "Time points per job" )
	int timePointsPerJob = 10;

	@Override
	public void run()
	{
		createJobSubmitter( executable.toString() + JobSubmitter.RUN_IJ_MACRO_OPTIONS );
		jobFutures = submitJobs( macros );
		new Thread( () ->  monitorJobs( jobFutures ) ).start();
	}

	private ArrayList< JobFuture > submitJobs( File[] macros )
	{
		ArrayList< JobFuture > jobFutures = new ArrayList<>( );

		for ( File file : macros )
		{
			String macro = new HeadlessMacroCreator( file ).createHeadlessExecutableMacroString();
			macro = PathMapper.asEMBLClusterMounted( macro );

			int tStart = getTimepoint( macro, ".*" + T_START + "(?<TSTART>\\d+).*" );
			int tEnd = getTimepoint( macro, ".*" + T_END + "(?<TSTART>\\d+).*" );

			for ( int t = tStart; t <= tEnd; t+=timePointsPerJob )
			{
				jobSubmitter.clearCommands();

				int t0 = t;
				int t1 = t + timePointsPerJob - 1;
				t1 = t1 <= tEnd ? t1 : tEnd;

				String timeSubsetMacro = macro
						.replace( T_START + tStart, T_START + t0 )
						.replace( T_END + tEnd, T_END + t1 );

				jobSubmitter.addIJMacroExecution( timeSubsetMacro );
				JobSettings jobSettings = getJobSettings( timeSubsetMacro, t0, t1 );
				jobFutures.add( jobSubmitter.submitJob( jobSettings ) );
				Utils.wait( 500 );
			}
		}

		return jobFutures;
	}

	private int getTimepoint( String macroString, String patternStart )
	{
		Matcher matcher = Pattern.compile( patternStart ).matcher(macroString);
		if ( matcher.matches() )
		{
			return Integer.parseInt( matcher.group( 1 ) );
		}
		else
		{
			throw new RuntimeException( "Could not determine starting or ending timepoint of the saving command." );
		}
	}

	private JobSettings getJobSettings( String macro, int t0, int t1 )
	{
		JobSettings jobSettings = new JobSettings();
		jobSettings.numWorkersPerNode = numWorkers;
		jobSettings.queue = JobSettings.DEFAULT_QUEUE;
		jobSettings.memoryPerJobInMegaByte = 16000; // TODO
		jobSettings.timePerJobInMinutes = ( t1 - t0 + 1) * 5; // assume 5 minutes per timepoint (usually much faster).
		return jobSettings;
	}

	public static void main( String[] args )
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		Services.commandService = ij.command();
		ij.command().run( BDP2MacroClusterExecutionCommand.class, true );
	}

}
