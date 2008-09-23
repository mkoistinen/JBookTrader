package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.model.Dispatcher.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;

import java.io.*;
import java.util.*;

public class CommandLineOptimizer {

    private class CommandLineOptimizerProgessIndicator implements OptimizerProgressIndicator {

        private int oldpercent = -1;

        public void enableProgress() {
            System.err.println("Starting to optimize...");
        }

        public void setProgress(long counter, long totalSteps, String text, String remainingTime) {
            int percent = (int) (100 * (counter / (double) totalSteps));

            if (percent != oldpercent) {
                System.err.println(text + ": " + percent + "% completed (ETA " + remainingTime + ")");
            }
            oldpercent = percent;
        }

        public void setResults(List<OptimizationResult> optimizationResults) {
        }

        public void showError(String string) {
            System.err.println("Error: " + string);
        }

        public void showMessage(String string) {
            System.err.println("Info: " + string);
        }

        public void showProgress(String string) {
            System.err.println(string);
        }

        public void signalCompleted() {

        }

    }

    public CommandLineOptimizer(String strategyName, String dataFileName, String sortCriteria, String pMinTrades, String optimizerMethodName) throws JBookTraderException, InterruptedException {

        Dispatcher.setMode(Mode.Optimization);

        Strategy strategy = ClassFinder.getInstance(strategyName);

        File file = new File(dataFileName);
        if (!file.exists()) {
            throw new JBookTraderException("Historical file " + "\"" + dataFileName + "\"" + " does not exist.");
        }

        int minTrades = Integer.valueOf(pMinTrades).intValue();
        if (minTrades < 1) {
            CommandLineStarter.showUsage();
            return;
        }

        PerformanceMetric performanceMetric = PerformanceMetric.getColumn(sortCriteria);
        if (performanceMetric == null) {
            CommandLineStarter.showUsage();
            return;
        }

        OptimizerRunner optimizerRunner;
        if (optimizerMethodName.equals("bf")) {
            optimizerRunner = new BruteForceOptimizerRunner(new CommandLineOptimizerProgessIndicator(), strategy, strategy.getParams(), dataFileName, performanceMetric, minTrades);
        } else if (optimizerMethodName.equals("dnc")) {
            optimizerRunner = new DivideAndConquerOptimizerRunner(new CommandLineOptimizerProgessIndicator(), strategy, strategy.getParams(), dataFileName, performanceMetric, minTrades);
        } else {
            CommandLineStarter.showUsage();
            return;
        }

        optimizerRunner.run();
    }
}
