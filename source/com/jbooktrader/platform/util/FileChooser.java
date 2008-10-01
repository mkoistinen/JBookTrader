package com.jbooktrader.platform.util;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JTextField;

import com.jbooktrader.platform.startup.JBookTrader;

public class FileChooser {
    
    static public boolean fillInTextField(JTextField textField, String title, String filename) {

        JFileChooser fileChooser = new JFileChooser(JBookTrader.getAppPath());
        fileChooser.setDialogTitle(title);

        if (filename.length() != 0) {
            fileChooser.setSelectedFile(new File(filename));
        }

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            textField.setText(file.getAbsolutePath());
            return true;
        }
        return false;
    }
}
