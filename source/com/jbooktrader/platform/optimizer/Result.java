package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.performance.PerformanceManager;

/**
 * Optimization results table model.
 */
public class Result {
    private final double netProfit, maxDrawdown, profitFactor, trueKelly;
    private final int trades;
    private final StrategyParams params;

    public Result(StrategyParams params, PerformanceManager performanceManager) {
        this.params = params;
        this.netProfit = performanceManager.getNetProfit();
        this.maxDrawdown = performanceManager.getMaxDrawdown();
        this.trades = performanceManager.getTrades();
        this.profitFactor = performanceManager.getProfitFactor();
        this.trueKelly = performanceManager.getTrueKelly();
    }

    public StrategyParams getParams() {
        return params;
    }

    public double getNetProfit() {
        return netProfit;
    }

    public double getMaxDrawdown() {
        return maxDrawdown;
    }

    public int getTrades() {
        return trades;
    }

    public double getProfitFactor() {
        return profitFactor;
    }

    public double getTrueKelly() {
        return trueKelly;
    }

}
