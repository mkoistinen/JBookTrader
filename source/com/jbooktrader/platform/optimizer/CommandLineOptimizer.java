package com.jbooktrader.platform.optimizer;

import java.io.File;
import java.util.List;

import com.jbooktrader.platform.model.JBookTraderException;
import com.jbooktrader.platform.startup.JBookTrader;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.util.ClassFinder;

public class CommandLineOptimizer {
    
    private class CommandLineOptimizerProgessIndicator implements OptimizerProgressIndicator {

        public void enableProgress() {
            System.err.println("Starting to optimize...");
        }

        public void setProgress(long counter, long totalSteps, String text, String remainingTime) {
            int percent = (int) (100 * (counter / (double) totalSteps));
            System.err.println(text + ": " + percent + "% completed (ETA "+remainingTime+")");   
        }

        public void setResults(List<OptimizationResult> optimizationResults) {
        }

        public void showError(String string) {
            System.err.println("Error: "+ string);
        }

        public void showMessage(String string) {
            System.err.println("Info: "+ string);
        }

        public void showProgress(String string) {
            System.err.println(string);
        }

        public void signalCompleted() {
            
        }
        
    }
    
    public CommandLineOptimizer(String strategyName, String dataFileName, String sortCriteria, String pMinTrades, String optimizerMethodName) throws JBookTraderException, InterruptedException {

        Strategy strategy = ClassFinder.getInstance(strategyName);

        File file = new File(dataFileName);
        if (!file.exists()) {
            throw new JBookTraderException("Historical file " + "\"" + dataFileName + "\"" + " does not exist.");
        }
        
        int minTrades = Integer.valueOf(pMinTrades).intValue();
        if(minTrades<1) {
            JBookTrader.showUsage();
            return;
        }

        PerformanceMetric performanceMetric = PerformanceMetric.getColumn(sortCriteria);
        if(performanceMetric==null) {
            JBookTrader.showUsage();
            return;
        }
        
        OptimizerRunner optimizerRunner;
        if (optimizerMethodName.equals("bf")) {
            optimizerRunner = new BruteForceOptimizerRunner(new CommandLineOptimizerProgessIndicator(), strategy, strategy.getParams(), dataFileName, performanceMetric, minTrades);
        } else if (optimizerMethodName.equals("dnc")) {
            optimizerRunner = new DivideAndConquerOptimizerRunner(new CommandLineOptimizerProgessIndicator(), strategy, strategy.getParams(), dataFileName, performanceMetric, minTrades);
        } else {
            JBookTrader.showUsage();
            return;
        }

        Thread optimizerThread = new Thread(optimizerRunner);
        optimizerThread.start();
        optimizerThread.join();
    }
}
