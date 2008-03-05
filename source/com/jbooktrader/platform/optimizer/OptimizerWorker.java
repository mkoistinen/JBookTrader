package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.backtest.BackTester;
import com.jbooktrader.platform.marketdepth.MarketDepth;
import com.jbooktrader.platform.model.Dispatcher;
import com.jbooktrader.platform.performance.PerformanceManager;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.util.MessageDialog;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 */
public class OptimizerWorker implements Runnable {
    private final List<Result> results;
    private final int minTrades;
    private final CountDownLatch remainingTasks;
    private final Constructor<?> strategyConstructor;
    private final LinkedList<StrategyParams> tasks;
    private final List<MarketDepth> marketDepths;

    public OptimizerWorker(List<MarketDepth> marketDepths, Constructor<?> strategyConstructor, LinkedList<StrategyParams> tasks, List<Result> results, int minTrades, CountDownLatch remainingTasks) {
        this.marketDepths = marketDepths;
        this.results = results;
        this.minTrades = minTrades;
        this.remainingTasks = remainingTasks;
        this.strategyConstructor = strategyConstructor;
        this.tasks = tasks;
    }

    public void run() {
        StrategyParams params;

        try {
            while (true) {
                synchronized (tasks) {
                    if (tasks.isEmpty()) {
                        break;
                    }
                    params = tasks.removeFirst();
                }

                Strategy strategy = (Strategy) strategyConstructor.newInstance(params);
                BackTester backTester = new BackTester(strategy, marketDepths);
                backTester.execute();

                PerformanceManager performanceManager = strategy.getPerformanceManager();
                int trades = performanceManager.getTrades();

                if (trades >= minTrades) {
                    Result result = new Result(params, performanceManager);
                    synchronized (results) {
                        results.add(result);
                    }
                }

                synchronized (remainingTasks) {
                    remainingTasks.countDown();
                }

            }
        } catch (Throwable t) {
            Dispatcher.getReporter().report(t);
            MessageDialog.showError(null, t.toString());
        }
    }
}
