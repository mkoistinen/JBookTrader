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
 */
public class OptimizerWorker implements Callable<List<OptimizationResult>> {
	private static final long GAP_SIZE = 60 * 60 * 1000;// 1 hour
    private final OptimizerRunner optimizerRunner;
    private final Queue<StrategyParams> tasks;
    private long lastInstant;

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


            if (strategies.size() != 0) {
                MarketBook marketBook = new MarketBook();
                TradingSchedule tradingSchedule = strategies.get(0).getTradingSchedule();

                for (Strategy strategy : strategies) {
                    strategy.setMarketBook(marketBook);
                }

                List<MarketSnapshot> snapshots = optimizerRunner.getSnapshots();
                for (MarketSnapshot marketSnapshot : snapshots) {
                    marketBook.setSnapshot(marketSnapshot);
                    long time = marketSnapshot.getTime();
                    boolean inSchedule = tradingSchedule.contains(time);

                    for (Strategy strategy : strategies) {
                        strategy.setTime(time);
                        IndicatorManager indicatorManager = strategy.getIndicatorManager();
                        indicatorManager.updateIndicators();
                        if (inSchedule) {
                        	if (time - lastInstant > GAP_SIZE) strategy.reset();
                        	lastInstant = time;

                            if (indicatorManager.hasValidIndicators()) {
                                strategy.onBookChange();
                            }
                        } else {
                            strategy.closePosition();// force flat position
                        }

                        strategy.getPositionManager().trade();

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
                    strategy.getPositionManager().trade();


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
