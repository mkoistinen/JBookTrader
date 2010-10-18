package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.schedule.*;
import com.jbooktrader.platform.strategy.*;

import java.util.*;
import java.util.concurrent.*;

/**
 */
public class OptimizerWorker implements Callable<List<OptimizationResult>> {
    private final OptimizerRunner optimizerRunner;
    private final Queue<StrategyParams> tasks;

    public OptimizerWorker(OptimizerRunner optimizerRunner, Queue<StrategyParams> tasks) {
        this.optimizerRunner = optimizerRunner;
        this.tasks = tasks;
    }


    public List<OptimizationResult> call() throws JBookTraderException {
        List<Strategy> strategies = new ArrayList<Strategy>();
        List<OptimizationResult> optimizationResults = new ArrayList<OptimizationResult>(strategies.size());
        int strategiesPerProcessor = PreferencesHolder.getInstance().getInt(JBTPreferences.StrategiesPerProcessor);

        while (!tasks.isEmpty()) {
            strategies.clear();
            while (strategies.size() < strategiesPerProcessor && !tasks.isEmpty()) {
                StrategyParams params = tasks.poll();
                if (params != null) {
                    Strategy strategy = optimizerRunner.getStrategyInstance(params);
                    strategies.add(strategy);
                }
            }


            if (!strategies.isEmpty()) {
                MarketBook marketBook = new MarketBook();
                TradingSchedule tradingSchedule = strategies.get(0).getTradingSchedule();

                for (Strategy strategy : strategies) {
                    strategy.setMarketBook(marketBook);
                }

                List<MarketSnapshot> snapshots = optimizerRunner.getSnapshots();
                for (MarketSnapshot marketSnapshot : snapshots) {
                    if (marketBook.isGapping(marketSnapshot)) {
                        for (Strategy strategy : strategies) {
                            strategy.closePosition();
                        }
                    }

                    marketBook.setSnapshot(marketSnapshot);
                    long time = marketSnapshot.getTime();
                    boolean isInSchedule = tradingSchedule.contains(time);

                    for (Strategy strategy : strategies) {
                        strategy.processInstant(isInSchedule);
                    }

                    optimizerRunner.iterationsCompleted(strategies.size());
                    if (optimizerRunner.isCancelled()) {
                        return optimizationResults;
                    }
                }


                optimizationResults.clear();
                int minTrades = optimizerRunner.getMinTrades();


                for (Strategy strategy : strategies) {
                    strategy.closePosition();

                    PerformanceManager performanceManager = strategy.getPerformanceManager();
                    int trades = performanceManager.getTrades();
                    if (trades >= minTrades) {
                        OptimizationResult optimizationResult = new OptimizationResult(strategy.getParams(), performanceManager);
                        optimizationResults.add(optimizationResult);
                    }
                }

                optimizerRunner.addResults(optimizationResults);
            }
        }

        return optimizationResults;
    }
}
