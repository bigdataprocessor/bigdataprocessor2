/**
 * @author Ashis Ravindran
 * Code adapted and modified for customization
 * from BigDataViewer core.
 *
 * Original Authors: Tobias Pietzsch, Stephan Saalfeld, Stephan Preibisch,
 * Jean-Yves Tinevez, HongKee Moon, Johannes Schindelin, Curtis Rueden, John Bogovic.
 */
package de.embl.cba.bdp2.boundingbox;

import bdv.tools.brightness.SliderPanel;
import bdv.util.BoundedInterval;
import net.imglib2.FinalRealInterval;
import net.imglib2.RealInterval;

import javax.swing.*;

public class CustomBoxSelectionPanel extends JPanel {

    public interface Box {
        RealInterval getInterval();
        void setInterval(RealInterval interval);
    }

    private static final long serialVersionUID = 1L;

    private final BoundedInterval[] ranges;

    private final SliderPanel[] minSliderPanels;

    private final SliderPanel[] maxSliderPanels;

    private final Box selectionBox;

    private int cols;

    private final int n;


    public CustomBoxSelectionPanel(final Box selectionBox,
                                   final RealInterval rangeInterval,
                                   final String[] axes) {
        n = selectionBox.getInterval().numDimensions();
        if (n != axes.length) {
            throw new RuntimeException("axes length doesn't match with realInterval dims");
        }
        this.selectionBox = selectionBox;
        ranges = new BoundedInterval[n];
        minSliderPanels = new SliderPanel[n];
        maxSliderPanels = new SliderPanel[n];
        cols = 2;
        for (int d = 0; d < n; ++d) {
            cols = Math.max(cols, Double.toString(rangeInterval.realMin(d)).length());
            cols = Math.max(cols, Double.toString(rangeInterval.realMax(d)).length());
        }

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        for (int d = 0; d < n; ++d) {
            final double rangeMin = rangeInterval.realMin(d);
            final double rangeMax = rangeInterval.realMax(d);
            final RealInterval interval = selectionBox.getInterval();
            final double initialMin = Math.max( interval.realMin(d), rangeMin );
            final double initialMax = Math.min( interval.realMax(d), rangeMax );
            // TODO: introduce scaling factor to select non-integer values
            final BoundedInterval range = new BoundedInterval(
                    (int) rangeMin,
                    (int) rangeMax,
                    (int) initialMin,
                    (int) initialMax,
                    0) {
                @Override
                protected void updateInterval( final int min, final int max)
                {
                    updateSelection();
                }
            };
            final JPanel sliders = new JPanel();
            sliders.setLayout(new BoxLayout(sliders, BoxLayout.PAGE_AXIS));
            //final String axis = ( d == 0 ) ? "x" : ( d == 1 ) ? "y" : "z";
            final String axis = axes[d];
            final SliderPanel minPanel = new SliderPanel(axis + " min", range.getMinBoundedValue(), 1);
            minPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
            minPanel.setNumColummns(cols);
            sliders.add(minPanel);
            final SliderPanel maxPanel = new SliderPanel(axis + " max", range.getMaxBoundedValue(), 1);
            maxPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
            maxPanel.setNumColummns(cols);
            sliders.add(maxPanel);
            add(sliders);
            minSliderPanels[d] = minPanel;
            maxSliderPanels[d] = maxPanel;
            ranges[d] = range;
        }
    }

    private void updateSelection() {
        final double[] min = new double[n];
        final double[] max = new double[n];
        for (int d = 0; d < n; ++d) {
            // TODO: one could add a scaling factor here to select non-integer values
            min[d] = ranges[d].getMinBoundedValue().getCurrentValue();
            max[d] = ranges[d].getMaxBoundedValue().getCurrentValue();
        }
        selectionBox.setInterval( new FinalRealInterval(min, max) );
    }

}
