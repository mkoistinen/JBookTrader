package com.jbooktrader.platform.startup;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.util.ui.*;

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
    public static final String VERSION = "9.01";
    public static final String RELEASE_DATE = "July 8, 2013";
    public static final String COPYRIGHT = "Open Source, BSD license";

    /**
     * Instantiates the necessary parts of the application: the application model,
     * views, and controller.
     */
    private JBookTrader(String homeDir) throws JBookTraderException {
        try {
            Dispatcher.getInstance().init(homeDir);
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
                throw new RuntimeException("Exactly one argument must be passed, specifying " + APP_NAME + " home directory.");
            }

            new JBookTrader(args[0]);
        } catch (Throwable t) {
            MessageDialog.showException(t);
        }
    }
}
