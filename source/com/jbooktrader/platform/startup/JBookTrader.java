package com.jbooktrader.platform.startup;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.util.*;

import javax.swing.*;
import java.io.*;
import java.nio.channels.*;


/**
 * Application starter.
 *
 * @author Eugene Kononov
 */
public class JBookTrader {
    public static final String APP_NAME = "JBookTrader";
    public static final String VERSION = "8.07";
    public static final String RELEASE_DATE = "January 9, 2012";
    private static String appPath;

    /**
     * Instantiates the necessary parts of the application: the application model,
     * views, and controller.
     */
    private JBookTrader() throws JBookTraderException {
        try {
            Dispatcher.getInstance().setReporter();
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            throw new JBookTraderException(e);
        }
        new MainFrameController();
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
            MessageDialog.showException(t);
        }
    }

    public static String getAppPath() {
        return appPath;
    }

}
