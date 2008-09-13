package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.schedule.*;
import com.jbooktrader.platform.strategy.*;

import java.util.*;
import java.util.concurrent.*;

/**
 */
public class OptimizerWorker implements Callable<List<OptimizationResult>> {
    private final OptimizerRunner optimizerRunner;
    private final List<Strategy> strategies;

    public OptimizerWorker(OptimizerRunner optimizerRunner, List<Strategy> strategies) {
        this.optimizerRunner = optimizerRunner;
        this.strategies = strategies;
    }

    public List<OptimizationResult> call() {
        List<OptimizationResult> optimizationResults = new ArrayList<OptimizationResult>();
        int size = strategies.size();
        MarketBook marketBook = new MarketBook();
        TradingSchedule tradingSchedule = strategies.get(0).getTradingSchedule();

        for (Strategy strategy : strategies) {
            strategy.setMarketBook(marketBook);
        }

        LinkedList<MarketSnapshot> snapshots = optimizerRunner.getBackTestFileReader().getAll();
        for (MarketSnapshot marketSnapshot : snapshots) {
            marketBook.add(marketSnapshot);
            long time = marketSnapshot.getTime();
            boolean inSchedule = tradingSchedule.contains(time);

            for (Strategy strategy : strategies) {
                strategy.setTime(time);
                IndicatorManager indicatorManager = strategy.getIndicatorManager();
                indicatorManager.updateIndicators();
                if (inSchedule) {
                    if (indicatorManager.hasValidIndicators()) {
                        strategy.onBookChange();
                    }
                } else {
                    strategy.closePosition();// force flat position
                }

                strategy.getPositionManager().trade();

            }

            optimizerRunner.iterationsCompleted(size);
            if (optimizerRunner.isCancelled()) {
                break;
            }

        }

        int minTrades = optimizerRunner.getMinTrades();
        int tradingDays = optimizerRunner.getBackTestFileReader().getTradingDays();
        for (Strategy strategy : strategies) {
            strategy.closePosition();
            strategy.getPositionManager().trade();


            PerformanceManager performanceManager = strategy.getPerformanceManager();
            int trades = performanceManager.getTrades();
            if (trades >= minTrades) {
                performanceManager.setTradingDays(tradingDays);
                OptimizationResult optimizationResult = new OptimizationResult(strategy.getParams(), performanceManager);
                optimizationResults.add(optimizationResult);
            }
        }

        return optimizationResults;
    }
}
