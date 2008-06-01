package com.jbooktrader.platform.model;

import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.position.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;

import java.util.*;

/**
 */
public class TradingTableModel extends TableDataModel {

    // inner class to represent table schema
    public enum Column {
        Strategy("Strategy", String.class),
        Symbol("Symbol", String.class),
        Balance("Balance", Integer.class),
        LowPrice("Low Price", Double.class),
        HighPrice("High Price", Double.class),
        Position("Position", Integer.class),
        Trades("Trades", Integer.class),
        MaxDD("Max DD", Double.class),
        PL("P&L", Double.class);

        private final String columnName;
        private final Class<?> columnClass;

        Column(String columnName, Class<?> columnClass) {
            this.columnName = columnName;
            this.columnClass = columnClass;
        }
    }

    private final Map<Integer, Strategy> rows = new HashMap<Integer, Strategy>();

    public TradingTableModel() {
        Column[] columns = Column.values();
        ArrayList<String> allColumns = new ArrayList<String>();
        for (Column column : columns) {
            allColumns.add(column.columnName);
        }
        setSchema(allColumns.toArray(new String[columns.length]));
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }


    @Override
    public Class<?> getColumnClass(int col) {
        Column column = Column.values()[col];
        return column.columnClass;
    }

    public Strategy getStrategyForRow(int row) {
        return rows.get(row);
    }

    public Strategy createStrategyForRow(int row) throws JBookTraderException {
        Strategy strategy = getStrategyForRow(row);
        if (strategy.isActive()) {
            throw new JBookTraderException("Strategy " + strategy + " is already running.");
        }
        strategy = ClassFinder.getInstance(strategy.getClass().getName());
        rows.put(row, strategy);
        update(strategy);
        fireTableRowsUpdated(row, row);
        return strategy;
    }


    private int getRow(Strategy strategy) {
        int row = -1;
        for (Map.Entry<Integer, Strategy> mapEntry : rows.entrySet()) {
            Strategy thisStrategy = mapEntry.getValue();
            if (thisStrategy == strategy) {
                row = mapEntry.getKey();
                break;
            }
        }
        return row;
    }

    public synchronized void update(Strategy strategy) {
        int row = getRow(strategy);
        if (row >= 0) {
            MarketBook marketBook = strategy.getMarketBook();
            if (marketBook.size() > 0) {
                setValueAt(marketBook.getLastMarketDepth().getMidBalance(), row, Column.Balance.ordinal());
                setValueAt(marketBook.getLastMarketDepth().getLowPrice(), row, Column.LowPrice.ordinal());
                setValueAt(marketBook.getLastMarketDepth().getHighPrice(), row, Column.HighPrice.ordinal());
            }
            PositionManager positionManager = strategy.getPositionManager();
            PerformanceManager performanceManager = strategy.getPerformanceManager();
            setValueAt(positionManager.getPosition(), row, Column.Position.ordinal());
            setValueAt(performanceManager.getTrades(), row, Column.Trades.ordinal());
            setValueAt(performanceManager.getMaxDrawdown(), row, Column.MaxDD.ordinal());
            setValueAt(performanceManager.getNetProfit(), row, Column.PL.ordinal());
        }
    }

    public void addStrategy(Strategy strategy) {
        Object[] row = new Object[getColumnCount()];
        row[Column.Strategy.ordinal()] = strategy.getName();
        row[Column.Symbol.ordinal()] = strategy.getContract().m_symbol;
        addRow(row);
        rows.put(getRowCount() - 1, strategy);
    }
}
