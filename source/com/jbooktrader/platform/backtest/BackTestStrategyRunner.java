package com.jbooktrader.platform.backtest;

import com.jbooktrader.platform.model.*;
import static com.jbooktrader.platform.model.Dispatcher.Mode.*;
import com.jbooktrader.platform.report.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;

import java.io.*;

/**
 * Runs a trading strategy in the optimizer mode using a data file containing
 * historical market depth.
 */
public class BackTestStrategyRunner implements Runnable {
    private final BackTestDialog backTestDialog;
    private final Strategy strategy;
    private boolean cancelled;
    private BackTestFileReader backTestFileReader;

    public BackTestStrategyRunner(BackTestDialog backTestDialog, Strategy strategy) throws IOException, JBookTraderException {
        this.backTestDialog = backTestDialog;
        this.strategy = strategy;

        boolean isOptimizationMode = (Dispatcher.getMode() == Optimization);
        if (!isOptimizationMode) {
            Dispatcher.getTrader().getAssistant().addStrategy(strategy);
            Dispatcher.getReporter().report(strategy.getName() + ": strategy started");
            Report strategyReport = new Report(strategy.getName());
            strategyReport.report(strategy.getStrategyReportHeaders());
            strategy.setReport(strategyReport);
        }
    }

    public void cancel() {
        backTestFileReader.cancel();
        backTestDialog.showProgress("Stopping back test...");
        cancelled = true;
    }

    public void run() {
        try {
            backTestDialog.enableProgress();
            backTestDialog.showProgress("Reading historical data file...");
            backTestFileReader = new BackTestFileReader(backTestDialog.getFileName());
            backTestFileReader.load();
            if (!cancelled) {
                backTestDialog.showProgress("Running back test...");
                BackTester backTester = new BackTester(strategy, backTestFileReader, backTestDialog);
                backTester.execute();
            }
        } catch (Throwable t) {
            Dispatcher.getReporter().report(t);
            MessageDialog.showError(backTestDialog, t.toString());
        } finally {
            backTestDialog.signalCompleted();
        }
    }
}
