package de.embl.cba.bdp2.ui;


import javax.swing.SwingWorker;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class ProgressBar implements PropertyChangeListener {

    private final SaveMenuDialog currentInstance;

    public ProgressBar(SaveMenuDialog currentInstance, long total ) {
        this.currentInstance = currentInstance;
    }

    class Task extends SwingWorker<Void, Void> {
        @Override
        public Void doInBackground() {
            setProgress(0);
            int progress = 0;
            Integer currentSaveId = currentInstance.getSaveId();
            do {
                setProgress(Math.min(progress, 100));
                if(null != BigDataProcessor2.progressTracker.get(currentSaveId)){
                    progress = Math.min( BigDataProcessor2.progressTracker.get(currentSaveId), 100);
                }
            } while (progress < 100);
            setProgress(0); //to make sure past value is not shown anymore for next save using the same window
            return null;
        }

        @Override
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            currentInstance.progressBar.setVisible(false);
            currentInstance.save.setEnabled(true);
            if (!currentInstance.MESSAGE_SAVE_INTERRUPTED.equals(currentInstance.MESSAGE.getText())) {
                currentInstance.MESSAGE.setText(currentInstance.MESSAGE_SAVE_FINISHED);
            }
            currentInstance.pack();
        }

        public void set
    }

    public void setProgress( long current, long total )
    {
        this.setProgress(  );

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
