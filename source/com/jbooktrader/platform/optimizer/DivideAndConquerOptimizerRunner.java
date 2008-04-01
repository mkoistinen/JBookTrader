package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.strategy.*;

import java.util.*;

/**
 * Runs a trading strategy in the optimizer mode using a data file containing
 * historical market depth.
 */
public class DivideAndConquerOptimizerRunner extends OptimizerRunner {
    private final static int POPULATION_SIZE = 3;

    public DivideAndConquerOptimizerRunner(OptimizerDialog optimizerDialog, Strategy strategy, StrategyParams params) throws ClassNotFoundException, NoSuchMethodException {
        super(optimizerDialog, strategy, params);
    }

    public void optimize() throws JBookTraderException {
        StrategyParams bestParams = new StrategyParams(strategyParams);
        HashSet<StrategyParams> uniqueParams = new HashSet<StrategyParams>();

        int maxRange = 0;
        for (StrategyParam param : bestParams.getAll()) {
            maxRange = Math.max(maxRange, param.getMax() - param.getMin());
        }

        int iterationsRemaining = (int) (Math.log(maxRange) / Math.log(2.0));
        long completedSteps = 0;

        boolean allDone = false;
        while (!allDone) {

            for (StrategyParam param : bestParams.getAll()) {
                int step = Math.max(1, (param.getMax() - param.getMin()) / POPULATION_SIZE);
                param.setStep(step);
                param.setValue(param.getMin());
            }

            LinkedList<StrategyParams> tasks = getTasks(bestParams);

            List<Strategy> strategies = new ArrayList<Strategy>();
            for (StrategyParams params : tasks) {
                if (!uniqueParams.contains(params)) {
                    uniqueParams.add(params);
                    try {
                        Strategy strategy = (Strategy) strategyConstructor.newInstance(params, marketBook, priceHistory);
                        strategies.add(strategy);
                    } catch (Exception e) {
                        throw new JBookTraderException(e);
                    }
                }
            }

            long totalSteps = completedSteps + (long) lineCount * iterationsRemaining * strategies.size();
            setTotalSteps(totalSteps);

            allDone = (strategies.size() == 0);
            if (!allDone) {
                execute(strategies);
                if (cancelled) {
                    return;
                }

                iterationsRemaining--;
                completedSteps += (long) lineCount * strategies.size();

                if (results.size() == 0) {
                    throw new JBookTraderException("No strategies found within the specified boundaries.");
                }

                StrategyParams topParams = results.get(0).getParams();

                bestParams.getAll().clear();
                for (StrategyParam param : topParams.getAll()) {
                    String name = param.getName();
                    int value = topParams.get(name).getValue();
                    int range = param.getMax() - param.getMin();
                    int min = Math.max(strategyParams.get(name).getMin(), value - range / 4);
                    int max = Math.min(strategyParams.get(name).getMax(), value + range / 4);
                    param.setMin(min);
                    param.setMax(max);
                    bestParams.add(name, min, max, 0, value);
                }
            }
        }
    }
}
