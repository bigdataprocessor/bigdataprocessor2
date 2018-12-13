import de.embl.cba.bigDataTools2.dataStreamingGUI.DataStreamingUI;

public class TestOpenUI {

    public static void main(String[] args) {
        //final net.imagej.ImageJ ij = new net.imagej.ImageJ();
        //ij.ui().showUI();
        DataStreamingUI ui = new DataStreamingUI();
        ui.showDialog();
    }
}
