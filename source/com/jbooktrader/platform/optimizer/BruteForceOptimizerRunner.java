package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.strategy.*;

import java.util.*;

/**
 */
public class BruteForceOptimizerRunner extends OptimizerRunner {

    public BruteForceOptimizerRunner(OptimizerDialog optimizerDialog, Strategy strategy, StrategyParams params) throws JBookTraderException {
        super(optimizerDialog, strategy, params);
    }

    @Override
    public void optimize() throws JBookTraderException {
        ArrayList<StrategyParams> tasks = getTasks(strategyParams);
        int taskSize = tasks.size();
        long totalSteps = snapshotCount * taskSize;
        setTotalSteps(totalSteps);
        setTotalStrategies(taskSize);

        int chunkSize = STRATEGIES_PER_PROCESSOR * availableProcessors;

        List<Strategy> strategies = new ArrayList<Strategy>(chunkSize);
        int index = 0;

        while (index < taskSize && !cancelled) {
            strategies.clear();
            while (index < taskSize && strategies.size() != chunkSize) {
                StrategyParams params = tasks.get(index);
                Strategy strategy = getStrategyInstance(params);
                strategies.add(strategy);
                index++;
            }
            execute(strategies);
        }
    }
}
