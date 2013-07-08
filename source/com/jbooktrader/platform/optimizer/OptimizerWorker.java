package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.schedule.*;
import com.jbooktrader.platform.strategy.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author Eugene Kononov
 */
public class OptimizerWorker implements Callable<List<OptimizationResult>> {
    private final OptimizerRunner optimizerRunner;
    private final List<StrategyParams> tasks;
    private static final PreferencesHolder pref = PreferencesHolder.getInstance();
    private static final String inclusionCriteria = pref.get(JBTPreferences.InclusionCriteria);

    public OptimizerWorker(OptimizerRunner optimizerRunner, List<StrategyParams> tasks) {
        this.optimizerRunner = optimizerRunner;
        this.tasks = tasks;
    }

    public List<OptimizationResult> call() throws JBookTraderException {
        List<Strategy> strategies = new ArrayList<>();
        List<OptimizationResult> optimizationResults = new LinkedList<>();

        MarketBook marketBook = new MarketBook();
        IndicatorManager indicatorManager = new IndicatorManager();

        for (StrategyParams params : tasks) {
            Strategy strategy = optimizerRunner.getStrategyInstance(params);
            strategy.setMarketBook(marketBook);
            strategy.setIndicatorManager(indicatorManager);
            strategy.setIndicators();
            strategies.add(strategy);
        }

        TradingSchedule tradingSchedule = strategies.get(0).getTradingSchedule();
        int strategiesCount = strategies.size();

        List<MarketSnapshot> snapshots = optimizerRunner.getSnapshots();
        long snapshotsCount = snapshots.size();
        for (int count = 0; count < snapshotsCount; count++) {
            MarketSnapshot marketSnapshot = snapshots.get(count);
            marketBook.setSnapshot(marketSnapshot);
            indicatorManager.updateIndicators();
            boolean isInSchedule = tradingSchedule.contains(marketSnapshot.getTime());
            if (count < snapshotsCount - 1) {
                // ekk-needs optimization
                isInSchedule = isInSchedule && !marketBook.isGapping(snapshots.get(count + 1));
            }

            for (Strategy strategy : strategies) {
                strategy.processInstant(isInSchedule);
            }

            if (count % 5000 == 0) {
                if (optimizerRunner.isCancelled()) {
                    break;
                }
                optimizerRunner.iterationsCompleted(strategiesCount * 5000);
            }
        }


        if (!optimizerRunner.isCancelled()) {
            int minTrades = optimizerRunner.getMinTrades();

            for (Strategy strategy : strategies) {
                strategy.closePosition();

                PerformanceManager performanceManager = strategy.getPerformanceManager();
                if (performanceManager.getTrades() >= minTrades) {
                    if (inclusionCriteria.equals("All strategies") || performanceManager.getNetProfit() > 0) {
                        OptimizationResult optimizationResult = new OptimizationResult(strategy.getParams(), performanceManager);
                        optimizationResults.add(optimizationResult);
                    }
                }
            }
        }

        return optimizationResults;
    }
}
