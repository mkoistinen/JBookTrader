package com.jbooktrader.platform.backtest;


import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.position.PositionManager;
import com.jbooktrader.platform.schedule.TradingSchedule;
import com.jbooktrader.platform.strategy.Strategy;

/**
 * This class is responsible for running the strategy against historical market data
 */
public class BackTester {
    private static final long MAX_HISTORY_PERIOD = 24 * 60 * 60 * 1000L; // 24 hours
    private final Strategy strategy;
    private final BackTestFileReader backTestFileReader;
    private final BackTestDialog backTestDialog;

    public BackTester(Strategy strategy, BackTestFileReader backTestFileReader, BackTestDialog backTestDialog) {
        this.strategy = strategy;
        this.backTestFileReader = backTestFileReader;
        this.backTestDialog = backTestDialog;
    }

    public void execute() throws JBookTraderException {
        MarketBook marketBook = strategy.getMarketBook();
        PositionManager positionManager = strategy.getPositionManager();
        TradingSchedule tradingSchedule = strategy.getTradingSchedule();

        long lineCount = 0;
        long totalLines = backTestFileReader.getTotalLineCount();
        backTestFileReader.reset();
        MarketDepth marketDepth;
        while ((marketDepth = backTestFileReader.getNextMarketDepth()) != null) {
            lineCount++;
            marketBook.add(marketDepth);
            long instant = marketBook.getLastMarketDepth().getTime();
            strategy.setTime(instant);
            strategy.updateIndicators();
            if (strategy.hasValidIndicators()) {
                strategy.onBookChange();
            }

            if (!tradingSchedule.contains(instant)) {
                strategy.closePosition();// force flat position
            }

            positionManager.trade();
            strategy.trim(instant - MAX_HISTORY_PERIOD);
            backTestDialog.setProgress(lineCount, totalLines, "Running back test:");
        }

        // go flat at the end of the test period to finalize the run
        strategy.closePosition();
        positionManager.trade();
        strategy.setIsActive(false);
        Dispatcher.fireModelChanged(ModelListener.Event.STRATEGY_UPDATE, strategy);
    }

}
