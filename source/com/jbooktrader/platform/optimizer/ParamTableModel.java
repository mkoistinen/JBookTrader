package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.model.TableDataModel;

/**
 * Strategy parameters table model.
 */
public class ParamTableModel extends TableDataModel {
    private static final String[] SCHEMA = {"Parameter", "Min Value", "Max Value", "Step"};

    public ParamTableModel() {
        setSchema(SCHEMA);
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return column == 0 ? String.class : Double.class;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        // param name column cannot be edited
        return !(col == 0);

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
        for (int i = 0; i < rows; i++) {
            Object[] row = getRow(i);
            String name = (String) row[0];

            double min = (Double) row[1];
            double max = (Double) row[2];
            double step = (Double) row[3];
            strategyParams.add(name, min, max, step);
        }

        return strategyParams;
    }
}
