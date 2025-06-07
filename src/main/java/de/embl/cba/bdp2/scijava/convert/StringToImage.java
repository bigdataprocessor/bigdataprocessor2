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
package de.embl.cba.bdp2.scijava.convert;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.service.ImageService;
import ij.IJ;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;
import org.scijava.convert.AbstractConverter;
import org.scijava.plugin.Plugin;

@Plugin(type = org.scijava.convert.Converter.class)
public class StringToImage<I extends String, O extends Image > extends AbstractConverter<I, O> {

    @Override
    public <T> T convert(Object src, Class<T> dest) {

        if ( ! ImageService.imageNameToImage.containsKey( src ) )
        {
            String imageNames = String.join( "\n", ImageService.imageNameToImage.keySet() );
            throw new RuntimeException( "Image not found: " + src +
                    "\nAvailable images:\n" + imageNames );
        }

        T image = ( T ) ImageService.imageNameToImage.get( src );

        if ( image == null )
        {
            throw new RuntimeException( "Image with name " + src + " is null." );
        }

        return image;
    }

    @Override
    public Class<O> getOutputType() {
        return (Class<O>) Image.class;
    }

    @Override
    public Class<I> getInputType() {
        return (Class<I>) String.class;
    }

    public < R extends RealType< R > & NativeType< R > > Image< R > getImage( String imageName )
    {
        Image< R > image = new StringToImage< String, Image< R > >().convert( imageName, Image.class );

        return image;
    }

}
