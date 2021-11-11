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
package de.embl.cba.bdp2.save.bdvhdf5;

import bdv.export.ProgressWriter;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.progress.ProgressListener;
import de.embl.cba.bdp2.save.AbstractImageSaver;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdv.utils.io.BdvRaiXYZCTExporter;
import ij.IJ;
import ij.io.LogStream;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import java.io.PrintStream;
import java.util.List;

public class BigDataViewerXMLHDF5Saver< R extends RealType< R > & NativeType< R > > extends AbstractImageSaver
{
	private final Image< R > image;
	private final SavingSettings savingSettings;

	/*
	 * public void export(
	 * 			RandomAccessibleInterval< T > raiXYZCT,
	 * 			String name,
	 * 			String filePathWithoutExtension,
	 * 			double[] calibration,
	 * 			String calibrationUnit,
	 * 			double[] translation // TODO: replace by AffineTransform3D
	 * 	)
	 * @param image
	 * @param savingSettings
	 */
	public BigDataViewerXMLHDF5Saver( Image< R > image, SavingSettings savingSettings )
	{
		this.image = image;
		this.savingSettings = savingSettings;
	}

	@Override
	public void startSave()
	{
		final BdvRaiXYZCTExporter< R > exporter = new BdvRaiXYZCTExporter<>();

		exporter.setProgressWriter( new ProgressAdaptor( this.progressListeners ) );

		exporter.export( image.getRai(), image.getName(), savingSettings.volumesFilePathStump, image.getVoxelDimensions(), image.getVoxelUnit().getSymbol(), new double[]{0,0,0} );
	}

	@Override
	public void stopSave()
	{
		IJ.showMessage( "Unfortunately this cannot be interrupted. You have to close Fiji.");
	}

	class ProgressAdaptor implements ProgressWriter
	{
		protected final PrintStream out = new LogStream();
		protected final PrintStream err = new LogStream();
		private final List< ProgressListener > progressListeners;

		public ProgressAdaptor( List< ProgressListener > progressListeners )
		{
			this.progressListeners = progressListeners;
		}

		@Override
		public PrintStream out()
		{
			return out;
		}

		@Override
		public PrintStream err()
		{
			return err;
		}

		@Override
		public void setProgress( double completionRatio )
		{
			for ( ProgressListener progressListener : progressListeners )
			{
				progressListener.progress( (long) ( completionRatio * 100.0), 100 );
			}
		}
	}
}
