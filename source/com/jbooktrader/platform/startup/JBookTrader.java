package com.jbooktrader.platform.startup;

import com.birosoft.liquid.*;
import com.jbooktrader.platform.backtest.CommandLineBackTester;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.report.ReportFactoryConsole;
import com.jbooktrader.platform.report.ReportFactoryFile;
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
    public static final String VERSION = "5.08";
    public static final String RELEASE_DATE = "September 12, 2008";
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
     * @param args The first parameter is the JBT installation directory. You can optionnally pass 3 additionnals parameters.
     *             "--backtest StrategyName DataFileName" to run a backtest from the command line
     *             "--optimize StrategyName DataFileName" to run the optimizer from the command line
     */
    public static void main(String[] args) {
        try {
            File file = new File(System.getProperty("user.home"), APP_NAME + ".tmp");
            FileChannel channel = new RandomAccessFile(file, "rw").getChannel();

            if (channel.tryLock() == null) {
                MessageDialog.showMessage(null, APP_NAME + " is already running.");
                return;
            }

            if(args.length >=1 ) {
                JBookTrader.appPath = args[0];
            }
            
            if(args.length == 1) {
                Dispatcher.setReportFactory(new ReportFactoryFile());
                Dispatcher.setReporter("EventReport");
            	new JBookTrader();	
            }
            else if(args.length == 4 && ( args[1].equals("--backtest") || args.equals("--optimize") ) ){
            	if(args[1].equals("--backtest")) {
                    Dispatcher.setReportFactory(new ReportFactoryConsole());
                    Dispatcher.setReporter("EventReport");
            		new CommandLineBackTester(args[2], args[3]);            		
            	}
            	else { // optimize
            		
            	}
            }
            else if (args.length != 1) {
                throw new JBookTraderException("Usage: JBookTrader <JBookTraderDirectory> [--backtest|--optimize StrategyName DataFileName]");
            }

        } catch (Throwable t) {
            MessageDialog.showError(null, t.getMessage());
            Dispatcher.getReporter().report(t);
        }
    }

    public static String getAppPath() {
        return JBookTrader.appPath;
    }

}

/* $Id$ */
