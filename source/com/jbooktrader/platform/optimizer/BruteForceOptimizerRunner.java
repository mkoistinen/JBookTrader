package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.util.ComputationalTimeEstimator;

import java.util.*;

/**
 * Runs a trading strategy in the optimizer mode using a data file containing
 * historical market depth.
 */
public class BruteForceOptimizerRunner extends OptimizerRunner {
    private static final int SUBTASK_SIZE = 1000;

    public BruteForceOptimizerRunner(OptimizerDialog optimizerDialog, Strategy strategy, StrategyParams params) throws ClassNotFoundException, NoSuchMethodException {
        super(optimizerDialog, strategy, params);
    }

    public void optimize() throws Exception {

        for (StrategyParam param : strategyParams.getAll()) {
            param.setValue(param.getMin());
        }

        LinkedList<StrategyParams> tasks = new LinkedList<StrategyParams>();
        optimizerDialog.showProgress("Distributing the tasks...");
        boolean allTasksAssigned = false;

        while (!allTasksAssigned) {
            StrategyParams strategyParamsCopy = new StrategyParams(strategyParams);
            tasks.add(strategyParamsCopy);

            StrategyParam lastParam = strategyParams.get(strategyParams.size() - 1);
            lastParam.setValue(lastParam.getValue() + lastParam.getStep());

            for (int paramNumber = strategyParams.size() - 1; paramNumber >= 0; paramNumber--) {
                StrategyParam param = strategyParams.get(paramNumber);
                if (param.getValue() > param.getMax()) {
                    param.setValue(param.getMin());
                    if (paramNumber == 0) {
                        allTasksAssigned = true;
                        break;
                    } else {
                        int prevParamNumber = paramNumber - 1;
                        StrategyParam prevParam = strategyParams.get(prevParamNumber);
                        prevParam.setValue(prevParam.getValue() + prevParam.getStep());
                    }
                }
            }
        }

        int numberOfTasks = tasks.size();
        if (numberOfTasks > MAX_ITERATIONS) {
            optimizerDialog.showMaxIterationsLimit(numberOfTasks, MAX_ITERATIONS);
            return;
        }


        timeEstimator = new ComputationalTimeEstimator(System.currentTimeMillis(), lineCount);

        ArrayList<ArrayList<Strategy>> subTasks = new ArrayList<ArrayList<Strategy>>();
        ArrayList<Strategy> subtask = new ArrayList<Strategy>();
        subTasks.add(subtask);
        long strategiesCreated = 0;

        for (StrategyParams params : tasks) {
            if (subtask.size() >= SUBTASK_SIZE) {
                subtask = new ArrayList<Strategy>();
                subTasks.add(subtask);
            }
            Strategy strategy = (Strategy) strategyConstructor.newInstance(params, marketBook);
            subtask.add(strategy);
            strategiesCreated++;
            if (strategiesCreated % 1000 == 0) {
                showFastProgress(strategiesCreated, tasks.size(), "Creating " + tasks.size() + " strategies: ");
            }
        }

        long totalSteps = (long) lineCount * (long) tasks.size();
        setTotalSteps(totalSteps);
        for (ArrayList<Strategy> strategies : subTasks) {
            execute(strategies);
        }

    }
}
