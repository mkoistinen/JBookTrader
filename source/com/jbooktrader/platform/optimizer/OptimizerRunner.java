package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.backtest.*;
import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;
import static com.jbooktrader.platform.optimizer.PerformanceMetric.*;
import com.jbooktrader.platform.report.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;

import java.io.*;
import java.lang.reflect.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * Runs a trading strategy in the optimizer mode using a data file containing
 * historical market snapshots.
 */
public abstract class OptimizerRunner implements Runnable {
    protected final List<OptimizationResult> optimizationResults;
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
    private final AtomicLong completedSteps;
    private final OptimizerDialog optimizerDialog;
    private ResultComparator resultComparator;
    private ComputationalTimeEstimator timeEstimator;
    private List<MarketSnapshot> snapshots;
    private long totalSteps;
    private String totalStrategiesString;
    private long previousResultsSize;

    private class ProgressRunner implements Runnable {
        public void run() {
            if (completedSteps.get() > 0) {
                showProgress(completedSteps.get(), "Optimizing " + totalStrategiesString + " strategies");
            }
        }
    }

    private class ResultsTableRunner implements Runnable {
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
        optimizationResults = new LinkedList<OptimizationResult>();
        nf2 = NumberFormatterFactory.getNumberFormatter(2);
        nf0 = NumberFormatterFactory.getNumberFormatter(0);
        gnf0 = NumberFormatterFactory.getNumberFormatter(0, true);
        availableProcessors = Runtime.getRuntime().availableProcessors();
        completedSteps = new AtomicLong();

        Class<?> clazz;
        try {
            clazz = Class.forName(strategy.getClass().getName());
        } catch (ClassNotFoundException cnfe) {
            throw new JBookTraderException("Could not find class " + strategy.getClass().getName());
        }

        try {
            strategyConstructor = clazz.getConstructor(new Class[] {StrategyParams.class});
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
            Set<Callable<Void>> workers = new HashSet<Callable<Void>>();
            for (int worker = 0; worker < availableProcessors; worker++) {
                Callable<Void> optimizerWorker = new OptimizerWorker(this, tasks);
                workers.add(optimizerWorker);
            }
            try {
                // submit all workers and wait until they are done
                List<Future<Void>> futureResults = optimizationExecutor.invokeAll(workers);
                // all workers are done, call the "get()" to check for exceptions
                for (Future<Void> futureResult : futureResults) {
                    futureResult.get();
                }
            } catch (Exception e) {
                throw new JBookTraderException(e.getMessage(), e);
            }
        }
    }

    public void cancel() {
        optimizerDialog.setProgress("Stopping optimization...");
        cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    private void saveToFile() throws IOException {
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
        optimizationReport.reportHeaders(otpimizerReportHeaders);

        int maxIndex = Math.min(MAX_SAVED_RESULTS, optimizationResults.size());
        for (int index = 0; index < maxIndex; index++) {
            OptimizationResult optimizationResult = optimizationResults.get(index);
            params = optimizationResult.getParams();

            List<String> columns = new ArrayList<String>();
            for (StrategyParam param : params.getAll()) {
                columns.add(nf0.format(param.getValue()));
            }

            columns.add(nf0.format(optimizationResult.get(Trades)));
            columns.add(nf0.format(optimizationResult.get(Duration)));
            columns.add(nf0.format(optimizationResult.get(Bias)));
            columns.add(nf2.format(optimizationResult.get(PF)));
            columns.add(nf2.format(optimizationResult.get(PI)));
            columns.add(nf0.format(optimizationResult.get(Kelly)));
            columns.add(nf0.format(optimizationResult.get(MaxDD)));
            columns.add(nf0.format(optimizationResult.get(NetProfit)));

            optimizationReport.report(columns);
        }

    }

    private void showProgress(long counter, String text) {
        optimizerDialog.setProgress(counter, totalSteps, text);
        String remainingTime = (counter >= totalSteps) ? "00:00:00" : timeEstimator.getTimeLeft(counter);
        optimizerDialog.setRemainingTime(remainingTime);
    }

    public void iterationsCompleted(long iterationsCompleted) {
        completedSteps.getAndAdd(iterationsCompleted);
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
                    }
                    int prevParamNumber = paramNumber - 1;
                    StrategyParam prevParam = params.get(prevParamNumber);
                    prevParam.setValue(prevParam.getValue() + prevParam.getStep());
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
            BackTestFileReader backTestFileReader = new BackTestFileReader(optimizerDialog.getFileName(), optimizerDialog.getDateFilter());
            optimizerDialog.setProgress("Loading historical data file...");
            snapshots = backTestFileReader.load(optimizerDialog);
            snapshotCount = snapshots.size();

            optimizerDialog.setProgress("Starting optimization ...");
            progressExecutor.scheduleWithFixedDelay(new ProgressRunner(), 0, 1, TimeUnit.SECONDS);
            resultsTableExecutor.scheduleWithFixedDelay(new ResultsTableRunner(), 0, 30, TimeUnit.SECONDS);
            long start = System.currentTimeMillis();
            optimize();
            long end = System.currentTimeMillis();
            progressExecutor.shutdownNow();
            resultsTableExecutor.shutdownNow();

            optimizerDialog.setResults(optimizationResults);

            if (!cancelled) {
                optimizerDialog.setProgress("Saving optimization results ...");
                saveToFile();
                long totalTimeInSecs = (end - start) / 1000;
                showProgress(totalSteps, "Optimization");
                MessageDialog.showMessage("Optimization completed successfully in " + totalTimeInSecs + " seconds.");
            }
        } catch (Throwable t) {
            MessageDialog.showException(t);
        } finally {
            progressExecutor.shutdownNow();
            resultsTableExecutor.shutdownNow();
            optimizationExecutor.shutdownNow();
            optimizerDialog.signalCompleted();
        }
    }
}
