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
        List<OptimizationResult> optimizationResults = new LinkedList<OptimizationResult>();
        int strategiesPerProcessor = PreferencesHolder.getInstance().getInt(JBTPreferences.StrategiesPerProcessor);

        while (!tasks.isEmpty()) {
            MarketBook marketBook = new MarketBook();
            strategies.clear();
            while (strategies.size() < strategiesPerProcessor && !tasks.isEmpty()) {
                StrategyParams params = tasks.poll();
                if (params != null) {
                    Strategy strategy = optimizerRunner.getStrategyInstance(params);
                    strategy.setMarketBook(marketBook);
                    strategies.add(strategy);
                }
            }


            if (!strategies.isEmpty()) {
                TradingSchedule tradingSchedule = strategies.get(0).getTradingSchedule();
                int size = strategies.size();

                List<MarketSnapshot> snapshots = optimizerRunner.getSnapshots();
                for (MarketSnapshot marketSnapshot : snapshots) {
                    if (marketBook.isGapping(marketSnapshot)) {
                        // For efficiency, avoid the (Strategy strategy : strategies) construct
                        for (int index = 0; index < size; index++) {
                            strategies.get(index).closePosition();
                        }
                    }


                    marketBook.setSnapshot(marketSnapshot);
                    boolean isInSchedule = tradingSchedule.contains(marketSnapshot.getTime());

                    // For efficiency, avoid the (Strategy strategy : strategies) construct
                    for (int index = 0; index < size; index++) {
                        strategies.get(index).processInstant(isInSchedule);
                    }

                    optimizerRunner.iterationsCompleted(size);
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
