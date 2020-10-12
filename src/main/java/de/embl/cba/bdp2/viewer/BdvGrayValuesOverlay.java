package de.embl.cba.bdp2.viewer;

import bdv.tools.brightness.ConverterSetup;
import bdv.util.Bdv;
import bdv.util.BdvOverlay;
import bdv.util.PlaceHolderConverterSetup;
import de.embl.cba.bdv.utils.BdvUtils;
import net.imglib2.RealPoint;
import net.imglib2.type.numeric.ARGBType;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BdvGrayValuesOverlay extends BdvOverlay implements MouseMotionListener {
    private final Bdv bdv;
    private final String fontName;
    private final int fontSize;
    private ArrayList<Double> values;
    private ArrayList<ARGBType> colors;

    public BdvGrayValuesOverlay(Bdv bdv, int fontSize,String fontFace) {
        super();
        this.bdv = bdv;
        this.fontName = (fontFace != null && !fontFace.isEmpty() && !fontFace.trim().isEmpty()) ? fontFace : "Default";
        bdv.getBdvHandle().getViewerPanel().getDisplay().addMouseMotionListener(this);
        this.fontSize = fontSize;
        values = new ArrayList<>();
        colors = new ArrayList<>();
    }

    private void setValuesAndColors(ArrayList<Double> values, ArrayList<ARGBType> colors) {
        this.values = values;
        this.colors = colors;
    }

    @Override
    protected void draw(final Graphics2D g) {
        int[] stringPosition = new int[]{(int) g.getClipBounds().getWidth() - 160, 20 + fontSize};//Handcrafted

        for (int i = 0; i < values.size(); ++i) {
            try
            {
                final int colorIndex = colors.get( i ).get();
                g.setColor( new Color( ARGBType.red( colorIndex ), ARGBType.green( colorIndex ), ARGBType.blue( colorIndex ) ) );
                g.setFont( new Font( this.fontName, Font.PLAIN, fontSize ) );
                g.drawString( "Value: " + values.get( i ), stringPosition[ 0 ], stringPosition[ 1 ] + fontSize * i + 5 );
            }
            catch ( Exception e )
            {
                // TODO: it sometimes happens that the color arraylist does not have the same size as the values
                // this is probably due to an error in the code within "mouseMoved"
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public synchronized void mouseMoved(MouseEvent e) {

        final RealPoint realPoint = new RealPoint(3);

        bdv.getBdvHandle().getViewerPanel().getGlobalMouseCoordinates(realPoint);

        final int currentTimePoint =
                bdv.getBdvHandle().getViewerPanel().getState().getCurrentTimepoint();

        final Map<Integer, Double> pixelValuesOfActiveSources =
                BdvUtils.getPixelValuesOfActiveSources(bdv, realPoint, currentTimePoint);

        ArrayList<Double> values = new ArrayList<>();
        ArrayList<ARGBType> colors = new ArrayList<>();

        final List< ConverterSetup > converterSetups
                = bdv.getBdvHandle().getSetupAssignments().getConverterSetups();

        final ArrayList< Integer > keys = new ArrayList<>( pixelValuesOfActiveSources.keySet() );

        for ( int i = 0; i < keys.size(); i++ )
            values.add( pixelValuesOfActiveSources.get( keys.get( i ) ) ) ;

        for ( int i = 0; i < converterSetups.size(); i++ )
        {
            final ConverterSetup converterSetup = converterSetups.get( i );

            if ( converterSetup instanceof PlaceHolderConverterSetup ) continue;

            final ARGBType color = converterSetup.getColor();
            final int colorIndex = color.get();
            if (colorIndex == 0) {
                colors.add(new ARGBType(ARGBType.rgba(255, 255, 255, 255)));
            }else{
                colors.add(color);
            }
        }

        setValuesAndColors(values, colors);
    }
}
