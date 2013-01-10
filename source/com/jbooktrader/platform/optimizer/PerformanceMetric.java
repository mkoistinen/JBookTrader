package com.jbooktrader.platform.optimizer;

public enum PerformanceMetric {
    Trades("Trades"),
    Duration("Duration"),
    Bias("Bias"),
    PF("PF"),
    PI("PI"),
    Kelly("Kelly"),
    MaxDD("Max DD"),
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
