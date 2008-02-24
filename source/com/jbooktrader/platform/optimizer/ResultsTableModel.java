package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.model.TableDataModel;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.util.NumberRenderer;

import javax.swing.table.TableCellRenderer;
import java.util.*;

/**
 * Optimization results table model
 */
public class ResultsTableModel extends TableDataModel {
    private static final NumberRenderer nr2 = new NumberRenderer(2);
    private static final NumberRenderer nr0 = new NumberRenderer(0);

    // inner class to represent table schema
    public enum Column {
        PL("P&L", nr2),
        MaxDD("Max DD", nr2),
        Trades("Trades", nr2),
        PF("Profit Factor", nr2),
        KellyCriterion("Kelly Criterion", nr0);

        private final String name;
        private final TableCellRenderer renderer;

        Column(String name, TableCellRenderer renderer) {
            this.name = name;
            this.renderer = renderer;
        }

        public String getName() {
            return name;
        }

        public TableCellRenderer getRenderer() {
            return renderer;
        }
    }

    public ResultsTableModel(Strategy strategy) {
        List<String> columnNames = new ArrayList<String>();
        for (StrategyParam param : strategy.getParams().getAll()) {
            columnNames.add(param.getName());
        }

        for (Column column : Column.values()) {
            columnNames.add(column.getName());
        }

        setSchema(columnNames.toArray(new String[columnNames.size()]));
    }


    @Override
    public Class<?> getColumnClass(int c) {
        return Double.class;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    @Override
    public synchronized Object getValueAt(int row, int column) {
        return super.getValueAt(row, column);
    }

    public synchronized void setResults(List<Result> results) {
        removeAllData();

        for (Result result : results) {
            Object[] item = new Object[getColumnCount() + 1];

            StrategyParams params = result.getParams();

            int index = -1;
            for (StrategyParam param : params.getAll()) {
                item[++index] = param.getValue();
            }

            item[++index] = result.getTotalProfit();
            item[++index] = result.getMaxDrawdown();
            item[++index] = result.getTrades();
            item[++index] = result.getProfitFactor();
            item[++index] = result.getKellyCriterion();

            addRow(item);
        }
    }
}
