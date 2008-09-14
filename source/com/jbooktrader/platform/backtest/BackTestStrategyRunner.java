package com.jbooktrader.platform.backtest;

import static com.jbooktrader.platform.model.Dispatcher.Mode.*;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;

/**
 * Runs a trading strategy in the optimizer mode using a data file containing
 * historical market depth.
 */
public class BackTestStrategyRunner implements Runnable {
    private final BackTestProgressIndicator backTestProgressIndicator;
    private final Strategy strategy;
    private final String dataFileName;
    private boolean cancelled;
    private BackTestFileReader backTestFileReader;

    public BackTestStrategyRunner(BackTestProgressIndicator backTestProgressIndicator, Strategy strategy, String dataFileName) {
        this.backTestProgressIndicator = backTestProgressIndicator;
        this.strategy = strategy;
        this.dataFileName = dataFileName;

        boolean isOptimizationMode = (Dispatcher.getMode() == Optimization);
        if (!isOptimizationMode) {
            Dispatcher.getTrader().getAssistant().addStrategy(strategy);
        }
    }

    public void cancel() {
        backTestFileReader.cancel();
        backTestProgressIndicator.showProgress("Stopping back test...");
        cancelled = true;
    }

    public void run() {
        try {
            backTestProgressIndicator.enableProgress();
            backTestFileReader = new BackTestFileReader(dataFileName);
            backTestProgressIndicator.showProgress("Reading historical data file...");
            backTestFileReader.load();
            if (!cancelled) {
                backTestProgressIndicator.showProgress("Running back test...");
                BackTester backTester = new BackTester(strategy, backTestFileReader, backTestProgressIndicator);
                backTester.execute();
            }
        } catch (Throwable t) {
            Dispatcher.getReporter().report(t);
            MessageDialog.showError(null, t.toString());
        } finally {
            backTestProgressIndicator.dispose();
        }
    }
}

/* $Id$ */
