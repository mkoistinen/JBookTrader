package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.performance.PerformanceManager;

/**
 * Optimization results table model.
 */
public class Result {
    private final double totalProfit, maxDrawdown, profitFactor, kellyCriterion;
    private final int trades;
    private final StrategyParams params;

    public Result(StrategyParams params, PerformanceManager performanceManager) {
        this.params = params;
        this.totalProfit = performanceManager.getTotalProfitAndLoss();
        this.maxDrawdown = performanceManager.getMaxDrawdown();
        this.trades = performanceManager.getTrades();
        this.profitFactor = performanceManager.getProfitFactor();
        this.kellyCriterion = performanceManager.getKellyCriterion();
    }

    public StrategyParams getParams() {
        return params;
    }

    public double getTotalProfit() {
        return totalProfit;
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

}
