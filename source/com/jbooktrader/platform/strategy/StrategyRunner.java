package com.jbooktrader.platform.strategy;

import com.jbooktrader.platform.bar.*;
import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.position.*;
import com.jbooktrader.platform.report.*;
import com.jbooktrader.platform.schedule.*;
import com.jbooktrader.platform.trader.*;
import com.jbooktrader.platform.util.*;

import java.io.*;


/**
 * Runs a trading strategy. There is a one-to-one map between the strategy class
 * and the strategy runner. That is, if 5 strategies are selected to run,
 * there will be 5 instances of the StrategyRunner created.
 */

public class StrategyRunner implements Runnable {
    private final Trader trader;
    private final Strategy strategy;
    private final Report eventReport;
    private final PositionManager positionManager;

    public StrategyRunner(Strategy strategy) throws IOException, JBookTraderException {
        this.strategy = strategy;
        eventReport = Dispatcher.getReporter();

        Report strategyReport = new Report(strategy.getName());
        strategyReport.report(strategy.getStrategyReportHeaders());
        strategy.setReport(strategyReport);

        trader = Dispatcher.getTrader();
        trader.getAssistant().addStrategy(strategy);
        positionManager = strategy.getPositionManager();

        String msg = strategy.getName() + ": strategy started. " + strategy.getTradingSchedule();
        eventReport.report(msg);
    }

    private void execute() throws InterruptedException, JBookTraderException {
        TradingSchedule tradingSchedule = strategy.getTradingSchedule();

        TraderAssistant traderAssistant = trader.getAssistant();
        traderAssistant.requestMarketDepth(strategy, 5);
        PerformanceManager performanceManager = strategy.getPerformanceManager();
        MarketBook marketBook = strategy.getMarketBook();
        MarketDepth marketDepth = strategy.getMarketDepth();
        PriceHistory priceHistory = strategy.getPriceBarHistory();
        strategy.setIsActive(true);

        while (strategy.isActive()) {
            synchronized (marketBook) {
                marketBook.wait();// wait until notified about the update
            }

            // This is a little awkward. We are waiting for 100 milliseconds of
            // inactivity, so that when we take a snapshot of the book, we know
            // it's been fully updated.
            while (true) {
                synchronized (marketDepth) {
                    // the getMillisSinceLastUpdate and addMarketDepth must be done atomically
                    if (marketDepth.getMillisSinceLastUpdate() >= 100) {
                        marketBook.add(new MarketDepth(marketDepth));
                        break;
                    }
                }
                Thread.sleep(50);
            }

            if (!marketBook.isEmpty()) {
                MarketDepth lastMarketDepth = marketBook.getLastMarketDepth();
                long instant = lastMarketDepth.getTime();
                priceHistory.update(marketDepth);
                strategy.setTime(instant);
                strategy.updateIndicators();
                if (strategy.hasValidIndicators()) {
                    strategy.onBookChange();
                }

                if (!tradingSchedule.contains(instant)) {
                    strategy.closePosition();// force flat position
                }

                positionManager.trade();
                performanceManager.updateNetProfit(lastMarketDepth.getMidPoint(), positionManager.getPosition());
                Dispatcher.fireModelChanged(ModelListener.Event.StrategyUpdate, strategy);
            }
        }
    }

    public void run() {
        try {
            Dispatcher.strategyStarted();
            execute();
        } catch (Throwable t) {
            eventReport.report(t);
            MessageDialog.showError(null, t.getMessage());
        } finally {
            eventReport.report(strategy.getName() + ": is inactive.");
            Dispatcher.strategyCompleted();
        }
    }


}
