package de.embl.cba.bdp2.dialog;

import net.imglib2.type.numeric.ARGBType;

public class DisplaySettings {

    private double displayRangeMin;
    private double displayRangeMax;
    private final ARGBType color;

    public DisplaySettings( double min, double max, ARGBType color ) {
        this.displayRangeMin = min;
        this.displayRangeMax = max;
        this.color = color;
    }

    public double getDisplayRangeMin() {
        return displayRangeMin;
    }

    public double getDisplayRangeMax() {
        return displayRangeMax;
    }

    public ARGBType getColor()
    {
        return color;
    }
}
