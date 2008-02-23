package com.jbooktrader.platform.optimizer;

/**
 * Optimization results table model.
 */
public class Result {
    private final double totalProfit, maxDrawdown, profitFactor;
    private final int trades;
    private final StrategyParams params;

    public Result(StrategyParams params, double totalProfit, double maxDrawdown, int trades, double profitFactor) {
        this.params = params;
        this.totalProfit = totalProfit;
        this.maxDrawdown = maxDrawdown;
        this.trades = trades;
        this.profitFactor = profitFactor;
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
}
