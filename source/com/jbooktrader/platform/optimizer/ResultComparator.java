package com.jbooktrader.platform.optimizer;

import java.util.*;

/**
 * Comparator for strategy optimization optimizationResults.
 */
public class ResultComparator implements Comparator<OptimizationResult> {
    private final PerformanceMetric performanceMetric;

    public ResultComparator(PerformanceMetric performanceMetric) {
        this.performanceMetric = performanceMetric;
    }

    public int compare(OptimizationResult r1, OptimizationResult r2) {
        int res = 0;

        switch (performanceMetric) {
            case NetProfit:
                res = ((Double) r2.getNetProfit()).compareTo(r1.getNetProfit());
                break;
            case PF:
                res = ((Double) r2.getProfitFactor()).compareTo(r1.getProfitFactor());
                break;
            case Kelly:
                res = ((Double) r2.getKellyCriterion()).compareTo(r1.getKellyCriterion());
                break;
            case PI:
                res = ((Double) r2.getPerformanceIndex()).compareTo(r1.getPerformanceIndex());
                break;
        }

        return res;
    }
}
