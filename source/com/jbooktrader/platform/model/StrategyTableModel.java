package com.jbooktrader.platform.model;

import com.jbooktrader.platform.marketdepth.*;
import static com.jbooktrader.platform.model.StrategyTableColumn.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.position.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.trader.*;
import com.jbooktrader.platform.util.*;

import java.util.*;

/**
 */
public class StrategyTableModel extends TableDataModel {
    private final TraderAssistant traderAssistant;

    public StrategyTableModel() {
        StrategyTableColumn[] columns = StrategyTableColumn.values();
        ArrayList<String> allColumns = new ArrayList<String>();
        for (StrategyTableColumn column : columns) {
            allColumns.add(column.getColumnName());
        }
        setSchema(allColumns.toArray(new String[columns.length]));
        traderAssistant = Dispatcher.getTrader().getAssistant();
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    @Override
    public Class<?> getColumnClass(int col) {
        StrategyTableColumn column = StrategyTableColumn.values()[col];
        return column.getColumnClass();
    }

    public String getStrategyNameForRow(int row) {
        return (String) getRow(row)[Strategy.ordinal()];
    }

    public Strategy getStrategyForRow(int row) {
        String name = getStrategyNameForRow(row);
        return traderAssistant.getStrategy(name);
    }

    public Strategy createStrategyForRow(int row) throws JBookTraderException {
        Strategy strategy = getStrategyForRow(row);
        if (strategy != null && strategy.isActive()) {
            throw new JBookTraderException("Strategy " + strategy + " is already running.");
        }
        String strategyName = getStrategyNameForRow(row);
        traderAssistant.removeStrategy(strategyName);
        strategy = ClassFinder.getInstance(strategyName);
        update(strategy);
        fireTableRowsUpdated(row, row);
        return strategy;
    }

    private int getRow(Strategy strategy) {
        int selectedRow = -1;
        int rowCount = getRowCount();
        for (int row = 0; row < rowCount; row++) {
            String name = getStrategyNameForRow(row);
            if (name.equals(strategy.getName())) {
                selectedRow = row;
            }
        }
        return selectedRow;
    }

    public synchronized void update(Strategy strategy) {
        int row = getRow(strategy);
        if (row >= 0) {
            MarketBook marketBook = strategy.getMarketBook();
            if (marketBook.size() > 0) {
                MarketDepth lastMarketDepth = marketBook.getLastMarketDepth();
                setValueAt(lastMarketDepth.getMidBalance(), row, Balance.ordinal());
                setValueAt(lastMarketDepth.getLowPrice(), row, LowPrice.ordinal());
                setValueAt(lastMarketDepth.getHighPrice(), row, HighPrice.ordinal());
            }
            PositionManager positionManager = strategy.getPositionManager();
            PerformanceManager performanceManager = strategy.getPerformanceManager();
            setValueAt(positionManager.getPosition(), row, Position.ordinal());
            setValueAt(performanceManager.getTrades(), row, Trades.ordinal());
            setValueAt(performanceManager.getMaxDrawdown(), row, MaxDD.ordinal());
            setValueAt(performanceManager.getNetProfit(), row, NetProfit.ordinal());
        }
    }

    public void addStrategy(Strategy strategy) {
        Object[] row = new Object[getColumnCount()];
        row[Strategy.ordinal()] = strategy.getName();
        row[Symbol.ordinal()] = strategy.getContract().m_symbol;
        addRow(row);
    }
}
