package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.strategy.Strategy;

import java.util.*;

/**
 * Runs a trading strategy in the optimizer mode using a data file containing
 * historical market depth.
 */
public class BruteForceOptimizerRunner extends OptimizerRunner {
    public BruteForceOptimizerRunner(OptimizerDialog optimizerDialog, Strategy strategy, StrategyParams params) throws ClassNotFoundException, NoSuchMethodException {
        super(optimizerDialog, strategy, params);
    }

    public void optimize() throws Exception {

        LinkedList<StrategyParams> tasks = getTasks(strategyParams);

        ArrayList<Strategy> strategies = new ArrayList<Strategy>();
        long strategiesCreated = 0;
        for (StrategyParams params : tasks) {
            Strategy strategy = (Strategy) strategyConstructor.newInstance(params, marketBook);
            strategies.add(strategy);
            strategiesCreated++;
            if (strategiesCreated % 100 == 0) {
                optimizerDialog.setProgress(strategiesCreated, tasks.size(), "Creating " + tasks.size() + " strategies: ");
            }
        }

        long totalSteps = (long) lineCount * (long) strategies.size();
        setTotalSteps(totalSteps);

        execute(strategies);
    }
}
