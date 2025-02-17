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
package users.isabell;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.process.align.splitchip.SplitChipDialog;
import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.viewer.ImageViewer;
import net.imagej.ImageJ;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class MergeSplitChipDialog
{
    public static < R extends RealType< R > & NativeType< R > >
    void main( String[] args )
    {
        final ImageJ imageJ = new ImageJ();
        imageJ.ui().showUI();

        final Image< R > image = BigDataProcessor2.openHDF5Series(
                "/Users/tischer/Desktop/stack_0_channel_0",
                ".*.h5",
                "Data");

        image.setVoxelUnit( "micrometer" );
        image.setVoxelDimensions( new double[]{0.13, 0.13, 1.04} );

        final ImageViewer viewer = BigDataProcessor2.showImage( image);
        new SplitChipDialog( viewer );
    }

}
