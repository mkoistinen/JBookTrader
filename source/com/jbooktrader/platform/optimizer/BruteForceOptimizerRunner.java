package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.strategy.*;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 */
public class BruteForceOptimizerRunner extends OptimizerRunner {
    private static final int STRATEGIES_PER_PROCESSOR = 250;

    public BruteForceOptimizerRunner(OptimizerDialog optimizerDialog, Strategy strategy, StrategyParams params) throws JBookTraderException {
        super(optimizerDialog, strategy, params);
    }

    public void optimize() throws JBookTraderException {
        LinkedList<StrategyParams> tasks = getTasks(strategyParams);
        int taskSize = tasks.size();
        long totalSteps = (long) lineCount * taskSize;
        setTotalSteps(totalSteps);
        setTotalStrategies(taskSize);

        int chunkSize = STRATEGIES_PER_PROCESSOR * availableProcessors;

        List<Strategy> strategies = new LinkedList<Strategy>();

        while (!tasks.isEmpty() && !cancelled) {
            strategies.clear();
            while (!tasks.isEmpty() && strategies.size() != chunkSize) {
                StrategyParams params = tasks.removeFirst();
                try {
                    Strategy strategy = (Strategy) strategyConstructor.newInstance(params);
                    strategies.add(strategy);
                } catch (InvocationTargetException ite) {
                	throw new JBookTraderException(new Exception(ite.getCause()));
                } catch (Exception e) {
                    throw new JBookTraderException(e);
                }

            }
            execute(strategies);
        }
    }
}
