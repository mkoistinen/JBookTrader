package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.performance.*;

/**
 * Optimization result.
 *
 * @author Eugene Kononov
 */
public class OptimizationResult {
    private final double netProfit, maxDrawdown, profitFactor, performanceIndex, kellyCriterion, cpi, aveDuration, bias;
    private double stability;
    private final int trades;
    private final StrategyParams params;


    public OptimizationResult(StrategyParams params, PerformanceManager performanceManager) {
        this.params = params;
        netProfit = performanceManager.getNetProfit();
        maxDrawdown = performanceManager.getMaxDrawdown();
        trades = performanceManager.getTrades();
        profitFactor = performanceManager.getProfitFactor();
        kellyCriterion = performanceManager.getKellyCriterion();
        cpi = performanceManager.getCPI();
        performanceIndex = performanceManager.getPerformanceIndex();
        aveDuration = performanceManager.getAveDuration();
        bias = performanceManager.getBias();
    }

    public void setStability(double stability) {
        this.stability = stability;
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
            case CPI:
                return cpi;
            case Stability:
                return stability;
            case MaxDD:
                return maxDrawdown;
            case NetProfit:
                return netProfit;
            case Duration:
                return aveDuration;
            case Bias:
                return bias;
        }
        throw new RuntimeException("Performance metric " + pm + " is not recognized.");
    }
}
