package com.jbooktrader.platform.backtest;


import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;
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
    private final BackTestProgressIndicator backTestProgressIndicator;

    public BackTester(Strategy strategy, BackTestFileReader backTestFileReader, BackTestProgressIndicator backTestProgressIndicator) {
        this.strategy = strategy;
        this.backTestFileReader = backTestFileReader;
        this.backTestProgressIndicator = backTestProgressIndicator;
    }

    public void execute() {
        MarketBook marketBook = strategy.getMarketBook();
        PositionManager positionManager = strategy.getPositionManager();
        IndicatorManager indicatorManager = strategy.getIndicatorManager();
        TradingSchedule tradingSchedule = strategy.getTradingSchedule();
        strategy.getPerformanceManager().setTradingDays(backTestFileReader.getTradingDays());

        long marketDepthCounter = 0;
        LinkedList<MarketSnapshot> marketSnapshots = backTestFileReader.getAll();
        int size = marketSnapshots.size();

        for (MarketSnapshot marketSnapshot : marketSnapshots) {
            marketDepthCounter++;
            marketBook.add(marketSnapshot);
            long instant = marketBook.getLastMarketSnapshot().getTime();
            strategy.setTime(instant);
            indicatorManager.updateIndicators();

            if (tradingSchedule.contains(instant)) {
                if (strategy.getIndicatorManager().hasValidIndicators()) {
                    strategy.onBookChange();
                }
            } else {
                strategy.closePosition();// force flat position
            }

            positionManager.trade();
            if (marketDepthCounter % 10000 == 0) {
                backTestProgressIndicator.setProgress(marketDepthCounter, size, "Running back test");
            }
        }

        // go flat at the end of the test period to finalize the run
        strategy.closePosition();
        positionManager.trade();
        strategy.setIsActive(false);
        Dispatcher.fireModelChanged(ModelListener.Event.StrategyUpdate, strategy);
    }
}
