package com.jbooktrader.platform.startup;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.util.*;

import javax.swing.*;
import java.io.*;
import java.nio.channels.*;


/**
 * Application starter.
 */
public class JBookTrader {
    public static final String APP_NAME = "JBookTrader";
    public static final String VERSION = "7.05";
    public static final String RELEASE_DATE = "May 31, 2010";
    private static String appPath;

    /**
     * Instantiates the necessary parts of the application: the application model,
     * views, and controller.
     */
    private JBookTrader() throws JBookTraderException {

        try {
            LookAndFeelManager.setFromPreferences();
        } catch (Throwable t) {
            String msg = t.getMessage() + ": Unable to set custom look & feel. The default L&F will be used.";
            MessageDialog.showMessage(msg);
        }

        Dispatcher.setReporter();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    new MainFrameController();
                } catch (Exception e) {
                    MessageDialog.showError(e);
                }
            }
        });
    }

    /**
     * Starts JBookTrader application.
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            File file = new File(System.getProperty("user.home"), APP_NAME + ".tmp");
            FileChannel channel = new RandomAccessFile(file, "rw").getChannel();

            if (channel.tryLock() == null) {
                MessageDialog.showMessage(APP_NAME + " is already running.");
                return;
            }

            if (args.length != 1) {
                String msg = "Exactly one argument must be passed. Usage: JBookTrader <JBookTraderDirectory>";
                throw new JBookTraderException(msg);
            }
            appPath = args[0];
            new JBookTrader();
        } catch (Throwable t) {
            MessageDialog.showError(t);
        }
    }

    public static String getAppPath() {
        return appPath;
    }

}
