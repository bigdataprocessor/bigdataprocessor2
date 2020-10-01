package de.embl.cba.bdp2.macro;

import de.embl.cba.bdp2.scijava.Services;
import de.embl.cba.cluster.AbstractClusterSubmitterCommand;
import de.embl.cba.cluster.JobFuture;
import de.embl.cba.cluster.JobSettings;
import de.embl.cba.cluster.JobSubmitter;
import de.embl.cba.morphometry.Utils;
import de.embl.cba.util.PathMapper;
import net.imagej.ImageJ;
import org.jetbrains.annotations.NotNull;
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
	public static final String NUMPROCESSINGTHREADS = "numprocessingthreads=";

	@Parameter ( label = "Macro files" )
	File[] macros;

	@Parameter ( label = "Timepoints per job" )
	int timePointsPerJob = 10;

	@Parameter ( label = "Minutes per timepoint" )
	int minutesPerTimePoint = 5;

	@Override
	public void run()
	{
		createJobSubmitter( executable + JobSubmitter.RUN_IJ_MACRO_OPTIONS );
		jobFutures = submitJobs( macros );
		new Thread( () ->  {
			monitorJobs( jobFutures );
		} ).start();
	}

	private ArrayList< JobFuture > submitJobs( File[] macros )
	{
		ArrayList< JobFuture > jobFutures = new ArrayList<>( );

		for ( File file : macros )
		{
			String macro = new HeadlessMacroCreator( file ).createHeadlessExecutableMacroString();
			macro = PathMapper.asEMBLClusterMounted( macro );

			int tStart = getNumber( macro, ".*" + T_START + "(?<x>\\d+).*" );
			int tEnd = getNumber( macro, ".*" + T_END + "(?<x>\\d+).*" );

			macro = setNumThreads( macro );

			for ( int t = tStart; t <= tEnd; t+= timePointsPerJob )
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

	@NotNull
	private String setNumThreads( String macro )
	{
		int numProcessingThreads = getNumber( macro, ".*" + NUMPROCESSINGTHREADS + "(?<x>\\d+).*" );

		macro = macro.replace( NUMPROCESSINGTHREADS + numProcessingThreads, NUMPROCESSINGTHREADS + numWorkers );
		return macro;
	}

	private int getNumber( String macroString, String patternStart )
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
		jobSettings.timePerJobInMinutes = ( t1 - t0 + 1) * minutesPerTimePoint;
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
