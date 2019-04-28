package de.embl.cba.bdp2.tracking;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class TrackingProgressBar implements PropertyChangeListener {

    private BigDataTrackerGUI currentInstance;
    private Integer trackId;

    public TrackingProgressBar(BigDataTrackerGUI ui,Integer trackId){
        this.currentInstance = ui;
        this.trackId = trackId;
    }
    class Task extends SwingWorker<Void, Void> {
        @Override
        public Void doInBackground() {
            setProgress(0);
            int progress = 0;
            System.out.println(trackId);
            do {
                setProgress(Math.min(progress, 100));
                if(null != BigDataTracker.progressTracker.get(trackId)){
                    progress = Math.min( BigDataTracker.progressTracker.get(trackId), 100);
                }
            } while (progress < 100);
            setProgress(0); //to make sure past value is not shown anymore for next save using the same window
            return null;
        }

        @Override
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            currentInstance.progressBar.setVisible(false);
            currentInstance.buttons[1].setEnabled(true);
            if (!currentInstance.MESSAGE_TRACK_INTERRUPTED.equals(currentInstance.MESSAGE.getText())) {
                currentInstance.MESSAGE.setText(currentInstance.MESSAGE_TRACK_FINISHED);
            }
            currentInstance.pack();
        }
    }


    public void createGUIandRunMonitor() {
        Task task = new Task();
        task.addPropertyChangeListener(this);
        task.execute();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress".equalsIgnoreCase(evt.getPropertyName())) {
            int progress = (Integer) evt.getNewValue();
            currentInstance.progressBar.setValue(progress);
        }
    }
}
