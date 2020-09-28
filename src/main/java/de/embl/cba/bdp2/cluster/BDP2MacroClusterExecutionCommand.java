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

@Plugin(type = Command.class, menuPath = "Plugins>EMBL>BDP2 Execute Macro on Cluster" )
public class BDP2MacroClusterExecutionCommand extends AbstractClusterSubmitterCommand
{
	@Parameter ( label = "Macro files" )
	File[] macros;

	@Override
	public void run()
	{
		createJobSubmitter( executable.toString() + JobSubmitter.RUN_IJ_MACRO_OPTIONS );
		jobFutures = submitJobs( macros );
		monitorJobs( jobFutures );
	}

	private ArrayList< JobFuture > submitJobs( File[] macros )
	{
		ArrayList< JobFuture > jobFutures = new ArrayList<>( );

		for ( File macro : macros )
		{
			// TODO: create time points sub setting
			jobSubmitter.clearCommands();

			String macroString = new HeadlessMacroCreator( macros[ 0 ] ).createHeadlessExecutableMacroString();
			macroString = PathMapper.asEMBLClusterMounted( macroString );
			jobSubmitter.addIJMacroExecution( macroString );
			JobSettings jobSettings = getJobSettings( macro );
			jobFutures.add( jobSubmitter.submitJobs( jobSettings ) );
			Utils.wait( 500 );
		}

		return jobFutures;
	}

	private JobSettings getJobSettings( File macro )
	{
		JobSettings jobSettings = new JobSettings();
		jobSettings.numWorkersPerNode = numWorkers;
		jobSettings.queue = JobSettings.DEFAULT_QUEUE;
		jobSettings.memoryPerJobInMegaByte = 16000; // TODO
		jobSettings.timePerJobInMinutes = 60; // TODO
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
