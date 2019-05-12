package de.embl.cba.bdp2.ui;

import javax.swing.*;
import java.io.File;

public class Utils
{
	public static File[] selectDirectories() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int showOpenDialog = fileChooser.showOpenDialog(null);
		if (showOpenDialog != JFileChooser.APPROVE_OPTION) {
			return null;
		}
		File[] dirs = fileChooser.getSelectedFiles();
		return dirs;
	}

}
