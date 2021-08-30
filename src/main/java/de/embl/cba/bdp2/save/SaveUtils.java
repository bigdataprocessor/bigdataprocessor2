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
package de.embl.cba.bdp2.save;

import net.imagej.DefaultDataset;
import net.imagej.ImgPlus;
import net.imagej.plugins.commands.imglib.Binner;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converters;
import net.imglib2.converter.RealUnsignedByteConverter;
import net.imglib2.converter.RealUnsignedShortConverter;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.IntervalView;

public class SaveUtils
{
    public static <T extends RealType<T>> RandomAccessibleInterval converter( RandomAccessibleInterval newRai, SavingSettings savingSettings){
        if (savingSettings.convertTo8Bit) {
            if (!(((IntervalView) newRai).firstElement() instanceof UnsignedByteType)){
                newRai = Converters.convert(newRai, new RealUnsignedByteConverter<T>(savingSettings.mapTo0,savingSettings.mapTo255), new UnsignedByteType());
            }
        }else if (savingSettings.convertTo16Bit) {
            if (!(((IntervalView) newRai).firstElement() instanceof UnsignedShortType)) {
                newRai = Converters.convert(newRai, new RealUnsignedShortConverter<T>(savingSettings.mapTo0,savingSettings.mapTo255), new UnsignedShortType()); //TODO : ashis
            }
        }
        return newRai;
    }

    public static<T extends RealType<T>> String doBinning(ImgPlus<T> impBinned,int[] binningA, String path, org.scijava.Context context){
        Binner binner = new Binner();
        DefaultDataset ds = new DefaultDataset(context,impBinned);
        binner.setDataset(ds);
        binner.setFactor(0,binningA[0]);
        binner.setFactor(1,binningA[1]);
        binner.setFactor(2,binningA[2]);
        binner.setValueMethod("Average");
        binner.run();
        ds= (DefaultDataset) binner.getDataset();
        impBinned =  (ImgPlus<T>)ds.getImgPlus();
        String newPath = path + "--bin-" + binningA[0] + "-" + binningA[1] + "-" + binningA[2];
        return newPath;
    }

}
