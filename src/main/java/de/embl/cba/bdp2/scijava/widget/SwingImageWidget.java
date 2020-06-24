package de.embl.cba.bdp2.scijava.widget;

import de.embl.cba.bdp2.image.Image;
import de.embl.cba.bdp2.service.ImageService;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.ui.swing.widget.SwingInputWidget;
import org.scijava.widget.InputWidget;
import org.scijava.widget.WidgetModel;

import javax.swing.*;
import java.util.Collection;

@Plugin(type = InputWidget.class, priority = Priority.EXTREMELY_HIGH)
public class SwingImageWidget extends SwingInputWidget< Image > implements
        ImageWidget< JPanel > {

    private JComboBox< Image > comboBox;

    @Override
    protected void doRefresh() {
    }

    @Override
    public boolean supports(final WidgetModel model) {
        return super.supports(model) && model.isType( Image.class);
    }

    @Override
    public Image getValue() {
        return (Image) comboBox.getSelectedItem();
    }

    @Override
    public void set(final WidgetModel model) {
        super.set(model);

        comboBox = new JComboBox<>();

        final Collection< Image > images = ImageService.imageNameToImage.values();
        for ( Image image : images )
        {
            comboBox.addItem( image );
        }

        comboBox.addItemListener((e)-> model.setValue(getValue()));

        getComponent().add( comboBox );
    }
}
