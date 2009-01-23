package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.strategy.*;

import java.util.*;

/**
 * Runs a trading strategy in the optimizer mode using a data file containing
 * historical market depth.
 */
public class DivideAndConquerOptimizerRunner extends OptimizerRunner {

    public DivideAndConquerOptimizerRunner(OptimizerDialog optimizerDialog, Strategy strategy, StrategyParams params) throws JBookTraderException {
        super(optimizerDialog, strategy, params);
    }

    public void optimize() throws JBookTraderException {
        List<StrategyParams> bestParamsList = new ArrayList<StrategyParams>();
        StrategyParams startingParams = new StrategyParams(strategyParams);
        bestParamsList.add(startingParams);
        int dimensions = strategyParams.size();
        HashSet<StrategyParams> uniqueParams = new HashSet<StrategyParams>();

        int maxRange = 0;
        for (StrategyParam param : startingParams.getAll()) {
            maxRange = Math.max(maxRange, param.getMax() - param.getMin());
        }

        int iterationsRemaining = 1 + (int) (Math.log(maxRange) / Math.log(3d / 2d));
        long completedSteps = 0;
        LinkedList<StrategyParams> tasks = new LinkedList<StrategyParams>();
        List<Strategy> strategies = new LinkedList<Strategy>();
        int chunkSize = STRATEGIES_PER_PROCESSOR * availableProcessors;
        int numberOfCandidates = (int) (chunkSize / Math.pow(2, dimensions));


        do {

            tasks.clear();
            int maxPartsPerDimension = (bestParamsList.size() == 1) ? (int) Math.pow(chunkSize, 1. / dimensions) : 2;

            for (StrategyParams params : bestParamsList) {
                for (StrategyParam param : params.getAll()) {
                    int step = Math.max(1, (param.getMax() - param.getMin()) / (maxPartsPerDimension - 1));
                    param.setStep(step);
                }
                tasks.addAll(getTasks(params));
            }

            strategies.clear();
            for (StrategyParams params : tasks) {
                if (!uniqueParams.contains(params)) {
                    uniqueParams.add(params);
                    Strategy strategy = getStrategyInstance(params);
                    strategies.add(strategy);
                }
            }

            long totalSteps = completedSteps + snapshotCount * iterationsRemaining * strategies.size();
            setTotalSteps(totalSteps);
            setTotalStrategies(strategies.size());
            execute(strategies);

            iterationsRemaining--;
            completedSteps += snapshotCount * strategies.size();

            if (optimizationResults.size() == 0 && !cancelled) {
                throw new JBookTraderException("No strategies found within the specified parameter boundaries.");
            }


            bestParamsList.clear();

            int maxIndex = Math.min(numberOfCandidates, optimizationResults.size());
            for (int index = 0; index < maxIndex; index++) {
                StrategyParams params = optimizationResults.get(index).getParams();
                for (StrategyParam param : params.getAll()) {
                    String name = param.getName();
                    int value = param.getValue();
                    int displacement = Math.max(1, (param.getMax() - param.getMin()) / 3);
                    StrategyParam originalParam = strategyParams.get(name);
                    param.setMin(Math.max(originalParam.getMin(), value - displacement));
                    param.setMax(Math.min(originalParam.getMax(), value + displacement));
                }
                bestParamsList.add(new StrategyParams(params));
            }

        } while (strategies.size() > 0 && !cancelled);
    }
}
