package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.strategy.*;

import java.util.*;

/**
 * Runs a trading strategy in the optimizer mode using a data file containing
 * historical market depth.
 */
public class DivideAndConquerOptimizerRunner extends OptimizerRunner {
    private final static int POPULATION_SIZE = 5;
    private final static int PREFERRED_RUNNING_LIST_SIZE = 100;

    public DivideAndConquerOptimizerRunner(OptimizerDialog optimizerDialog, Strategy strategy, StrategyParams params) throws ClassNotFoundException, NoSuchMethodException {
        super(optimizerDialog, strategy, params);
    }

    public void optimize() throws JBookTraderException {
        List<StrategyParams> bestParamsList = new ArrayList<StrategyParams>();
        StrategyParams startingParams = new StrategyParams(strategyParams);
        bestParamsList.add(startingParams);
        int dimensions = strategyParams.size();
        int hyperCubes = (int) Math.pow(POPULATION_SIZE, dimensions);
        int topSize = PREFERRED_RUNNING_LIST_SIZE / hyperCubes;

        HashSet<StrategyParams> uniqueParams = new HashSet<StrategyParams>();

        int maxRange = 0;
        for (StrategyParam param : startingParams.getAll()) {
            maxRange = Math.max(maxRange, param.getMax() - param.getMin());
        }

        int iterationsRemaining = (int) (Math.log(maxRange) / Math.log(2.0));
        long completedSteps = 0;
        LinkedList<StrategyParams> tasks = new LinkedList<StrategyParams>();
        List<Strategy> strategies = new ArrayList<Strategy>();
        List<StrategyParams> topParams = new ArrayList<StrategyParams>();

        boolean allDone = false;
        while (!allDone) {

            tasks.clear();

            for (StrategyParams params : bestParamsList) {
                for (StrategyParam param : params.getAll()) {
                    int step = Math.max(1, (param.getMax() - param.getMin()) / (POPULATION_SIZE - 1));
                    param.setStep(step);
                }
                tasks.addAll(getTasks(params));
            }


            strategies.clear();
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

            long totalSteps = completedSteps + (long) lineCount * iterationsRemaining * (PREFERRED_RUNNING_LIST_SIZE / 3);
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
                    throw new JBookTraderException("No strategies found within the specified parameter boundaries.");
                }


                topParams.clear();
                long maxIndex = Math.min(results.size(), Math.max(1, topSize));
                for (int index = 0; index < maxIndex; index++) {
                    topParams.add(results.get(index).getParams());
                }


                bestParamsList.clear();
                for (StrategyParams params : topParams) {
                    StrategyParams bestParams = new StrategyParams();
                    for (StrategyParam param : params.getAll()) {
                        String name = param.getName();
                        int value = params.get(name).getValue();
                        int displacement = (param.getMax() - param.getMin()) / 4;
                        StrategyParam originalParam = strategyParams.get(name);
                        int min = Math.max(originalParam.getMin(), value - displacement);
                        int max = Math.min(originalParam.getMax(), value + displacement);
                        param.setMin(min);
                        param.setMax(max);
                        bestParams.add(name, min, max, 0, value);
                    }
                    bestParamsList.add(bestParams);
                }
            }
        }
    }
}
