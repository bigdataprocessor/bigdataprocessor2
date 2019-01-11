package de.embl.cba.bigDataTools2.dataStreamingGUI;


import javax.swing.SwingWorker;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class ProgressBar implements PropertyChangeListener {

    public static int progress; //This variable is written from MonitorThreadPoolStatus class.
    private final SaveMenuDialog currentInstance;

    public ProgressBar(SaveMenuDialog currentInstance) {
        this.currentInstance = currentInstance;
    }

    class Task extends SwingWorker<Void, Void> {

        @Override
        public Void doInBackground() {
            setProgress(0);
            do {
                setProgress(Math.min(progress, 100));
            } while (progress < 100);

            return null;
        }

        @Override
        public void done() {
            Toolkit.getDefaultToolkit().beep();
            currentInstance.progressBar.setVisible(false);
            currentInstance.save.setEnabled(true);
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
