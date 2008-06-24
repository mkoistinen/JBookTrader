package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.performance.*;

/**
 * Optimization result.
 */
public class OptimizationResult {
    private final double netProfit, maxDrawdown, profitFactor, performanceIndex, trueKelly;
    private final int trades;
    private final StrategyParams params;

    public OptimizationResult(StrategyParams params, PerformanceManager performanceManager) {
        this.params = params;
        this.netProfit = performanceManager.getNetProfit();
        this.maxDrawdown = performanceManager.getMaxDrawdown();
        this.trades = performanceManager.getTrades();
        this.profitFactor = performanceManager.getProfitFactor();
        this.trueKelly = performanceManager.getTrueKelly();
        this.performanceIndex = performanceManager.getPerformanceIndex();
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

    public double getPerformanceIndex() {
        return performanceIndex;
    }


    public double getTrueKelly() {
        return trueKelly;
    }

}
