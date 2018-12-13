/*
 * #%L
 * BigDataViewer core classes with minimal dependencies
 * %%
 * Copyright (C) 2012 - 2016 Tobias Pietzsch, Stephan Saalfeld, Stephan Preibisch,
 * Jean-Yves Tinevez, HongKee Moon, Johannes Schindelin, Curtis Rueden, John Bogovic
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
package de.embl.cba.bigDataTools2.bigDataTrackerGUI;

import bdv.tools.boundingbox.BoundingBoxOverlay;
import bdv.tools.boundingbox.BoundingBoxOverlay.BoundingBoxOverlaySource;
import bdv.tools.boundingbox.BoxRealRandomAccessible;
import bdv.tools.brightness.RealARGBColorConverterSetup;
import bdv.tools.brightness.SetupAssignments;
import bdv.tools.transformation.TransformedSource;
import bdv.util.ModifiableInterval;
import bdv.util.RealRandomAccessibleSource;
import bdv.viewer.DisplayMode;
import bdv.viewer.SourceAndConverter;
import bdv.viewer.ViewerPanel;
import bdv.viewer.VisibilityAndGrouping;
import javafx.geometry.Point3D;
import net.imglib2.Interval;
import net.imglib2.display.RealARGBColorConverter;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.type.numeric.ARGBType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.util.Intervals;

public class TrackedAreaBoxOverlay
{
    private final ModifiableInterval interval;
    private final BoxRealRandomAccessible<UnsignedShortType> boxRealRandomAccessible;
    private final SourceAndConverter<UnsignedShortType> boxSourceAndConverter;
    private final RealARGBColorConverterSetup boxConverterSetup;
    private final BoundingBoxOverlay boxOverlay;
    private boolean repaint;
    private int time=-1;
    private Track trackingResults;

    public TrackedAreaBoxOverlay(
            Track trackingResults,
            final ViewerPanel viewer,
            final SetupAssignments setupAssignments,
            final int boxSetupId,
            final Interval initialInterval){
        this(trackingResults,viewer, setupAssignments, boxSetupId, initialInterval, true, true);
    }

    public TrackedAreaBoxOverlay(
            Track trackingResults,
            final ViewerPanel viewer,
            final SetupAssignments setupAssignments,
            final int boxSetupId,
            final Interval initialInterval,
            final boolean showBoxSource,
            final boolean showBoxOverlay) {

        // create a procedural RealRandomAccessible that will render the bounding box
        final UnsignedShortType insideValue = new UnsignedShortType(1000); // inside the box pixel value is 1000
        final UnsignedShortType outsideValue = new UnsignedShortType(0); // outside is 0
        interval = new ModifiableInterval(initialInterval);
        boxRealRandomAccessible = new BoxRealRandomAccessible<>(interval, insideValue, outsideValue);
        this.trackingResults = trackingResults;
        // create a bdv.viewer.Source providing data from the bbox RealRandomAccessible
        final RealRandomAccessibleSource<UnsignedShortType> boxSource = new RealRandomAccessibleSource<UnsignedShortType>(boxRealRandomAccessible, new UnsignedShortType(), "selection") {
            @Override
            public Interval getInterval(final int t, final int level) {
                if (trackingResults.isTimePresent(t)) {
                    if (t != time) {
                        time = t;
                        repaint = true;
                    } else {
                        repaint = false;
                    }
                    Point3D[] pMinMax = trackingResults.getPosition(t);
                    long[] range = {(long) pMinMax[0].getX(), (long) pMinMax[0].getY(), (long) pMinMax[0].getZ(), (long) pMinMax[1].getX(), (long) pMinMax[1].getY(), (long) pMinMax[1].getZ()};
                    interval.set(Intervals.createMinMax(range));
                    if (repaint) {
                        viewer.requestRepaint();
                    }
                }else {
                    interval.set(Intervals.createMinMax(0, 0, 0, 0, 0, 0));
                }
                return interval;
            }
        };

        // set up a converter from the source type (UnsignedShortType in this case) to ARGBType
        final RealARGBColorConverter<UnsignedShortType> converter = new RealARGBColorConverter.Imp1<>(0, 3000);
        //converter.setColor( new ARGBType( 0x00994499 ) ); // set bounding box color to magenta
        converter.setColor(new ARGBType(0x7AA7D5)); // set bounding box color to ...

        // create a ConverterSetup (can be used by the brightness dialog to adjust the converter settings)
        boxConverterSetup = new RealARGBColorConverterSetup(boxSetupId, converter);
        boxConverterSetup.setViewer(viewer);

        // create a SourceAndConverter (can be added to the viewer for display)
        final TransformedSource<UnsignedShortType> ts = new TransformedSource<>(boxSource);
        boxSourceAndConverter = new SourceAndConverter<>(ts, converter);

        // create an Overlay to show 3D wireframe box
        boxOverlay = new BoundingBoxOverlay(new BoundingBoxOverlaySource() {
            @Override
            public void getIntervalTransform(final AffineTransform3D transform) {
                ts.getSourceTransform(0, 0, transform);
            }

            @Override
            public Interval getInterval() {
                return interval;
            }
        });
        if (showBoxSource) {
            viewer.addSource(boxSourceAndConverter);
            setupAssignments.addSetup(boxConverterSetup);
            boxConverterSetup.setViewer(viewer);

            final int bbSourceIndex = viewer.getState().numSources() - 1;
            final VisibilityAndGrouping vg = viewer.getVisibilityAndGrouping();
            if (vg.getDisplayMode() != DisplayMode.FUSED) {
                for (int i = 0; i < bbSourceIndex; ++i)
                    vg.setSourceActive(i, vg.isSourceVisible(i));
                vg.setDisplayMode(DisplayMode.FUSED);
            }
            vg.setSourceActive(bbSourceIndex, true);
            vg.setCurrentSource(bbSourceIndex);
        }
        if (showBoxOverlay) {
            viewer.getDisplay().addOverlayRenderer(boxOverlay);
            viewer.addRenderTransformListener(boxOverlay);
        }
    }
}

