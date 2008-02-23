package com.jbooktrader.platform.startup;

import com.birosoft.liquid.LiquidLookAndFeel;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.util.MessageDialog;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.io.*;
import java.nio.channels.FileChannel;


/**
 *
 * Application starter.
 */
public class JBookTrader {
    public static final String APP_NAME = "JBookTrader";
    private static final String LOOK_AND_FEEL = "com.birosoft.liquid.LiquidLookAndFeel";
    private static String appPath;

    /**
     * Instantiates the necessary parts of the application: the application model,
     * views, and controller.
     */
    private JBookTrader() throws JBookTraderException, IOException {
        try {
            LiquidLookAndFeel.setLiquidDecorations(true, "mac");
            UIManager.setLookAndFeel(LOOK_AND_FEEL);
        } catch (Throwable t) {
            String msg = t.getMessage() + ": Unable to set custom look & feel. The default L&F will be used.";
            MessageDialog.showMessage(null, msg);
        }

        // Set the color scheme explicitly
        ColorUIResource color = new ColorUIResource(102, 102, 153);
        UIManager.put("Label.foreground", color);
        UIManager.put("TitledBorder.titleColor", color);
        Dispatcher.setReporter("EventReport");

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
                MessageDialog.showMessage(null, APP_NAME + " is already running.");
                return;
            }

            if (args.length != 1) {
                String msg = "Exactly one argument must be passed. Usage: JBookTrader <JBookTraderDirectory>";
                throw new JBookTraderException(msg);
            }
            appPath = args[0];
            new JBookTrader();
        } catch (Throwable t) {
            MessageDialog.showError(null, t.getMessage());
            Dispatcher.getReporter().report(t);
        }
    }

    public static String getAppPath() {
        return appPath;
    }

}
