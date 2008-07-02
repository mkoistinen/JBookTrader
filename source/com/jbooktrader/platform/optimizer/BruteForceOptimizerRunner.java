package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.strategy.*;

import java.util.*;

/**
 * Runs a trading strategy in the optimizer mode using a data file containing
 * historical market depth.
 */
public class BruteForceOptimizerRunner extends OptimizerRunner {
    private static final int CHUNK_SIZE = 1000;

    public BruteForceOptimizerRunner(OptimizerDialog optimizerDialog, Strategy strategy, StrategyParams params) throws ClassNotFoundException, NoSuchMethodException {
        super(optimizerDialog, strategy, params);
    }

    public void optimize() throws JBookTraderException {
        LinkedList<StrategyParams> tasks = getTasks(strategyParams);
        int taskSize = tasks.size();
        long totalSteps = (long) lineCount * taskSize;

        ArrayList<Strategy> strategies = new ArrayList<Strategy>();

        while (!tasks.isEmpty() && !cancelled) {
            strategies.clear();
            while (!tasks.isEmpty() && strategies.size() != CHUNK_SIZE) {
                StrategyParams params = tasks.removeFirst();
                try {
                    Strategy strategy = (Strategy) strategyConstructor.newInstance(params, marketBook);
                    strategies.add(strategy);
                } catch (Exception e) {
                    throw new JBookTraderException(e);
                }

            }

            execute(strategies, taskSize, totalSteps);
        }
    }
}
