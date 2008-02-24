package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.position.PositionManager;

/**
 * Optimization results table model.
 */
public class Result {
    private final double totalProfit, maxDrawdown, profitFactor, kellyCriterion;
    private final int trades;
    private final StrategyParams params;

    public Result(StrategyParams params, PositionManager positionManager) {
        this.params = params;
        this.totalProfit = positionManager.getTotalProfitAndLoss();
        this.maxDrawdown = positionManager.getMaxDrawdown();
        this.trades = positionManager.getTrades();
        this.profitFactor = positionManager.getProfitFactor();
        this.kellyCriterion = positionManager.getKellyCriterion();
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
