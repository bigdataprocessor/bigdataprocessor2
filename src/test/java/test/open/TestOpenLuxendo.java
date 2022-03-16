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
package test.open;

import de.embl.cba.bdp2.BigDataProcessor2;
import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.open.NamingSchemes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import test.Utils;

import java.util.regex.Pattern;

import static de.embl.cba.bdp2.open.NamingSchemes.HDF5;
import static de.embl.cba.bdp2.open.NamingSchemes.LUXENDO;
import static de.embl.cba.bdp2.open.NamingSchemes.LUXENDO_STACKINDEX;
import static de.embl.cba.bdp2.open.NamingSchemes.P;
import static de.embl.cba.bdp2.open.NamingSchemes.T;

public class TestOpenLuxendo
{
    private static Image image;

    public static void main( String[] args)
    {
        Utils.prepareInteractiveMode();
        new TestOpenLuxendo().run();
        BigDataProcessor2.showImage( image, true );
    }

    @Test
    public void run()
    {
        String regExp = LUXENDO.replace( P, "0" );

        image = BigDataProcessor2.openHDF5Series(
                "src/test/resources/test/luxendo-different-stack-size",
                null,
                regExp,
                "Data",
                new String[]{"Channel_2_Cam_Fused"});

        // Note that the above data set is weird (not representative)
        //   as the voxel dimensions are stored in the wrong order.
        // Thus, we only compare the middle dimension
        //   as this is not affected by the order.
        Assertions.assertEquals( image.getVoxelDimensions()[1], 0.25999999046325684, 0.01 );
    }
}
