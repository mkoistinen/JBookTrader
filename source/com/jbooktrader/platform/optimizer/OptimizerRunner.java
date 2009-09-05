package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.backtest.*;
import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.report.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;

import java.lang.reflect.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Runs a trading strategy in the optimizer mode using a data file containing
 * historical market depth.
 */
public abstract class OptimizerRunner implements Runnable {
    protected final ArrayList<OptimizationResult> optimizationResults;
    protected final StrategyParams strategyParams;
    protected long snapshotCount;
    protected boolean cancelled;
    protected final int availableProcessors;
    private static final int MAX_SAVED_RESULTS = 100;// max number of results in the optimization results file
    private final Constructor<?> strategyConstructor;
    private final ScheduledExecutorService progressExecutor, resultsTableExecutor;
    private ExecutorService optimizationExecutor;
    private final NumberFormat nf2, nf0, gnf0;
    private final String strategyName;
    private final int minTrades;
    private final OptimizerDialog optimizerDialog;
    private ResultComparator resultComparator;
    private ComputationalTimeEstimator timeEstimator;
    private final List<MarketSnapshot> snapshots;
    private long completedSteps, totalSteps;
    private String totalStrategiesString;
    private long previousResultsSize;


    class ProgressRunner implements Runnable {
        public void run() {
            if (completedSteps > 0) {
                showFastProgress(completedSteps, "Optimizing " + totalStrategiesString + " strategies");
            }
        }
    }

    class ResultsTableRunner implements Runnable {
        public void run() {
            int size = optimizationResults.size();
            if (size > previousResultsSize) {
                optimizerDialog.setResults(optimizationResults);
                previousResultsSize = size;
            }
        }
    }


    protected OptimizerRunner(OptimizerDialog optimizerDialog, Strategy strategy, StrategyParams params) throws JBookTraderException {
        this.optimizerDialog = optimizerDialog;
        strategyName = strategy.getName();
        strategyParams = params;
        optimizationResults = new ArrayList<OptimizationResult>();
        snapshots = new LinkedList<MarketSnapshot>();
        nf2 = NumberFormatterFactory.getNumberFormatter(2);
        nf0 = NumberFormatterFactory.getNumberFormatter(0);
        gnf0 = NumberFormatterFactory.getNumberFormatter(0, true);
        availableProcessors = Runtime.getRuntime().availableProcessors();

        Class<?> clazz;
        try {
            clazz = Class.forName(strategy.getClass().getName());
        } catch (ClassNotFoundException cnfe) {
            throw new JBookTraderException("Could not find class " + strategy.getClass().getName());
        }
        Class<?>[] parameterTypes = new Class[] {StrategyParams.class};

        try {
            strategyConstructor = clazz.getConstructor(parameterTypes);
        } catch (NoSuchMethodException nsme) {
            throw new JBookTraderException("Could not find strategy constructor for " + strategy.getClass().getName());
        }

        resultComparator = new ResultComparator(optimizerDialog.getSortCriteria());
        minTrades = optimizerDialog.getMinTrades();
        progressExecutor = Executors.newSingleThreadScheduledExecutor();
        resultsTableExecutor = Executors.newSingleThreadScheduledExecutor();
        optimizationExecutor = Executors.newFixedThreadPool(availableProcessors);
    }


    public Strategy getStrategyInstance(StrategyParams params) throws JBookTraderException {
        try {
            return (Strategy) strategyConstructor.newInstance(params);
        } catch (InvocationTargetException ite) {
            throw new JBookTraderException(ite.getCause());
        } catch (Exception e) {
            throw new JBookTraderException(e);
        }
    }


    protected abstract void optimize() throws JBookTraderException;

    protected void setTotalSteps(long totalSteps) {
        this.totalSteps = totalSteps;
        if (timeEstimator == null) {
            timeEstimator = new ComputationalTimeEstimator(System.currentTimeMillis(), totalSteps);
        }
        timeEstimator.setTotalIterations(totalSteps);
    }

    protected void setTotalStrategies(long totalStrategies) {
        totalStrategiesString = gnf0.format(totalStrategies);
    }

    public int getMinTrades() {
        return minTrades;
    }


    public List<MarketSnapshot> getSnapshots() {
        return snapshots;
    }

    public void addResults(List<OptimizationResult> results) {
        synchronized (optimizationResults) {
            optimizationResults.addAll(results);
            Collections.sort(optimizationResults, resultComparator);
        }
    }

    void execute(Queue<StrategyParams> tasks) throws JBookTraderException {
        if (!tasks.isEmpty()) {
            Set<Callable<List<OptimizationResult>>> workers = new HashSet<Callable<List<OptimizationResult>>>();
            for (int worker = 0; worker < availableProcessors; worker++) {
                Callable<List<OptimizationResult>> optimizerWorker = new OptimizerWorker(this, tasks);
                workers.add(optimizerWorker);
            }

            try {
                // this blocks until all workers are done
                optimizationExecutor.invokeAll(workers);
            } catch (InterruptedException ie) {
                throw new JBookTraderException(ie);
            }
        }
    }

