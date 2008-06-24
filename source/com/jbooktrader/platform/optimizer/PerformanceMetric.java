package com.jbooktrader.platform.optimizer;

public enum PerformanceMetric {
    PL("P&L"),
    MaxDD("Max DD"),
    Trades("Trades"),
    PF("Profit Factor"),
    TrueKelly("True Kelly"),
    PI("Performance Index");

    private final String name;

    PerformanceMetric(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
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
