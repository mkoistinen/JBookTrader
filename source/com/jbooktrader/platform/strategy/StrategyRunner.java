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
    private final Strategy strategy;
    protected final Report eventReport;
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
        strategy.setIsActive(true);

        String msg = strategy.getName() + ": strategy started. " + strategy.getTradingSchedule();
        eventReport.report(msg);
    }

    public void execute() throws InterruptedException {
        TradingSchedule tradingSchedule = strategy.getTradingSchedule();

        TraderAssistant traderAssistant = trader.getAssistant();
        traderAssistant.requestMarketDepth(strategy, 5);
        new MarketDepthFactory(strategy);
        MarketBook marketBook = strategy.getMarketBook();

        while (strategy.isActive()) {
            synchronized (marketBook) {
                marketBook.wait();
            }

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
