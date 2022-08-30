/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2022 EMBL
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
package de.embl.cba.bdp2.open.fileseries;

import ch.epfl.biop.bdv.bioformats.BioFormatsMetaDataHelper;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.open.CacheUtils;
import de.embl.cba.bdp2.open.CachedCellImgCreator;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.cache.img.ReadOnlyCachedCellImgFactory;
import net.imglib2.cache.img.ReadOnlyCachedCellImgOptions;
import net.imglib2.cache.img.optional.CacheOptions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import ome.units.quantity.Length;
import ome.units.unit.Unit;

import java.io.File;
import java.util.Arrays;

import static net.imglib2.cache.img.ReadOnlyCachedCellImgOptions.options;

public class FileSeriesCachedCellImgCreator< R extends RealType< R > & NativeType< R > > implements CachedCellImgCreator< R >
{
    private final FileInfos fileInfos;
    private final String imageName;
    private final long[] imageDimsXYZCT;
    private String[] channelNames;
    private double[] voxelSize;
    private Unit< Length > voxelUnit;

    public FileSeriesCachedCellImgCreator( FileInfos fileInfos )
    {
        this.fileInfos = fileInfos;
        this.imageDimsXYZCT = new long[]{ (long) fileInfos.nX, (long) fileInfos.nY, (long) fileInfos.nZ, (long) fileInfos.nC, (long) fileInfos.nT };
        this.channelNames = fileInfos.channelNames;
        this.voxelSize = fileInfos.voxelSize;
        setVoxelUnit( fileInfos );
        this.imageName = new File( fileInfos.directory ).getName();
    }

    public void setVoxelUnit( FileInfos fileInfos )
    {
        try
        {
            this.voxelUnit = BioFormatsMetaDataHelper.getUnitFromString( fileInfos.voxelUnit );
        } catch ( Exception e )
        {
            Logger.warn( "Could not convert voxel size " + fileInfos.voxelUnit + " into BioFormats' Unit< Length >.");
            this.voxelUnit = null;
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
        return channelNames;
    }

    @Override
    public ARGBType[] getChannelColors()
    {
        ARGBType[] argbTypes = new ARGBType[ fileInfos.nC ];
        for ( ARGBType argbType : argbTypes )
        {
            argbType.set( ARGBType.rgba( 1.0, 1.0, 1.0, 1.0 ) );
        }

        return new ARGBType[ 0 ];
    }

    @Override
    public double[] getVoxelSize()
    {
        return voxelSize;
    }

    @Override
    public Unit< Length > getVoxelUnit()
    {
        return voxelUnit;
    }

    @Override
    public int[] getDefaultCellDimsXYZCT()
    {
        // try to construct sensible cell dimensions for fast plane wise browsing
        long[] imageDimensionsXYZ = Arrays.stream( imageDimsXYZCT ).limit( 3 ).toArray();

        // int[] cellDims = CacheUtils.planeWiseCellDims( imageDimensionsXYZ, fileInfos.bitDepth, isPlaneWiseCompressed( fileInfos ) );
        // to simplify benchmarking and maybe this still is the fastest in most cases anyway
        int [] cellDims = CacheUtils.planeWiseCellDims( imageDimensionsXYZ, fileInfos.bitDepth, true );

        return cellDims;
    }

    public CachedCellImg< R, ? > createCachedCellImg( int[] cellDimsXYZCT, CacheOptions.CacheType cacheType, long cacheSize )
    {
        final FileSeriesCellLoader loader = new FileSeriesCellLoader<>( fileInfos, cellDimsXYZCT );

        final ReadOnlyCachedCellImgOptions options = options()
                .cellDimensions( loader.getCellDims() )
                .cacheType( cacheType )
                .maxCacheSize( cacheSize );

        final CachedCellImg cachedCellImg = new ReadOnlyCachedCellImgFactory().create(
                loader.getDimensions(),
                fileInfos.getType(),
                loader,
                options);

        return cachedCellImg;
    }
}
