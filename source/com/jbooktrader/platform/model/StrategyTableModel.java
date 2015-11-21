package com.jbooktrader.platform.model;

import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.trader.*;
import com.jbooktrader.platform.util.classfinder.*;
import com.jbooktrader.platform.util.format.*;

import java.text.*;
import java.util.*;

import static com.jbooktrader.platform.model.StrategyTableColumn.*;

/**
 * @author Eugene Kononov
 */
public class StrategyTableModel extends TableDataModel {
    private final TraderAssistant traderAssistant;
    private final DecimalFormat df0, df2, df6;

    public StrategyTableModel() {
        StrategyTableColumn[] columns = StrategyTableColumn.values();
        ArrayList<String> allColumns = new ArrayList<>();
        for (StrategyTableColumn column : columns) {
            allColumns.add(column.getColumnName());
        }
        setSchema(allColumns.toArray(new String[columns.length]));
        traderAssistant = Dispatcher.getInstance().getTrader().getAssistant();
        df0 = NumberFormatterFactory.getNumberFormatter(0);
        df2 = NumberFormatterFactory.getNumberFormatter(2);
        df6 = NumberFormatterFactory.getNumberFormatter(6);
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
        if (strategy != null) {
            throw new JBookTraderException("Strategy " + strategy + " is already running.");
        }
        String strategyName = getStrategyNameForRow(row);
        strategy = ClassFinder.getInstance(strategyName);
        update(strategy);

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

    public void expirationUpdate(Strategy strategy) {
        int rowIndex = getRowForStrategy(strategy);
        EnumMap<StrategyTableColumn, Object> row = new EnumMap<>(StrategyTableColumn.class);
        row.put(Expiration, strategy.getContract().m_expiry);
        updateRow(rowIndex, row);
    }

    public void update(Strategy strategy) {
        int rowIndex = getRowForStrategy(strategy);
        EnumMap<StrategyTableColumn, Object> row = new EnumMap<>(StrategyTableColumn.class);

        MarketBook marketBook = strategy.getMarketBook();
        if (!marketBook.isEmpty()) {
            MarketSnapshot lastMarketSnapshot = marketBook.getSnapshot();
            row.put(Price, df6.format(lastMarketSnapshot.getPrice()));
            row.put(DepthBalance, df0.format(lastMarketSnapshot.getBalance()));
        }

        row.put(Position, strategy.getPositionManager().getCurrentPosition());

        PerformanceManager pm = strategy.getPerformanceManager();
        row.put(Trades, df0.format(pm.getTrades()));
        row.put(AveDuration, df0.format(pm.getAveDuration()));
        row.put(Bias, df0.format(pm.getBias()));
        row.put(ProfitFactor, df2.format(pm.getProfitFactor()));
        row.put(PI, df2.format(pm.getPerformanceIndex()));
        row.put(Kelly, df0.format(pm.getKellyCriterion()));
        row.put(CPI, df0.format(pm.getCPI()));
        row.put(MaxSL, df0.format(pm.getMaxSingleLoss()));
        row.put(MaxDD, df0.format(pm.getMaxDrawdown()));
        row.put(NetProfit, df0.format(pm.getNetProfit()));

        updateRow(rowIndex, row);
    }

    public void addStrategy(Strategy strategy) {
        Object[] row = new Object[getColumnCount()];
        row[Strategy.ordinal()] = strategy.getName();
        String symbol = strategy.getSymbol();
        row[Symbol.ordinal()] = symbol;
        addRow(row);
    }
}
