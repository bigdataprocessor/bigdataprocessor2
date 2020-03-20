package de.embl.cba.bdp2.scijava.widget;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.scijava.RegExpAndHelp;
import de.embl.cba.bdp2.scijava.StringAndHelp;
import de.embl.cba.bdp2.service.ImageService;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.ui.swing.widget.SwingInputWidget;
import org.scijava.widget.InputWidget;
import org.scijava.widget.WidgetModel;

import javax.swing.*;
import java.util.Collection;

@Plugin(type = InputWidget.class, priority = Priority.EXTREMELY_HIGH)
public class SwingStringAndHelpWidget< T extends StringAndHelp > extends SwingInputWidget< T > implements StringAndHelpWidget< T, JPanel > {

    private JTextField textField;
    private JButton helpButton;

    @Override
    protected void doRefresh() { }

    @Override
    public boolean supports(final WidgetModel model) {
        return super.supports(model) && model.isType( StringAndHelp.class );
    }

    @Override
    public T getValue() {
        // TODO: How to get an instance of the object T
        return null;
    }

    @Override
    public void set(final WidgetModel model) {
        super.set(model);

        // TODO: How to get an instance of the object T to populate the textField and button
        textField = new JTextField("input");
        helpButton = new JButton( "?" );

        textField.addPropertyChangeListener( (e) -> model.setValue(getValue()));

        getComponent().add( helpButton );
        getComponent().add( textField );
    }
}
