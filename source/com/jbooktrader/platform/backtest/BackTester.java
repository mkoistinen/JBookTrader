package com.jbooktrader.platform.backtest;


import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.position.*;
import com.jbooktrader.platform.schedule.*;
import com.jbooktrader.platform.strategy.*;

import java.util.*;

/**
 * This class is responsible for running the strategy against historical market data
 */
public class BackTester {
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
        PerformanceManager performanceManager = strategy.getPerformanceManager();
        TradingSchedule tradingSchedule = strategy.getTradingSchedule();

        long marketDepthCounter = 0;
        LinkedList<MarketDepth> marketDepths = backTestFileReader.getAll();
        int size = marketDepths.size();

        for (MarketDepth marketDepth : marketDepths) {
            marketDepthCounter++;
            marketBook.add(marketDepth);
            performanceManager.update(marketDepth.getMidPrice(), positionManager.getPosition());
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
            if (marketDepthCounter % 1000 == 0) {
                backTestDialog.setProgress(marketDepthCounter, size, "Running back test");
            }
        }

        // go flat at the end of the test period to finalize the run
        strategy.closePosition();
        positionManager.trade();
        strategy.setIsActive(false);
        Dispatcher.fireModelChanged(ModelListener.Event.StrategyUpdate, strategy);
    }

}
