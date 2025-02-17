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
package test.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.NamingSchemes;
import org.junit.jupiter.api.Test;
import test.Utils;

import static de.embl.cba.bdp2.open.NamingSchemes.Z;

public class TestOpenTiffPlanes
{
    private static Image image;

    public static void main( String[] args)
    {
        Utils.prepareInteractiveMode();
        new TestOpenTiffPlanes().run();
        BigDataProcessor2.showImage( image, true );
    }

    @Test
    public void run()
    {
        final String directory = "src/test/resources/test/tiff-planes";

        image = BigDataProcessor2.openTIFFSeries( directory, ".*_T(" + NamingSchemes.T + "\\d+)__z(" + Z + "\\d+).*_c(" + NamingSchemes.C + "\\d+).*" );

        double[] voxelSize = image.getVoxelDimensions();
        image.setVoxelDimensions( voxelSize[ 0 ], voxelSize[ 1 ], 1.0 ); // necessary because voxel size in z is NaN for single plane Tiff
    }
}
