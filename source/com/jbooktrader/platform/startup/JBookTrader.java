package com.jbooktrader.platform.startup;

import com.birosoft.liquid.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.report.*;
import com.jbooktrader.platform.util.*;

import javax.swing.*;
import javax.swing.plaf.*;
import java.io.*;
import java.nio.channels.*;


/**
 * Application starter.
 */
public class JBookTrader {
    public static final String APP_NAME = "JBookTrader";
    public static final String VERSION = "5.09";
    public static final String RELEASE_DATE = "September 27, 2008";
    private static String appPath;

    /**
     * Instantiates the necessary parts of the application: the application model,
     * views, and controller.
     */
    private JBookTrader() throws JBookTraderException {
        try {
            LiquidLookAndFeel.setLiquidDecorations(true, "mac");
            UIManager.setLookAndFeel("com.birosoft.liquid.LiquidLookAndFeel");
        } catch (Throwable t) {
            String msg = t.getMessage() + ": Unable to set custom look & feel. The default L&F will be used.";
            MessageDialog.showMessage(null, msg);
        }

        // Set the color scheme explicitly
        ColorUIResource color = new ColorUIResource(102, 102, 153);
        UIManager.put("Label.foreground", color);
        UIManager.put("TitledBorder.titleColor", color);

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
                throw new JBookTraderException(APP_NAME + " is already running.");
            }

            if (args.length >= 1) {
                setAppPath(args[0]);
                Dispatcher.setReportFactory(new ReportFactoryFile());
                Dispatcher.setReporter("EventReport");
                // Launch JBT GUI
                new JBookTrader();
            } else {
                throw new JBookTraderException("You omit to pass the JBT path in command line argument.");
            }
        } catch (Throwable t) {
            MessageDialog.showError(null, t.getMessage());
            if (Dispatcher.getReporter() != null) {
                Dispatcher.getReporter().report(t);
            }
            System.exit(-1);
        }
    }

    public static String getAppPath() {
        return JBookTrader.appPath;
    }

    public static void setAppPath(String appPath) {
        JBookTrader.appPath = appPath;
    }
}
