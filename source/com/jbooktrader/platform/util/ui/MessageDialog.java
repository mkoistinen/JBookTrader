package com.jbooktrader.platform.util.ui;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.startup.*;

import javax.swing.*;

/**
 * Utility class to display message and error dialogs.
 *
 * @author Eugene Kononov
 */
public class MessageDialog {

    public static void showMessage(String msg) {
        JOptionPane.showMessageDialog(null, msg, JBookTrader.APP_NAME, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(String msg) {
        JOptionPane.showMessageDialog(null, msg, JBookTrader.APP_NAME, JOptionPane.ERROR_MESSAGE);
    }

    public static void showException(Throwable t) {
        showError(t.getMessage());
        Dispatcher.getInstance().getEventReport().report(t);
    }
}
