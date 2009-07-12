package com.jbooktrader.platform.model;

public enum StrategyTableColumn {
    Strategy("Strategy"),
    Symbol("Symbol"),
    MarketDepth("Market Depth"),
    Indicators("Indicators"),
    Price("Price"),
    Position("Position"),
    Trades("Trades"),
    NetProfit("Net Profit"),
    ProfitFactor("Profit Factor"),
    MaxDD("Max DD");

    private final String columnName;

    StrategyTableColumn(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }

}