    public void cancel() {
        optimizerDialog.showProgress("Stopping optimization...");
        cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    private void saveToFile() throws JBookTraderException {
        if (optimizationResults.isEmpty()) {
            return;
        }

        String fileName = strategyName + "Optimizer";
        OptimizationReport optimizationReport = new OptimizationReport(fileName);

        optimizationReport.reportDescription("Strategy parameters:");
        for (StrategyParam param : strategyParams.getAll()) {
            optimizationReport.reportDescription(param.toString());
        }
        optimizationReport.reportDescription("Minimum trades for strategy inclusion: " + optimizerDialog.getMinTrades());
        optimizationReport.reportDescription("Back data file: " + optimizerDialog.getFileName());

        List<String> otpimizerReportHeaders = new ArrayList<String>();
        StrategyParams params = optimizationResults.iterator().next().getParams();
        for (StrategyParam param : params.getAll()) {
            otpimizerReportHeaders.add(param.getName());
        }

        for (PerformanceMetric performanceMetric : PerformanceMetric.values()) {
            otpimizerReportHeaders.add(performanceMetric.getName());
        }
        optimizationReport.report(otpimizerReportHeaders);

        int maxIndex = Math.min(MAX_SAVED_RESULTS, optimizationResults.size());
        for (int index = 0; index < maxIndex; index++) {
            OptimizationResult optimizationResult = optimizationResults.get(index);
            params = optimizationResult.getParams();

            List<String> columns = new ArrayList<String>();
            for (StrategyParam param : params.getAll()) {
                columns.add(nf0.format(param.getValue()));
            }

            columns.add(nf0.format(optimizationResult.getTrades()));
            columns.add(nf0.format(optimizationResult.getNetProfit()));
            columns.add(nf0.format(optimizationResult.getMaxDrawdown()));
            columns.add(nf2.format(optimizationResult.getProfitFactor()));
            columns.add(nf0.format(optimizationResult.getKellyCriterion()));
            columns.add(nf2.format(optimizationResult.getPerformanceIndex()));

            optimizationReport.report(columns);
        }

    }

    private void showFastProgress(long counter, String text) {
        String remainingTime = (counter == totalSteps) ? "00:00:00" : timeEstimator.getTimeLeft(counter);
        optimizerDialog.setProgress(counter, totalSteps, text, remainingTime);
    }

    public synchronized void iterationsCompleted(long iterationsCompleted) {
        completedSteps += iterationsCompleted;
    }

    protected Queue<StrategyParams> getTasks(StrategyParams params) {
        for (StrategyParam param : params.getAll()) {
            param.setValue(param.getMin());
        }

        Queue<StrategyParams> tasks = new LinkedBlockingQueue<StrategyParams>();

        boolean allTasksAssigned = false;
        while (!allTasksAssigned && !cancelled) {
            StrategyParams strategyParamsCopy = new StrategyParams(params);
            tasks.add(strategyParamsCopy);

            StrategyParam lastParam = params.get(params.size() - 1);
            lastParam.setValue(lastParam.getValue() + lastParam.getStep());

            for (int paramNumber = params.size() - 1; paramNumber >= 0; paramNumber--) {
                StrategyParam param = params.get(paramNumber);
                if (param.getValue() > param.getMax()) {
                    param.setValue(param.getMin());
                    if (paramNumber == 0) {
                        allTasksAssigned = true;
                        break;
                    } else {
                        int prevParamNumber = paramNumber - 1;
                        StrategyParam prevParam = params.get(prevParamNumber);
                        prevParam.setValue(prevParam.getValue() + prevParam.getStep());
                    }
                }
            }
        }

        return tasks;
    }

    public void run() {
        try {
            optimizationResults.clear();
            optimizerDialog.setResults(optimizationResults);
            optimizerDialog.enableProgress();
            optimizerDialog.showProgress("Scanning historical data file...");
            BackTestFileReader backTestFileReader = new BackTestFileReader(optimizerDialog.getFileName());
            backTestFileReader.setFilter(optimizerDialog.getDateFilter());
            backTestFileReader.scan();
            snapshotCount = backTestFileReader.getSnapshotCount();

            MarketSnapshot marketSnapshot;
            long count = 0;
            String progressMessage = "Loading historical data file: ";
            while ((marketSnapshot = backTestFileReader.next()) != null) {
                snapshots.add(marketSnapshot);
                count++;
                if (count % 50000 == 0) {
                    optimizerDialog.setProgress(count, snapshotCount, progressMessage);
                }
                if (cancelled) {
                    return;
                }
            }

            optimizerDialog.showProgress("Starting optimization ...");
            progressExecutor.scheduleWithFixedDelay(new ProgressRunner(), 0, 1, TimeUnit.SECONDS);
            resultsTableExecutor.scheduleWithFixedDelay(new ResultsTableRunner(), 0, 30, TimeUnit.SECONDS);
            long start = System.currentTimeMillis();
            optimize();
            long end = System.currentTimeMillis();
            progressExecutor.shutdownNow();
            resultsTableExecutor.shutdownNow();


            optimizerDialog.setResults(optimizationResults);

            if (!cancelled) {
                optimizerDialog.showProgress("Saving optimization results ...");
                saveToFile();
                long totalTimeInSecs = (end - start) / 1000;
                showFastProgress(totalSteps, "Optimization");
                MessageDialog.showMessage("Optimization completed successfully in " + totalTimeInSecs + " seconds.");
            }
        } catch (Throwable t) {
            MessageDialog.showError(t);
        } finally {
            progressExecutor.shutdownNow();
            resultsTableExecutor.shutdownNow();
            optimizationExecutor.shutdownNow();
            optimizerDialog.signalCompleted();
        }
    }
}
