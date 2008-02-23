package com.jbooktrader.platform.strategy;

import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.position.PositionManager;
import com.jbooktrader.platform.report.Report;
import com.jbooktrader.platform.schedule.TradingSchedule;
import com.jbooktrader.platform.trader.*;

import java.io.IOException;


/**
 * Runs a trading strategy. There is a one-to-one map between the strategy class
 * and the strategy runner. That is, if 5 strategies are selected to run,
 * there will be 5 instances of the StrategyRunner created.
 */

public class StrategyRunner implements Runnable {
    private final Trader trader;
    protected final Strategy strategy;
    protected final Report eventReport;
    protected final PositionManager positionManager;

    public StrategyRunner(Strategy strategy) throws IOException, JBookTraderException {
        this.strategy = strategy;
        eventReport = Dispatcher.getReporter();

        Report strategyReport = new Report(strategy.getName());
        strategyReport.report(strategy.getStrategyReportHeaders());
        strategy.setReport(strategyReport);

        trader = Dispatcher.getTrader();
        trader.getAssistant().addStrategy(strategy);
        positionManager = strategy.getPositionManager();
        strategy.setIActive(true);

        eventReport.report(strategy.getName() + ": strategy started");
    }

    public void execute() throws InterruptedException, JBookTraderException {
        TradingSchedule tradingSchedule = strategy.getTradingSchedule();

        TraderAssistant traderAssistant = trader.getAssistant();
        traderAssistant.requestMarketDepth(strategy, 5);
        MarketBook marketBook = strategy.getMarketBook();
        MarketDepth marketDepth = strategy.getMarketDepth();

        while (strategy.isActive()) {
            synchronized (marketBook) {
                marketBook.wait();
            }

            if (!marketDepth.isValid())
                continue;
            
            while (System.currentTimeMillis() - marketDepth.getTime() < 5) {
                Thread.sleep(5);
            }
            marketDepth.update();
            marketBook.addMarketDepth(marketDepth);


            long instant = marketBook.getLastMarketDepth().getTime();
            strategy.setTime(instant);
            strategy.updateIndicators();
            if (strategy.hasValidIndicators()) {
                strategy.onBookChange();
            }

            boolean canTrade = tradingSchedule.contains(instant);
            if (!canTrade && (positionManager.getPosition() != 0)) {
                canTrade = true;
                strategy.setPosition(0);// force flat position
                String msg = "End of trading interval. Closing current position.";
                eventReport.report(strategy.getName() + ": " + msg);
            }

            if (canTrade && traderAssistant.isConnected()) {
                positionManager.trade();
            }
            Dispatcher.fireModelChanged(ModelListener.Event.STRATEGY_UPDATE, strategy);
        }
    }

    public void run() {
        try {
            Dispatcher.strategyStarted();
            execute();
        } catch (Throwable t) {
            eventReport.report(t);
        } finally {
            eventReport.report(strategy.getName() + ": is inactive.");
            Dispatcher.strategyCompleted();
        }
    }


}
