package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.strategy.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * Runs a trading strategy in the optimizer mode using a data file containing
 * historical market depth.
 */
public class DivideAndConquerOptimizerRunner extends OptimizerRunner {

    public DivideAndConquerOptimizerRunner(OptimizerProgressIndicator optimizerProgressIndicator, Strategy strategy, StrategyParams params, String dataFileName, PerformanceMetric sortCriteria, int minTrades) throws JBookTraderException {
        super(optimizerProgressIndicator, strategy, params, dataFileName, sortCriteria, minTrades);
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

        int iterationsRemaining = (int) (Math.log(maxRange) / Math.log(2.0));
        long completedSteps = 0;
        LinkedList<StrategyParams> tasks = new LinkedList<StrategyParams>();
        List<Strategy> strategies = new LinkedList<Strategy>();
        List<StrategyParams> topParams = new ArrayList<StrategyParams>();
        int chunkSize = STRATEGIES_PER_PROCESSOR * availableProcessors;
        int numberOfCandidates = (int) Math.min(Math.sqrt(chunkSize), bestParamsList.size());

        do {

            tasks.clear();
            int partsPerDimension = (int) Math.max(2, Math.pow((double) chunkSize / numberOfCandidates, 1. / dimensions));
            for (StrategyParams params : bestParamsList) {
                for (StrategyParam param : params.getAll()) {
                    int step = Math.max(1, (param.getMax() - param.getMin()) / (partsPerDimension - 1));
                    param.setStep(step);
                }
                tasks.addAll(getTasks(params));
            }

            strategies.clear();
            for (StrategyParams params : tasks) {
                if (!uniqueParams.contains(params)) {
                    uniqueParams.add(params);
                    try {
                        Strategy strategy = (Strategy) strategyConstructor.newInstance(params);
                        strategies.add(strategy);
                    } catch (InvocationTargetException ite) {
                        throw new JBookTraderException(new Exception(ite.getCause()));
                    } catch (Exception e) {
                        throw new JBookTraderException(e);
                    }
                }
            }

            long totalSteps = completedSteps + (long) lineCount * iterationsRemaining * strategies.size();
            setTotalSteps(totalSteps);
            setTotalStrategies(strategies.size());
            execute(strategies);

            iterationsRemaining--;
            completedSteps += (long) lineCount * strategies.size();

            if (optimizationResults.size() == 0 && !cancelled) {
                throw new JBookTraderException("No strategies found within the specified parameter boundaries.");
            }

            topParams.clear();
            numberOfCandidates = (int) Math.min(Math.sqrt(chunkSize), optimizationResults.size());
            for (int index = 0; index < numberOfCandidates; index++) {
                topParams.add(optimizationResults.get(index).getParams());
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
        } while (strategies.size() > 0 && !cancelled);
    }
}

/* $Id$ */
