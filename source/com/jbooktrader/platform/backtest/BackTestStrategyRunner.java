package com.jbooktrader.platform.backtest;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;

/**
 * Runs a trading strategy in the back testing mode using a file containing
 * historical market data.
 */
public class BackTestStrategyRunner implements Runnable {
    private final BackTestDialog backTestDialog;
    private final Strategy strategy;
    private BackTestFileReader backTestFileReader;
    private BackTester backTester;

    public BackTestStrategyRunner(BackTestDialog backTestDialog, Strategy strategy) {
        this.backTestDialog = backTestDialog;
        this.strategy = strategy;
        Dispatcher.getInstance().getTrader().getAssistant().addStrategy(strategy);
    }

    public void cancel() {
        if (backTester != null) {
            backTester.cancel();
        }
    }

    public void run() {
        try {
            backTestDialog.enableProgress();
            backTestFileReader = new BackTestFileReader(backTestDialog.getFileName(), backTestDialog.getDateFilter());
            backTester = new BackTester(strategy, backTestFileReader, backTestDialog);
            backTester.execute();
        } catch (Throwable t) {
            MessageDialog.showError(t);
        } finally {
            backTestDialog.dispose();
        }
    }
}
