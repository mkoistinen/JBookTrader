package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.model.*;
import static com.jbooktrader.platform.optimizer.PerformanceMetric.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;

import java.text.*;
import java.util.*;

/**
 * Optimization results table model
 */
public class ResultsTableModel extends TableDataModel {
    public ResultsTableModel(Strategy strategy) {
        List<String> columnNames = new LinkedList<String>();
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
        return DoubleNumericString.class;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    public void setResults(List<OptimizationResult> optimizationResults) {
        synchronized (optimizationResults) {
            rows.clear();
            for (OptimizationResult optimizationResult : optimizationResults) {
                Object[] item = new Object[getColumnCount() + 1];

                StrategyParams params = optimizationResult.getParams();

                int column = 0;
                for (StrategyParam param : params.getAll()) {
                    item[column] = param.getValue();
                    column++;
                }

                DecimalFormat df2 = NumberFormatterFactory.getNumberFormatter(2);
                DecimalFormat df0 = NumberFormatterFactory.getNumberFormatter(0);

                item[column + Trades.ordinal()] = new DoubleNumericString(df0.format(optimizationResult.get(Trades)));
                item[column + Duration.ordinal()] = new DoubleNumericString(df0.format(optimizationResult.get(Duration)));
                item[column + Bias.ordinal()] = new DoubleNumericString(df0.format(optimizationResult.get(Bias)));
                item[column + PF.ordinal()] = new DoubleNumericString(df2.format(optimizationResult.get(PF)));
                item[column + PI.ordinal()] = new DoubleNumericString(df2.format(optimizationResult.get(PI)));
                item[column + Kelly.ordinal()] = new DoubleNumericString(df0.format(optimizationResult.get(Kelly)));
                item[column + MaxDD.ordinal()] = new DoubleNumericString(df0.format(optimizationResult.get(MaxDD)));
                item[column + NetProfit.ordinal()] = new DoubleNumericString(df0.format(optimizationResult.get(NetProfit)));

                rows.add(item);
            }

            fireTableDataChanged();
        }
    }
}
