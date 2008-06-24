package com.jbooktrader.platform.optimizer;

import java.util.*;

/**
 * Comparator for strategy optimization optimizationResults.
 */
public class ResultComparator implements Comparator<OptimizationResult> {
    private final PerformanceMetric sortPerformanceMetric;

    public ResultComparator(PerformanceMetric sortPerformanceMetric) {
        this.sortPerformanceMetric = sortPerformanceMetric;
    }

    public int compare(OptimizationResult r1, OptimizationResult r2) {
        int res = 0;

        switch (sortPerformanceMetric) {
            case PL:
                // highest to lowest
                res = ((Double) r2.getNetProfit()).compareTo(r1.getNetProfit());
                break;
            case PF:
                // highest to lowest
                res = ((Double) r2.getProfitFactor()).compareTo(r1.getProfitFactor());
                break;
            case MaxDD:
                // lowest to highest
                res = ((Double) r1.getMaxDrawdown()).compareTo(r2.getMaxDrawdown());
                break;
            case Trades:
                // lowest to highest
                res = ((Integer) r1.getTrades()).compareTo(r2.getTrades());
                break;
            case TrueKelly:
                // highest to lowest
                res = ((Double) r2.getTrueKelly()).compareTo(r1.getTrueKelly());
                break;
            case PI:
                // highest to lowest
                res = ((Double) r2.getPerformanceIndex()).compareTo(r1.getPerformanceIndex());
                break;
        }

        return res;
    }
}
