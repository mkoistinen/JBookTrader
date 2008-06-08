package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.strategy.*;

import java.util.*;

/**
 * Runs a trading strategy in the optimizer mode using a data file containing
 * historical market depth.
 */
public class BruteForceOptimizerRunner extends OptimizerRunner {
    public BruteForceOptimizerRunner(OptimizerDialog optimizerDialog, Strategy strategy, StrategyParams params) throws ClassNotFoundException, NoSuchMethodException {
        super(optimizerDialog, strategy, params);
    }

    public void optimize() throws JBookTraderException {
        LinkedList<StrategyParams> tasks = getTasks(strategyParams);

        ArrayList<Strategy> strategies = new ArrayList<Strategy>();
        long strategiesCreated = 0;
        for (StrategyParams params : tasks) {
            try {
                Strategy strategy = (Strategy) strategyConstructor.newInstance(params, marketBook, priceHistory);
                strategies.add(strategy);
            } catch (Exception e) {
                throw new JBookTraderException(e);
            }
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
