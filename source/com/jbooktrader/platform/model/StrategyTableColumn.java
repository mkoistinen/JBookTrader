package com.jbooktrader.platform.model;

/**
 * @author Eugene Kononov
 */
public enum StrategyTableColumn {
    Strategy("Strategy"),
    Symbol("Symbol"),
    DepthBalance("Book"),
    Price("Price"),
    Position("Position"),
    Trades("Trades"),
    AveDuration("Duration"),
    Bias("Bias"),
    ProfitFactor("PF"),
    PI("PI"),
    Kelly("Kelly"),
    CPI("CPI"),
    MaxDD("Max DD"),
    NetProfit("Net Profit");

    private final String columnName;

    StrategyTableColumn(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }

}
