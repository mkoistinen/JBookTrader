package com.jbooktrader.platform.backtest;


import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.position.PositionManager;
import com.jbooktrader.platform.schedule.TradingSchedule;
import com.jbooktrader.platform.strategy.Strategy;

import java.util.List;

/**
 * This class is responsible for running the strategy against historical market data
 */
public class BackTester {
    private final Strategy strategy;
    private final List<MarketDepth> marketDepths;

    public BackTester(Strategy strategy, List<MarketDepth> marketDepths) {
        this.strategy = strategy;
        this.marketDepths = marketDepths;
    }

    public void execute() {
        MarketBook marketBook = strategy.getMarketBook();
        PositionManager positionManager = strategy.getPositionManager();
        TradingSchedule tradingSchedule = strategy.getTradingSchedule();

        for (MarketDepth marketDepth : marketDepths) {
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
        }

        // go flat at the end of the test period to finalize the run
        strategy.closePosition();
        positionManager.trade();
        strategy.setIsActive(false);
        Dispatcher.fireModelChanged(ModelListener.Event.STRATEGY_UPDATE, strategy);
    }

}
