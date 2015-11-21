package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.strategy.*;

import java.util.*;

/**
 * @author Eugene Kononov
 */
public class BruteForceOptimizerRunner extends OptimizerRunner {

    public BruteForceOptimizerRunner(OptimizerDialog optimizerDialog, Strategy strategy, StrategyParams params) throws JBookTraderException {
        super(optimizerDialog, strategy, params);
    }

    @Override
    public void optimize() throws JBookTraderException {
        Queue<StrategyParams> tasks = getTasks(strategyParams);
        int taskSize = tasks.size();
        setTotalSteps(snapshotCount * taskSize);
        setTotalStrategies(taskSize);
        execute(tasks);
    }
}
