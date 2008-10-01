package com.jbooktrader.platform.startup;

import com.jbooktrader.platform.backtest.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.model.Dispatcher.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.platform.report.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;


/**
 * Command line application starter.
 */
public class CommandLineStarter {


    public static void showUsage() throws JBookTraderException {

        StringBuilder pmList = new StringBuilder();
        for (PerformanceMetric pm : PerformanceMetric.values()) {
            pmList.append("  '" + pm.getName() + "'\n");
        }

        throw new JBookTraderException("Usage: CommandLineStarter <JBookTraderDirectory>\n" +
                "  * Add the following options to run one or more strategy in forward test mode:\n" +
                "    --forwardtest OneStrategy [AnotherStrategy ...]\n" +
                "  * Add the following options to live trade one or more strategy:\n" +
                "    --trade OneStrategy [AnotherStrategy ...]\n" +
                "  * Add the following options to run a backtest from the command line :\n" +
                "    --backtest StrategyName DataFileName\n" +
                "  * Add the followind options to run the optimizer from the command line:\n" +
                "    --optimize StrategyName DataFileName SortCriteria MinTrades OptimizerMethod\n" +
                "\n" +
                "Examples:\n" +
                "$> JBookTrader /work/jbt\n" +
                "$> CommandLineStarter /work/jbt --forwardtest Strategy1 Strategy2\n" +
                "$> CommandLineStarter /work/jbt --trade Strategy1 Strategy2\n" +
                "$> CommandLineStarter /work/jbt --backtest MyStrategy /marketdata/ES.txt\n" +
                "$> CommandLineStarter /work/jbt --optimize MyStrategy /marketdata/ES.txt 'Profit Factor' 100 bf\n" +
                "\n" +
                "Available SortCriteria are:\n" + pmList.toString() +
                "\n" +
                "Available OptimizerMethod are:\n" +
                "  'bf' : for brute force omptimizer method\n" +
                "  'dnc' : for divide and conquer optimizer method\n");
    }


    private static void runCommandLineTrade(Mode mode, String[] args) throws JBookTraderException {
        Dispatcher.setReportFactory(new ReportFactoryConsole());
        Dispatcher.setReporter("EventReport");
        String[] strategyNames = new String[args.length - 2];
        System.arraycopy(args, 2, strategyNames, 0, args.length - 2);
        new CommandLineStrategyRunner(mode, strategyNames);
    }

    public static void main(String[] args) {
        
        MessageDialog.setConsoleMode(true);
        
        try {
            if(args.length>=1) {
                JBookTrader.setAppPath(args[0]);
            }

            // Launch command line backtester
            if (args.length == 4 && args[1].equals("--backtest")) {
                Dispatcher.setReportFactory(new ReportFactoryConsole());
                Dispatcher.setReporter("EventReport");
                new CommandLineBackTester(args[2], args[3]);
            }
            // Launch command line optimizer
            else if (args.length == 7 && args[1].equals("--optimize")) {
                Dispatcher.setReportFactory(new ReportFactoryConsole());
                Dispatcher.setReporter("EventReport");
                new CommandLineOptimizer(args[2], args[3], args[4], args[5], args[6]);
            }
            // Launch command line forward test
            else if (args.length > 2 && args[1].equals("--forwardtest")) {
                runCommandLineTrade(Mode.ForwardTest, args);
            }
            // Launch command line live trading
            else if (args.length > 2 && args[1].equals("--trade")) {
                runCommandLineTrade(Mode.Trade, args);
            }
            // Show Usage help
            else if (args.length != 1) {
                showUsage();
            }
        } catch (Throwable t) {
            MessageDialog.showError(null, t.getMessage());
            if (Dispatcher.getReporter() != null) {
                Dispatcher.getReporter().report(t);
            }
            System.exit(-1);
        }
    }
}
