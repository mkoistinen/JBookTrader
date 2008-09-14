package com.jbooktrader.platform.startup;

import com.birosoft.liquid.*;
import com.jbooktrader.platform.backtest.CommandLineBackTester;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.CommandLineOptimizer;
import com.jbooktrader.platform.optimizer.PerformanceMetric;
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

    
    public static void showUsage() throws JBookTraderException {
        
        StringBuilder pmList = new StringBuilder();
        for(PerformanceMetric pm : PerformanceMetric.values()) {
            pmList.append("  '"+pm.getName()+"'\n");
        }
        
        throw new JBookTraderException("Usage: JBookTrader <JBookTraderDirectory>\n" +
                "  * Add the following options to run a backtest from the command line :\n" +
                "    --backtest StrategyName DataFileName\n" +
                "  * Add the followind options to run the optimizer from the command line:\n" +
                "    --optimize StrategyName DataFileName SortCriteria MinTrades OptimizerMethod\n" +
                "\n" +
                "Examples:\n" +
                "$> JBookTrader /work/jbt\n" +
                "$> JBookTrader /work/jbt --backtest MyStrategy /marketdata/ES.txt\n" +
                "$> JBookTrader /work/jbt --optimize MyStrategy /marketdata/ES.txt 'Profit Factor' 100 bf\n" +
                "\n" +
                "Available SortCriteria are:\n" + pmList.toString() +
                "\n" +
                "Available OptimizerMethod are:\n" +
                "  'bf' : for brute force omptimizer method\n" +
                "  'dnc' : for divide and conquer optimizer method\n" );
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

            if(args.length >=1 ) {
                JBookTrader.appPath = args[0];
            }

            // Launch JBT GUI
            if(args.length == 1) {
                Dispatcher.setReportFactory(new ReportFactoryFile());
                Dispatcher.setReporter("EventReport");
            	new JBookTrader();	
            }
            // Launch command line backtester
            else if(args.length == 4 && args[1].equals("--backtest")){
                Dispatcher.setReportFactory(new ReportFactoryConsole());
                Dispatcher.setReporter("EventReport");
        		new CommandLineBackTester(args[2], args[3]);            		
            }
            // Launch command line optimizer
            else if(args.length == 7 && args[1].equals("--optimize") ){
                Dispatcher.setReportFactory(new ReportFactoryConsole());
                Dispatcher.setReporter("EventReport");
                new CommandLineOptimizer(args[2],args[3],args[4],args[5],args[6]);                
            }
            // Show Usage help
            else if (args.length != 1) {
                showUsage();
            }

        } catch (Throwable t) {
            MessageDialog.showError(null, t.getMessage());
            if(Dispatcher.getReporter()!=null) {
                Dispatcher.getReporter().report(t);
            }
        }
    }

    public static String getAppPath() {
        return JBookTrader.appPath;
    }

}

/* $Id$ */
