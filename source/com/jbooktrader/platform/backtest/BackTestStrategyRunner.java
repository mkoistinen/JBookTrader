package com.jbooktrader.platform.backtest;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.report.Report;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.util.MessageDialog;

import java.io.IOException;

/**
 * Runs a trading strategy in the optimizer mode using a data file containing
 * historical market depth.
 */
public class BackTestStrategyRunner implements Runnable {
    private final BackTestDialog backTestDialog;
    private boolean cancelled;
    private final Strategy strategy;
    private BackTestFileReader backTestFileReader;

    public BackTestStrategyRunner(BackTestDialog backTestDialog, Strategy strategy) throws IOException, JBookTraderException {
        this.backTestDialog = backTestDialog;
        this.strategy = strategy;

        boolean isOptimizationMode = (Dispatcher.getMode() == Dispatcher.Mode.OPTIMIZATION);

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
            backTestFileReader = new BackTestFileReader(backTestDialog.getFileName());
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
