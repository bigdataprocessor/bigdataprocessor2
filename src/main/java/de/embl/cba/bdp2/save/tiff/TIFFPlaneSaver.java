/*-
 * #%L
 * Fiji plugin for inspection and processing of big image data
 * %%
 * Copyright (C) 2018 - 2025 EMBL
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
package de.embl.cba.bdp2.save.tiff;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.log.Logger;
import de.embl.cba.bdp2.save.SavingSettings;
import de.embl.cba.bdp2.utils.DimensionOrder;
import ij.ImagePlus;
import ij.io.FileSaver;
import loci.common.services.ServiceFactory;
import loci.formats.ImageWriter;
import loci.formats.meta.IMetadata;
import loci.formats.out.TiffWriter;
import loci.formats.services.OMEXMLService;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import ome.xml.model.enums.PixelType;
import ome.xml.model.primitives.PositiveInteger;

import java.util.concurrent.atomic.AtomicBoolean;

import static de.embl.cba.bdp2.utils.RAISlicer.createPlaneCopy;
import static de.embl.cba.bdp2.save.tiff.TIFFUtils.ShortToByteBigEndian;

public class TIFFPlaneSaver < R extends RealType< R > & NativeType< R > > implements Runnable {

    private final int c;
    private final int t;
    private final int z;
    private final Image< R > image;
    private final SavingSettings savingSettings;
    private final AtomicBoolean stop;

    public TIFFPlaneSaver( int c,
                           int t,
                           int z,
                           Image< R > image,
                           SavingSettings settings,
                           AtomicBoolean stop) {
        this.c = c;
        this.z = z;
        this.t = t;
        this.image = image;
        this.savingSettings = settings;
        this.stop = stop;
    }

    @Override
    public void run() {

        Logger.debug( "# TIFFPlaneSaver..." );

        if ( stop.get() )
        {
            savingSettings.saveVolumes = true;
            return;
        }

        final RandomAccessibleInterval raiXY
                = createPlaneCopy(
                        image.getRai(),
                        image.getRai(),
                        ( R ) image.getType(),
                        z, c, t );

        @SuppressWarnings("unchecked")
        ImagePlus imp = ImageJFunctions.wrap( raiXY, "slice");
        imp.setDimensions(1, 1, 1);

        final long nC = image.getRai().dimension( DimensionOrder.C );
        final long nT = image.getRai().dimension( DimensionOrder.T );

        if ( ! savingSettings.compression.equals( SavingSettings.COMPRESSION_NONE ) )
        {
            saveCompressed( imp, nC, nT, savingSettings.compression );
        }
        else
        {
            FileSaver fileSaver = new FileSaver( imp );
            fileSaver.saveAsTiff( getPath( false, nC, nT ) );
        }
    }

    private void saveCompressed( ImagePlus imp, long nC, long nT, String compression )
    {
        try
        {
            ServiceFactory factory = new ServiceFactory();
            OMEXMLService service = factory.getInstance( OMEXMLService.class );
            IMetadata meta = service.createOMEXMLMetadata();
            meta.setImageID( "Image:0", 0 );
            meta.setPixelsID( "Pixels:0", 0 );
            meta.setPixelsBinDataBigEndian( Boolean.TRUE, 0, 0 );
            meta.setPixelsDimensionOrder( ome.xml.model.enums.DimensionOrder.XYZCT, 0 );

            if ( imp.getBytesPerPixel() == 2 )
                meta.setPixelsType( PixelType.UINT16, 0 );
            else if ( imp.getBytesPerPixel() == 1 )
                meta.setPixelsType( PixelType.UINT8, 0 );

            meta.setPixelsSizeX( new PositiveInteger( imp.getWidth() ), 0 );
            meta.setPixelsSizeY( new PositiveInteger( imp.getHeight() ), 0 );
            meta.setPixelsSizeZ( new PositiveInteger( imp.getNSlices() ), 0 );
            meta.setPixelsSizeC( new PositiveInteger( 1 ), 0 );
            meta.setPixelsSizeT( new PositiveInteger( 1 ), 0 );

            int channel = 0;
            meta.setChannelID( "Channel:0:" + channel, 0, channel );
            meta.setChannelSamplesPerPixel( new PositiveInteger( 1 ), 0, channel );

            ImageWriter writer = new ImageWriter();
            writer.setValidBitsPerPixel( imp.getBytesPerPixel() * 8 );
            writer.setMetadataRetrieve( meta );
            writer.setId( getPath( true, nC, nT ) );
            writer.setWriteSequentially( true ); // ? is this necessary

            if ( compression.equals( SavingSettings.COMPRESSION_LZW ) )
                writer.setCompression( TiffWriter.COMPRESSION_LZW );
            else if ( compression.equals( SavingSettings.COMPRESSION_ZLIB ) )
                writer.setCompression( TiffWriter.COMPRESSION_ZLIB );

            // save using planes
            if (imp.getBytesPerPixel() == 2)
                writer.saveBytes( 0, ShortToByteBigEndian((short[]) imp.getStack().getProcessor(1).getPixels() ) );
            else if (imp.getBytesPerPixel() == 1)
                writer.saveBytes( 0, (byte[]) (imp.getStack().getProcessor(1).getPixels() ) );

            //
                // save using strips
//                long[] rowsPerStripArray = new long[]{ rowsPerStrip };
//                TiffWriter tiffWriter = ( TiffWriter ) writer.getWriter();

//                    IFD ifd = new IFD();
//                    ifd.put( IFD.ROWS_PER_STRIP, rowsPerStripArray );
//                    if ( imp.getBytesPerPixel() == 2 )
//                    {
//                        tiffWriter.saveBytes( z, ShortToByteBigEndian( ( short[] ) imp.getStack().getProcessor( z + 1 ).getPixels() ), ifd );
//                    } else if ( imp.getBytesPerPixel() == 1 )
//                    {
//                        tiffWriter.saveBytes( z, ( byte[] ) ( imp.getStack().getProcessor( z + 1 ).getPixels() ), ifd );
//                    }

            writer.close();

        }
        catch ( Exception e )
        {
            Logger.error( e.toString() );
        }
    }

    private String getPath( boolean isOME, long nC, long nT )
    {
        String sC = String.format("%1$02d", c);
        String sT = String.format("%1$05d", t);
        String sZ = String.format("%1$05d", z);

        String pathCTZ;

        String extension = getExtension( isOME );

        if ( nC > 1 || nT > 1 )
            pathCTZ = savingSettings.volumesFilePathStump + "--C" + sC + "--T" + sT + "--Z" + sZ + extension;
        else
            pathCTZ = savingSettings.volumesFilePathStump + "--Z" + sZ + extension;

        return pathCTZ;
    }

    private String getExtension( boolean isOME )
    {
        String extension;
        if ( isOME )
            extension = ".ome.tif";
        else
            extension = ".tif";
        return extension;
    }
}
