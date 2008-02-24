package com.jbooktrader.platform.backtest;


import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.position.PositionManager;
import com.jbooktrader.platform.report.Report;
import com.jbooktrader.platform.schedule.TradingSchedule;
import com.jbooktrader.platform.strategy.Strategy;

import java.util.List;

/**
 */
public class BackTester {
    private final Strategy strategy;
    private final List<MarketDepth> marketDepths;

    public BackTester(Strategy strategy, List<MarketDepth> marketDepths) {
        this.strategy = strategy;
        this.marketDepths = marketDepths;
    }

    public void execute() {
        Report eventReport = Dispatcher.getReporter();
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

            boolean canTrade = tradingSchedule.contains(instant);
            if (!canTrade && (positionManager.getPosition() != 0)) {
                canTrade = true;
                strategy.setPosition(0); // force flat position
                String msg = "End of trading interval. Closing current position.";
                eventReport.report(strategy.getName() + ": " + msg);
            }

            if (canTrade) {
                positionManager.trade();
            }
        }

        // go flat at the end of the test period to finalize the run
        strategy.setPosition(0);
        positionManager.trade();
        strategy.setIActive(false);
        Dispatcher.fireModelChanged(ModelListener.Event.STRATEGY_UPDATE, strategy);
    }

}
