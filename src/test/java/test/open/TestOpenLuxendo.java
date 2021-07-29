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
import test.Utils;

import java.util.regex.Pattern;

import static de.embl.cba.bdp2.open.NamingSchemes.HDF5;
import static de.embl.cba.bdp2.open.NamingSchemes.LUXENDO;
import static de.embl.cba.bdp2.open.NamingSchemes.T;

public class TestOpenLuxendo
{
    public static void main(String[] args)
    {
        //Utils.prepareInteractiveMode();

        new TestOpenLuxendo().run();
    }

    //@Test
    public void run()
    {
//        String regExp = ".*stack_6_(?<C1>channel_.*)\\/(?<C2>Cam_.*)_(" + T + "\\d+)(?:.lux)" + HDF5;
        //String regExp = ".*stack_6_(?<C1>channel_.*)\\/(?<C2>Cam_.*)_(" + T + "\\d+)(?:.lux).h5";
        String regExp = LUXENDO.replace( NamingSchemes.P, "6" );
        final String s = "/Volumes/cba/exchange/bigdataprocessor/data/mouse_2cam_publication/stack_6_channel_2/Cam_Short_00136.h5";

        Pattern pattern = Pattern.compile( regExp );
        final boolean matches = pattern.matcher( s ).matches();

        // /Volumes/cba/exchange/bigdataprocessor/data/mouse_2cam_publication_figure/stack_6_channel_2
        final Image image = BigDataProcessor2.openHDF5Series(
                "/Volumes/cba/exchange/bigdataprocessor/data/mouse_2cam_publication",
                regExp,
                "Data" );

        BigDataProcessor2.showImage( image, true );
    }
}
