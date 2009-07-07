package com.jbooktrader.platform.model;

public enum StrategyTableColumn {
    Strategy("Strategy", String.class),
    Symbol("Symbol", String.class),
    MarketDepth("Market Depth", String.class),
    Price("Price", Double.class),
    Indicators("Indicators", String.class),
    Position("Position", Integer.class),
    Trades("Trades", Integer.class),
    NetProfit("Net Profit", Double.class),
    ProfitFactor("Profit Factor", Double.class),
    MaxDD("Max DD", Double.class);

    private final String columnName;
    private final Class<?> columnClass;

    StrategyTableColumn(String columnName, Class<?> columnClass) {
        this.columnName = columnName;
        this.columnClass = columnClass;
    }

    public String getColumnName() {
        return columnName;
    }

    public Class<?> getColumnClass() {
        return columnClass;
    }

}
