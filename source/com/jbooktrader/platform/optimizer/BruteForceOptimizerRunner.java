package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.strategy.*;

import java.lang.reflect.*;
import java.util.*;

/**
 */
public class BruteForceOptimizerRunner extends OptimizerRunner {

    public BruteForceOptimizerRunner(OptimizerProgressIndicator optimizerProgressIndicator, Strategy strategy, StrategyParams params, String dataFileName, PerformanceMetric sortCriteria, int minTrades) throws JBookTraderException {
        super(optimizerProgressIndicator, strategy, params, dataFileName, sortCriteria, minTrades);
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
                    throw new JBookTraderException(ite.getCause());
                } catch (Exception e) {
                    throw new JBookTraderException(e);
                }

            }
            execute(strategies);
        }
    }
}
