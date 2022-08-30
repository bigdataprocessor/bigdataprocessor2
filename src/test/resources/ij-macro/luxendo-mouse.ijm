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

run("BDP2 Open Position And Channel Subset...", "viewingmodality=[Show in new viewer] directory=[/Volumes/cba/exchange/bigdataprocessor/data/mouse_2cam_publication] enablearbitraryplaneslicing=false regexp=[.*\\/[sS]tack_6_(?<C1>[cC]hannel_.*)\\/(?<C2>Cam_.*)_(?<T>\\d+)(|.lux).h5] channelsubset=[channel_2_Cam_Long,channel_2_Cam_Short] ");
run("BDP2 Crop...", "inputimage=[mouse_2cam_publication] outputimagename=[mouse_2cam_publication-crop] viewingmodality=[Show in new viewer] minx=682 miny=553 minz=31 minc=0 mint=0 maxx=1690 maxy=1616 maxz=61 maxc=1 maxt=0 ");
run("BDP2 Save As...", "inputimage=[mouse_2cam_publication-crop] directory=[/Volumes/cba/exchange/bigdataprocessor/data/bdp2-out-tmp/] numiothreads=1 numprocessingthreads=4 filetype=[TIFFVolumes] saveprojections=false projectionmode=[sum] savevolumes=true channelnames=[Channel index (C00, C01, ...)] tiffcompression=[None] tstart=0 tend=0 ");
