/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2021 EMBL
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */
package de.embl.cba.bdp2.cluster;

import de.embl.cba.bdp2.record.HeadlessMacroCreator;
import de.embl.cba.bdp2.scijava.Services;
import de.embl.cba.cluster.AbstractClusterSubmitterCommand;
import de.embl.cba.cluster.JobExecutor;
import de.embl.cba.cluster.JobFuture;
import de.embl.cba.cluster.JobSettings;
import de.embl.cba.cluster.JobSubmitter;
import de.embl.cba.morphometry.Utils;
import de.embl.cba.util.PathMapper;
import net.imagej.ImageJ;
import net.imglib2.type.logic.BoolType;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Plugin(type = Command.class, menuPath = "Plugins>EMBL>BDP2 Submit Macro to Cluster" )
public class BDP2MacroClusterSubmitterCommand extends AbstractClusterSubmitterCommand
{
	public static final String T_START = "tstart=";
	public static final String T_END = "tend=";
	public static final String NUMPROCESSINGTHREADS = "numprocessingthreads=";

	@Parameter ( label = "Macro files" )
	public File[] macros;

	//@Parameter ( label = "Timepoints to process [-1 = all]" )
	int timePointsToProcess = -1;

	@Parameter ( label = "Timepoints per job" )
	public int timePointsPerJob = 5;

	@Parameter ( label = "Maximal execution time per timepoint [Minutes]" )
	public int minutesPerTimePoint = 20;

	@Parameter ( label = "Maximal memory usage [MB]" )
	public int memory = 16000;

	@Override
	public void run()
	{
		createJobSubmitter( executable.toString() + " -Djava.io.tmpdir=$TMPDIR" + JobSubmitter.RUN_IJ_MACRO_OPTIONS, new JobExecutor() );
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

			int tStartInMacro = getNumber( macro, ".*" + T_START + "(?<x>\\d+).*" );
			int tEndInMacro = getNumber( macro, ".*" + T_END + "(?<x>\\d+).*" );

			macro = setNumThreads( macro );

			int tStart = tStartInMacro;
			int tEnd = getTEnd( tEndInMacro, tStart );

			for ( int t = tStart; t <= tEnd; t+= timePointsPerJob )
			{
				jobSubmitter.clearCommands();
				jobSubmitter.addLinuxCommand( "source /etc/profile.d/z00_lmod.sh" );
				jobSubmitter.addLinuxCommand( "module load HDF5" );

				int t0 = t;
				int t1 = t + timePointsPerJob - 1;
				t1 = t1 <= tEnd ? t1 : tEnd;

				String timeSubsetMacro = macro.replace( T_START + tStartInMacro, T_START + t0 );
				timeSubsetMacro = timeSubsetMacro.replace( T_END + tEndInMacro, T_END + t1 );

				jobSubmitter.addIJMacroExecution( timeSubsetMacro );
				JobSettings jobSettings = getJobSettings( t0, t1 );
				jobFutures.add( jobSubmitter.submitJob( jobSettings ) );
				Utils.wait( 500 );
			}
		}

		return jobFutures;
	}

	private int getTEnd( int tEndInMacro, int tStart )
	{
		if ( timePointsToProcess == -1 )
		{
			return tEndInMacro; // process all
		}
		else
		{
			return Math.min( tEndInMacro, tStart + timePointsToProcess - 1);
		}
	}

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

	private JobSettings getJobSettings( int t0, int t1 )
	{
		JobSettings jobSettings = new JobSettings();
		jobSettings.numWorkersPerNode = numWorkers;
		jobSettings.queue = JobSettings.DEFAULT_QUEUE;
		jobSettings.memoryPerJobInMegaByte = memory; // TODO: Make configurable?
		jobSettings.timePerJobInMinutes = ( t1 - t0 + 1) * minutesPerTimePoint;
		return jobSettings;
	}

	public static void main( String[] args )
	{
		final ImageJ ij = new ImageJ();
		ij.ui().showUI();

		Services.setCommandService( ij.command() );
		ij.command().run( BDP2MacroClusterSubmitterCommand.class, true );
	}
}
