package de.embl.cba.bdp2.open.fileseries;

import ch.epfl.biop.bdv.bioformats.BioFormatsMetaDataHelper;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.CacheUtils;
import de.embl.cba.bdp2.open.CachedCellImgCreator;
import de.embl.cba.tables.Logger;
import net.imglib2.cache.img.CachedCellImg;
import net.imglib2.cache.img.DiskCachedCellImgOptions;
import net.imglib2.cache.img.ReadOnlyCachedCellImgFactory;
import net.imglib2.cache.img.ReadOnlyCachedCellImgOptions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.RealType;
import ome.units.quantity.Length;
import ome.units.unit.Unit;

import java.io.File;
import java.util.Arrays;

import static de.embl.cba.bdp2.open.CacheUtils.isPlaneWiseCompressed;
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
//        int[] cellDims = CacheUtils.planeWiseCellDims( imageDimensionsXYZ, fileInfos.bitDepth, isPlaneWiseCompressed( fileInfos ) );
        int[] cellDims = CacheUtils.planeWiseCellDims( imageDimensionsXYZ, fileInfos.bitDepth, true ); // to simplify benchmarking
        return cellDims;
    }

    public CachedCellImg< R, ? > createCachedCellImg( int[] cellDimsXYZCT, DiskCachedCellImgOptions.CacheType cacheType, long cacheSize )
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
