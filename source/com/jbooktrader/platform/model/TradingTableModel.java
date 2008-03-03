package com.jbooktrader.platform.model;

import com.jbooktrader.platform.marketdepth.MarketDepth;
import com.jbooktrader.platform.optimizer.StrategyParams;
import com.jbooktrader.platform.performance.PerformanceManager;
import com.jbooktrader.platform.position.PositionManager;
import com.jbooktrader.platform.strategy.Strategy;

import java.lang.reflect.Constructor;
import java.util.*;

/**
 */
public class TradingTableModel extends TableDataModel {

    // inner class to represent table schema
    public enum Column {
        Strategy("Strategy", String.class),
        Symbol("Symbol", String.class),
        MarketDepth("Market Depth", String.class),
        Bid("Bid", Double.class),
        Ask("Ask", Double.class),
        Position("Position", Integer.class),
        Trades("Trades", Integer.class),
        PL("P&L", Double.class),
        MaxDD("Max DD", Double.class),
        PF("PF", Double.class),
        TK("TC", Double.class);

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
        try {
            Class<?> clazz = Class.forName(strategy.getClass().getName());
            Constructor<?> ct = clazz.getConstructor(StrategyParams.class);
            strategy = (Strategy) ct.newInstance(new StrategyParams());
            rows.put(row, strategy);
            update(strategy);
            fireTableRowsUpdated(row, row);
        } catch (Exception e) {
            throw new JBookTraderException(e);
        }

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
            MarketDepth marketDepth = strategy.getMarketBook().getLastMarketDepth();
            if (marketDepth != null) {
                setValueAt(marketDepth.toShortString(), row, Column.MarketDepth.ordinal());
                setValueAt(marketDepth.getBestBid(), row, Column.Bid.ordinal());
                setValueAt(marketDepth.getBestAsk(), row, Column.Ask.ordinal());
            }
            PositionManager positionManager = strategy.getPositionManager();
            PerformanceManager performanceManager = strategy.getPerformanceManager();
            setValueAt(positionManager.getPosition(), row, Column.Position.ordinal());
            setValueAt(performanceManager.getTrades(), row, Column.Trades.ordinal());
            setValueAt(performanceManager.getTotalProfitAndLoss(), row, Column.PL.ordinal());
            setValueAt(performanceManager.getMaxDrawdown(), row, Column.MaxDD.ordinal());
            setValueAt(performanceManager.getProfitFactor(), row, Column.PF.ordinal());
            setValueAt(performanceManager.getTrueKelly(), row, Column.TK.ordinal());
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
