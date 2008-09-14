package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.util.*;

/**
 * Strategy parameters table model.
 */
public class ParamTableModel extends TableDataModel {
    private static final String[] SCHEMA = {"Parameter", "Min Value", "Max Value", "Step"};

    public ParamTableModel() {
        setSchema(SCHEMA);
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col > 0) {
            try {
                super.setValueAt(Integer.parseInt(value.toString()), row, col);
            } catch (NumberFormatException nfe) {
                MessageDialog.showError(null, value + " is not an integer.");
            }
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // param name column is fixed and cannot be edited
        return (columnIndex != 0);
    }

    public void setParams(StrategyParams strategyParams) {
        removeAllData();

        for (StrategyParam param : strategyParams.getAll()) {
            Object[] row = new Object[getColumnCount() + 1];
            row[0] = param.getName();
            row[1] = param.getMin();
            row[2] = param.getMax();
            row[3] = param.getStep();
            addRow(row);
        }
    }

    public StrategyParams getParams() {
        StrategyParams strategyParams = new StrategyParams();

        int rows = getRowCount();
        for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
            Object[] row = getRow(rowIndex);
            String name = (String) row[0];

            int min = (Integer) row[1];
            int max = (Integer) row[2];
            int step = (Integer) row[3];
            strategyParams.add(name, min, max, step, 0);
        }

        return strategyParams;
    }
}

/* $Id$ */
