package com.jbooktrader.platform.optimizer;

public enum PerformanceMetric {
    Trades("Trades", 0),
    Exposure("Exposure", 0),
    NetProfit("Net Profit", 0),
    MaxDD("Max DD", 0),
    PF("Profit Factor", 2),
    Kelly("Kelly", 0),
    PI("PI", 0);

    private final String name;
    private final int precision;

    PerformanceMetric(String name, int precision) {
        this.name = name;
        this.precision = precision;
    }

    public String getName() {
        return name;
    }

    public int getPrecision() {
        return precision;
    }


    static public PerformanceMetric getColumn(String name) {
        for (PerformanceMetric performanceMetric : values()) {
            if (performanceMetric.getName().equals(name)) {
                return performanceMetric;
            }
        }
        return null;
    }
}
