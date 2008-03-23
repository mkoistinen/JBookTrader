package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.performance.PerformanceManager;

/**
 * Optimization result.
 */
public class Result {
    private final double netProfit, maxDrawdown, profitFactor, trueKelly;
    private final int trades;
    private final StrategyParams params;
    private int hashCode;

    public Result(StrategyParams params, PerformanceManager performanceManager) {
        this.params = params;
        this.netProfit = performanceManager.getNetProfit();
        this.maxDrawdown = performanceManager.getMaxDrawdown();
        this.trades = performanceManager.getTrades();
        this.profitFactor = performanceManager.getProfitFactor();
        this.trueKelly = performanceManager.getTrueKelly();

        hashCode = 17;
        for (StrategyParam param : params.getAll()) {
            int value = param.getValue();
            hashCode = 37 * hashCode + value;
        }
    }

    public boolean equals(Object o) {
        return o instanceof Result && hashCode == ((Result) o).hashCode;
    }

    public int hashCode() {
        return hashCode;
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
