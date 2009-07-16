package com.jbooktrader.platform.backtest;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;

/**
 * Strategy parameters table model for the back test dialog.
 */
public class BackTestParamTableModel extends TableDataModel {
    private static final String[] SCHEMA = {"Parameter", "Value", "Min", "Max"};

    public BackTestParamTableModel() {
        setSchema(SCHEMA);
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return column == 0 ? String.class : Integer.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // only the "value" column is editable
        return (columnIndex == 1);
    }

    public void setParams(StrategyParams strategyParams) {
        removeAllData();

        for (StrategyParam param : strategyParams.getAll()) {
            Object[] row = new Object[getColumnCount() + 1];
            row[0] = param.getName();
            row[1] = param.getValue();
            row[2] = param.getMin();
            row[3] = param.getMax();
            addRow(row);
        }
    }

    public StrategyParams getParams() {
        StrategyParams strategyParams = new StrategyParams();

        int rows = getRowCount();
        for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
            Object[] row = getRow(rowIndex);
            String name = (String) row[0];
            int value = (Integer) row[1];
            strategyParams.add(name, value, value, 1, value);
        }

        return strategyParams;
    }
}
