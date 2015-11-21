package com.jbooktrader.platform.backtest;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.ui.*;

/**
 * Runs a trading strategy in the back testing mode using a file containing
 * historical market data.
 *
 * @author Eugene Kononov
 */
public class BackTestStrategyRunner implements Runnable {
    private final BackTestDialog backTestDialog;
    private final Strategy strategy;

    public BackTestStrategyRunner(BackTestDialog backTestDialog, Strategy strategy) throws InterruptedException {
        this.backTestDialog = backTestDialog;
        this.strategy = strategy;
        Dispatcher.getInstance().getTrader().getAssistant().addStrategy(strategy);
    }

    public void run() {
        try {
            backTestDialog.enableProgress();
            BackTestFileReader backTestFileReader = new BackTestFileReader(backTestDialog.getFileName(), backTestDialog.getDateFilter());
            BackTester backTester = new BackTester(strategy, backTestFileReader, backTestDialog);
            backTester.execute();
        } catch (Throwable t) {
            MessageDialog.showException(t);
        } finally {
            backTestDialog.dispose();
        }
    }
}
