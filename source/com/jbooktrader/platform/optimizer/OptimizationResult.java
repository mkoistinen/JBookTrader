package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.performance.*;

/**
 * Optimization result.
 */
public class OptimizationResult {
    private final double netProfit, maxDrawdown, profitFactor, performanceIndex, kellyCriterion;
    private final int trades;
    private final StrategyParams params;

    public OptimizationResult(StrategyParams params, PerformanceManager performanceManager) {
        this.params = params;
        netProfit = performanceManager.getNetProfit();
        maxDrawdown = performanceManager.getMaxDrawdown();
        trades = performanceManager.getTrades();
        profitFactor = performanceManager.getProfitFactor();
        kellyCriterion = performanceManager.getKellyCriterion();
        performanceIndex = performanceManager.getPerformanceIndex();
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

    public double getKellyCriterion() {
        return kellyCriterion;
    }

    public double getPerformanceIndex() {
        return performanceIndex;
    }
}
