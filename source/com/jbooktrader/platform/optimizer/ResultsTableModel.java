package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;

import javax.swing.*;
import java.text.*;
import java.util.*;

/**
 * Optimization results table model
 */
public class ResultsTableModel extends TableDataModel {
    public ResultsTableModel(Strategy strategy) {
        List<String> columnNames = new ArrayList<String>();
        for (StrategyParam param : strategy.getParams().getAll()) {
            columnNames.add(param.getName());
        }

        for (PerformanceMetric performanceMetric : PerformanceMetric.values()) {
            columnNames.add(performanceMetric.getName());
        }

        setSchema(columnNames.toArray(new String[columnNames.size()]));
    }


    @Override
    public Class<?> getColumnClass(int c) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    public void setResults(final List<OptimizationResult> optimizationResults) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                removeAllData();
                synchronized (optimizationResults) {
                    for (OptimizationResult optimizationResult : optimizationResults) {
                        Object[] item = new Object[getColumnCount() + 1];

                        StrategyParams params = optimizationResult.getParams();

                        int index = -1;
                        for (StrategyParam param : params.getAll()) {
                            item[++index] = param.getValue();
                        }

                        DecimalFormat df2 = NumberFormatterFactory.getNumberFormatter(2);
                        DecimalFormat df0 = NumberFormatterFactory.getNumberFormatter(0);

                        item[++index] = optimizationResult.getTrades();
                        item[++index] = df0.format(optimizationResult.getNetProfit());
                        item[++index] = df0.format(optimizationResult.getMaxDrawdown());
                        item[++index] = df2.format(optimizationResult.getProfitFactor());
                        item[++index] = df0.format(optimizationResult.getKellyCriterion());
                        item[++index] = df2.format(optimizationResult.getPerformanceIndex());

                        addRowFast(item);
                    }
                    fireTableDataChanged();
                }
            }
        });
    }
}
