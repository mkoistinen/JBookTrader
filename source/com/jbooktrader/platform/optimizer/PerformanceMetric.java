package com.jbooktrader.platform.optimizer;

/**
 * @author Eugene Kononov
 */
public enum PerformanceMetric {
    Trades("Trades"), // number of trades
    Duration("Duration"), // average trade duration in minutes
    Bias("Bias"), // short/long bias
    PF("PF"), // profit factor
    PI("PI"), // performance index
    Kelly("Kelly"), // Kelly criterion
    CPI("CPI"), // cumulative performance index
    MaxSL("Max SL"), // maximum single loss
    MaxDD("Max DD"), // maximum drawdown
    NetProfit("Net Profit");

    private final String name;

    PerformanceMetric(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static PerformanceMetric getColumn(String name) {
        for (PerformanceMetric performanceMetric : values()) {
            if (performanceMetric.name.equals(name)) {
                return performanceMetric;
            }
        }
        return null;
    }
}
