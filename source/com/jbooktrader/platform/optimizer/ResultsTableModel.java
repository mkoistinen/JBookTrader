package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.model.TableDataModel;
import com.jbooktrader.platform.strategy.Strategy;

import java.util.*;

/**
 * Optimization results table model
 * todo: needs refactoring, as it's too whacky
 */
public class ResultsTableModel extends TableDataModel {
    // inner class to represent table schema
    public enum Column {
        PL("P&L", Double.class),
        MaxDD("Max DD", Double.class),
        Trades("Trades", Integer.class),
        PF("Profit Factor", Double.class);

        private final String columnName;
        //private final Class<?> columnClass;

        Column(String columnName, Class<?> columnClass) {
            this.columnName = columnName;
            //this.columnClass = columnClass;
        }

        public String getColumnName() {
            return columnName;
        }
    }

    public ResultsTableModel() {
        Column[] columns = Column.values();
        ArrayList<String> allColumns = new ArrayList<String>();
        for (Column column : columns) {
            allColumns.add(column.columnName);
        }

        setSchema(allColumns.toArray(new String[columns.length]));
    }

    public void updateSchema(Strategy strategy) {
        List<String> paramNames = new ArrayList<String>();
        for (StrategyParam param : strategy.getParams().getAll()) {
            paramNames.add(param.getName());
        }

        for (Column column : Column.values()) {
            paramNames.add(column.getColumnName());
        }

        setSchema(paramNames.toArray(new String[paramNames.size()]));
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

            int size = params.size();
            item[Column.PL.ordinal() + size] = result.getTotalProfit();
            item[Column.MaxDD.ordinal() + size] = result.getMaxDrawdown();
            item[Column.Trades.ordinal() + size] = result.getTrades();
            item[Column.PF.ordinal() + size] = result.getProfitFactor();

            addRow(item);
        }
    }
}
