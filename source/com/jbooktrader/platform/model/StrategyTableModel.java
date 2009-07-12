package com.jbooktrader.platform.model;

import com.ib.client.*;
import com.jbooktrader.platform.marketbook.*;
import static com.jbooktrader.platform.model.StrategyTableColumn.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.trader.*;
import com.jbooktrader.platform.util.*;

import javax.swing.*;
import java.text.*;
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
        strategy = ClassFinder.getInstance(strategyName);
        update(strategy);
        fireTableRowsUpdated(row, row);
        return strategy;
    }

    private int getRowForStrategy(Strategy strategy) {
        int selectedRow = -1;
        int rowCount = getRowCount();
        for (int row = 0; row < rowCount; row++) {
            String name = getStrategyNameForRow(row);
            if (name.equals(strategy.getName())) {
                selectedRow = row;
                break;
            }
        }
        return selectedRow;
    }

    public void update(Strategy strategy) {
        DecimalFormat df0 = NumberFormatterFactory.getNumberFormatter(0);
        DecimalFormat df2 = NumberFormatterFactory.getNumberFormatter(2);

        final int row = getRowForStrategy(strategy);

        MarketBook marketBook = strategy.getMarketBook();
        if (!marketBook.isEmpty()) {
            MarketSnapshot lastMarketSnapshot = marketBook.getSnapshot();
            setValueAtFast(lastMarketSnapshot.getPrice(), row, Price.ordinal());
            setValueAtFast(marketBook.getMarketDepth().getMarketDepthAsString(), row, MarketDepth.ordinal());
        }

        setValueAtFast(strategy.indicatorsState(), row, Indicators.ordinal());
        setValueAtFast(strategy.getPositionManager().getPosition(), row, Position.ordinal());

        PerformanceManager performanceManager = strategy.getPerformanceManager();
        setValueAtFast(performanceManager.getTrades(), row, Trades.ordinal());
        setValueAtFast(df0.format(performanceManager.getMaxDrawdown()), row, MaxDD.ordinal());
        setValueAtFast(df0.format(performanceManager.getNetProfit()), row, NetProfit.ordinal());
        setValueAtFast(df2.format(performanceManager.getProfitFactor()), row, ProfitFactor.ordinal());

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                fireTableRowsUpdated(row, row);
            }
        });
    }

    public void addStrategy(Strategy strategy) {
        Object[] row = new Object[getColumnCount()];
        row[Strategy.ordinal()] = strategy.getName();
        Contract contract = strategy.getContract();
        String symbol = contract.m_symbol;
        if (contract.m_currency != null) {
            symbol += "." + contract.m_currency;
        }
        row[Symbol.ordinal()] = symbol;
        addRow(row);
    }
}
