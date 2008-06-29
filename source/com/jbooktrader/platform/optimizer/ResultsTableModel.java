package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.strategy.*;

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

    public synchronized void setResults(List<OptimizationResult> optimizationResults) {
        removeAllData();

        for (OptimizationResult optimizationResult : optimizationResults) {
            Object[] item = new Object[getColumnCount() + 1];

            StrategyParams params = optimizationResult.getParams();

            int index = -1;
            for (StrategyParam param : params.getAll()) {
                item[++index] = param.getValue();
            }

            item[++index] = optimizationResult.getNetProfit();
            item[++index] = optimizationResult.getMaxDrawdown();
            item[++index] = optimizationResult.getTrades();
            item[++index] = optimizationResult.getProfitFactor();
            item[++index] = optimizationResult.getKellyCriterion();
            item[++index] = optimizationResult.getPerformanceIndex();

            addRow(item);
        }
    }
}
