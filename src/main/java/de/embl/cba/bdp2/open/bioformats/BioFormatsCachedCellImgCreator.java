/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2023 EMBL
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
package de.embl.cba.bdp2.open.bioformats;

import bdv.BigDataViewer;
import bdv.ViewerImgLoader;
import bdv.img.cache.VolatileCachedCellImg;
import bdv.img.cache.VolatileGlobalCellCache;
import bdv.spimdata.WrapBasicImgLoader;
import bdv.viewer.Source;
import bdv.viewer.SourceAndConverter;
import ch.epfl.biop.bdv.img.legacy.bioformats.BioFormatsBdvOpener;
import ch.epfl.biop.bdv.img.legacy.bioformats.BioFormatsToSpimData;
import ch.epfl.biop.bdv.img.legacy.bioformats.BioFormatsTools;
import de.embl.cba.bdp2.open.CachedCellImgCreator;
import loci.formats.IFormatReader;
import loci.formats.MetadataTools;
import loci.formats.meta.IMetadata;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.optional.CacheOptions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import ome.units.UNITS;
import ome.units.quantity.Length;
import ome.units.unit.Unit;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BioFormatsCachedCellImgCreator < R extends RealType< R > & NativeType< R > > implements CachedCellImgCreator< R >
{
	private RandomAccessibleInterval raiXYCZT;
	private String imageName = "";
	private long sizeX, sizeY, sizeZ;
	private int sizeC, sizeT;
	private int[] cacheSize;
	private ARGBType[] channelColors;
	private double[] voxelSize = new double[3];
	private int seriesCount;

	public BioFormatsCachedCellImgCreator( String filePath, int series ) {

		imageName = FilenameUtils.removeExtension( new File( filePath ).getName() ); // + "_S"+series;

		BioFormatsBdvOpener opener = BioFormatsBdvOpener.getOpener()
				.location( filePath )
				.auto()
				.cornerPositionConvention()
				.micrometer()
				.cacheBounded( 100 ) // TODO : is this value ok ?
				.positionReferenceFrameLength( new Length( 1, UNITS.MICROMETER ) ) // Compulsory
				.voxSizeReferenceFrameLength( new Length( 1, UNITS.MICROMETER ) );

		try ( IFormatReader reader = opener.getReaderPool().acquire() ) {

			IMetadata meta = (IMetadata) (reader.getMetadataStore());

			seriesCount = opener.getNewReader().getSeriesCount();

			AbstractSpimData<?> spimData = BioFormatsToSpimData.getSpimData(opener);

			final AbstractSequenceDescription< ?, ?, ? > seq = spimData.getSequenceDescription();
			final int numTimepoints = seq.getTimePoints().size();
			final VolatileGlobalCellCache cache = ( VolatileGlobalCellCache ) ( (ViewerImgLoader) seq.getImgLoader() ).getCacheControl();
			cache.clearCache();

			WrapBasicImgLoader.wrapImgLoaderIfNecessary( spimData );
			final ArrayList<SourceAndConverter< ? >> sources = new ArrayList<>();
			BigDataViewer.initSetups( spimData, new ArrayList<>(), sources );

			// Count all the setups before the one of the series of interest
			int firstSetup = 0;
			for (int i = 0; i<series; i++) {
				firstSetup+=meta.getChannelCount(i);
			}

			RandomAccessibleInterval<R> modelRAI = (RandomAccessibleInterval<R>) sources.get(firstSetup).getSpimSource().getSource(0,0);

			sizeX = modelRAI.dimension(0); // limited to 2GPixels in one dimension
			sizeY = modelRAI.dimension(1);
			sizeZ = modelRAI.dimension(2);
			sizeC = meta.getChannelCount(series);
			sizeT = numTimepoints;

			channelColors = new ARGBType[sizeC];

			// TODO : sanity check identical size in XYZCT for all channels. Currently assuming selecting one series does the trick

			List<RandomAccessibleInterval<R>> raisXYZCT = new ArrayList<>();

			int[] cacheSizeXYZ = new int[3];

			for (int iTime = 0; iTime<sizeT;iTime++) {
				List<RandomAccessibleInterval<R>> raisXYZC = new ArrayList<>();
				for (int iChannel = 0; iChannel<sizeC;iChannel++) {
					Source<R> source = (Source<R>) sources.get(firstSetup+iChannel).getSpimSource();
					channelColors[iChannel] = BioFormatsTools.getColorFromMetadata(meta, series, iChannel);
					raisXYZC.add(source.getSource(iTime,0));
					source.getVoxelDimensions().dimensions(voxelSize);
				}
				((VolatileCachedCellImg) raisXYZC.get(0)).getCellGrid().cellDimensions(cacheSizeXYZ);
				raisXYZCT.add(Views.stack(raisXYZC));
			}

			cacheSize = new int[]{cacheSizeXYZ[0], cacheSizeXYZ[1], cacheSizeXYZ[2],1,1};

			raiXYCZT = Views.stack( raisXYZCT );
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getImageName()
	{
		return imageName;
	}

	@Override
	public String[] getChannelNames()
	{
		String[] nameChannels = new String[sizeC];

		for (int iChannel = 0;iChannel<sizeC;iChannel++) {
			nameChannels[iChannel] = "channel_"+iChannel;
		}

		return nameChannels;
	}

	@Override
	public ARGBType[] getChannelColors()
	{
		ARGBType[] colorChannels = new ARGBType[sizeC];

		for (int iChannel = 0;iChannel<sizeC;iChannel++) {
			colorChannels[iChannel] = new ARGBType(ARGBType.rgba(255,255,255,128));//"channel_"+iChannel;
		}

		return colorChannels;
	}

	@Override
	public double[] getVoxelSize()
	{
		return voxelSize;
	}

	@Override
	public Unit< Length > getVoxelUnit()
	{
		return UNITS.MICROMETER;
	}

	@Override
	public int[] getDefaultCellDimsXYZCT()
	{
		return cacheSize;
	}

	@Override
	public RandomAccessibleInterval< R > createCachedCellImg( int[] cellDimsXYZCT, CacheOptions.CacheType cacheType, long cacheSize )
	{
		// TODO: in fact here the raiXYCZT should be built with a cache according to the function arguments
		return raiXYCZT;
	}

	public int getSeriesCount()
	{
		return seriesCount;
	}
}
