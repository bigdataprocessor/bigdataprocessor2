package de.embl.cba.bdp2.track.scijava;

import de.embl.cba.bdp2.track.Track;
import de.embl.cba.bdp2.track.TrackManager;
import org.scijava.Priority;
import org.scijava.object.ObjectService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.swing.widget.SwingInputWidget;
import org.scijava.widget.InputWidget;
import org.scijava.widget.WidgetModel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Plugin(type = InputWidget.class, priority = Priority.EXTREMELY_HIGH)
public class SwingTrackWidget extends SwingInputWidget< Track > implements TrackWidget< JPanel > {

    @Override
    protected void doRefresh() {
    }

    @Override
    public boolean supports( final WidgetModel model ) {
        return super.supports( model ) && model.isType( Track.class);
    }

    @Override
    public Track getValue() {
        final Track selectedItem = ( Track ) comboBox.getSelectedItem();
        return selectedItem;
    }

    JComboBox comboBox;

    @Parameter
	ObjectService os;

    @Override
    public void set( final WidgetModel model ) {
        super.set( model );
        List<Track> list = new ArrayList<>( TrackManager.getTracks().values() );
        Track[] data = list.toArray(new Track[ list.size() ]);
        this.comboBox = new JComboBox( data );
        model.setValue( getValue() ); // initialise model
        this.comboBox.addItemListener( ( e ) -> model.setValue( getValue() ) );
        getComponent().add( comboBox );
    }
}
