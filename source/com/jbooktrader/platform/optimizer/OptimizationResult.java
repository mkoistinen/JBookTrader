package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.performance.*;

/**
 * Optimization result.
 */
public class OptimizationResult {
    private final double netProfit, maxDrawdown, profitFactor, performanceIndex, kellyCriterion, aveDuration;
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
        aveDuration = performanceManager.getAveDuration();
    }

    public StrategyParams getParams() {
        return params;
    }

    public double get(PerformanceMetric pm) {
        switch (pm) {
            case Trades:
                return trades;
            case PF:
                return profitFactor;
            case PI:
                return performanceIndex;
            case Kelly:
                return kellyCriterion;
            case MaxDD:
                return maxDrawdown;
            case NetProfit:
                return netProfit;
            case AverageDuration:
                return aveDuration;
        }
        throw new RuntimeException("Performance metric " + pm + " is not recognized.");
    }
}
