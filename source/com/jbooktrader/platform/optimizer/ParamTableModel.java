package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.model.*;

/**
 * Strategy parameters table model.
 *
 * @author Eugene Kononov
 */
public class ParamTableModel extends TableDataModel {
    private static final String[] SCHEMA = {"Parameter", "Min Value", "Max Value", "Step"};

    public ParamTableModel() {
        setSchema(SCHEMA);
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return column == 0 ? String.class : Integer.class;
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

        int rowCount = getRowCount();
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            Object[] row = getRow(rowIndex);
            String name = (String) row[0];

            int min = (Integer) row[1];
            int max = (Integer) row[2];
            int step = (Integer) row[3];
            strategyParams.add(name, min, max, step, 0);
        }

        return strategyParams;
    }

    public long getNumCombinations() {
        long product = 1;

        for (int rowIndex = 0; rowIndex < getRowCount(); rowIndex++) {
            Object[] row = getRow(rowIndex);
            product *= (((Integer) row[2] - (Integer) row[1]) / (Integer) row[3]) + 1;
        }

        return product;
    }
}
