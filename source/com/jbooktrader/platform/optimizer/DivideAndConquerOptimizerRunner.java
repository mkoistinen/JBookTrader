package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.strategy.Strategy;

import java.util.*;

/**
 * Runs a trading strategy in the optimizer mode using a data file containing
 * historical market depth.
 */
public class DivideAndConquerOptimizerRunner extends OptimizerRunner {
    private final int divider = 2;

    public DivideAndConquerOptimizerRunner(OptimizerDialog optimizerDialog, Strategy strategy, StrategyParams params) throws ClassNotFoundException, NoSuchMethodException {
        super(optimizerDialog, strategy, params);
    }

    public void optimize() throws Exception {
        StrategyParams newParams = new StrategyParams(strategyParams);

        int range = 0;
        for (StrategyParam param : newParams.getAll()) {
            range = Math.max(range, param.getMax() - param.getMin());
        }

        int iterationsRemaining = (int) (Math.log(range) / Math.log(2.0));
        long completedSteps = 0;

        boolean allDone = false;
        while (!allDone) {

            for (StrategyParam param : newParams.getAll()) {
                param.setValue(param.getMin());
            }

            LinkedList<StrategyParams> tasks = new LinkedList<StrategyParams>();
            boolean allTasksAssigned = false;

            while (!allTasksAssigned) {
                StrategyParams strategyParamsCopy = new StrategyParams(newParams);
                tasks.add(strategyParamsCopy);

                StrategyParam lastParam = newParams.get(newParams.size() - 1);
                int step = Math.max(1, (lastParam.getMax() - lastParam.getMin()) / divider);
                lastParam.setValue(lastParam.getValue() + step);

                for (int paramNumber = newParams.size() - 1; paramNumber >= 0; paramNumber--) {
                    StrategyParam param = newParams.get(paramNumber);
                    if (param.getValue() > param.getMax()) {
                        param.setValue(param.getMin());
                        if (paramNumber == 0) {
                            allTasksAssigned = true;
                            break;
                        } else {
                            int prevParamNumber = paramNumber - 1;
                            StrategyParam prevParam = newParams.get(prevParamNumber);
                            step = Math.max(1, (prevParam.getMax() - prevParam.getMin()) / divider);
                            prevParam.setValue(prevParam.getValue() + step);
                        }
                    }
                }
            }

            List<Strategy> strategies = new ArrayList<Strategy>();
            for (StrategyParams params : tasks) {
                Strategy strategy = (Strategy) strategyConstructor.newInstance(params, marketBook);
                strategies.add(strategy);
            }


            long totalSteps = completedSteps + (long) lineCount * iterationsRemaining * strategies.size();
            setTotalSteps(totalSteps);

            execute(strategies);
            if (cancelled) {
                return;
            }
            iterationsRemaining--;
            completedSteps += (long) lineCount * strategies.size();

            strategies.clear();

            allDone = true;
            for (StrategyParam param : newParams.getAll()) {
                if (param.getMax() - param.getMin() >= 4) {
                    allDone = false;
                }
            }

            if (!allDone) {
                Result bestResult = results.get(0);
                StrategyParams bestParams = bestResult.getParams();

                newParams.getAll().clear();
                for (StrategyParam param : bestParams.getAll()) {
                    int value = bestResult.getParams().get(param.getName());
                    range = param.getMax() - param.getMin();
                    int min = Math.max(strategyParams.getMin(param.getName()), value - range / 4);
                    int max = Math.min(strategyParams.getMax(param.getName()), value + range / 4);
                    param.setMinMax(min, max);
                    newParams.add(param.getName(), min, max, 0, value);
                }
            }
        }
    }
}
