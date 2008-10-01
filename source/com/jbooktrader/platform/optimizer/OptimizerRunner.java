package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.backtest.*;
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
    protected final int STRATEGIES_PER_PROCESSOR = 250;
    private final OptimizerProgressIndicator optimizerProgressIndicator;
    protected final LinkedList<OptimizationResult> optimizationResults;
    protected final StrategyParams strategyParams;
    protected final Constructor<?> strategyConstructor;
    protected int lineCount;
    protected boolean cancelled;
    protected final int availableProcessors;

    private static final int MAX_RESULTS = 10000;// max number of rows in the "optimization results" table
    private final ScheduledExecutorService progressExecutor;
    private final NumberFormat nf2, nf0;
    private final String strategyName;
    private final int minTrades;
    private final String dataFileName;
    private ResultComparator resultComparator;
    private ComputationalTimeEstimator timeEstimator;
    private BackTestFileReader backTestFileReader;
    private long completedSteps;
    private long totalSteps, totalStrategies;
    private ExecutorService executor;

    class ProgressRunner implements Runnable {
        public void run() {
            if (completedSteps > 0) {
                showFastProgress(completedSteps, "Optimizing " + totalStrategies + " strategies");
            }
        }
    }

    OptimizerRunner(OptimizerProgressIndicator optimizerProgressIndicator, Strategy strategy, StrategyParams params, String dataFileName, PerformanceMetric sortCriteria, int minTrades) throws JBookTraderException {
        this.optimizerProgressIndicator = optimizerProgressIndicator;
        this.strategyName = strategy.getName();
        this.strategyParams = params;
        optimizationResults = new LinkedList<OptimizationResult>();
        nf2 = NumberFormatterFactory.getNumberFormatter(2);
        nf0 = NumberFormatterFactory.getNumberFormatter(0);
        availableProcessors = Runtime.getRuntime().availableProcessors();

        Class<?> clazz;
        try {
            clazz = Class.forName(strategy.getClass().getName());
        } catch (ClassNotFoundException cnfe) {
            throw new JBookTraderException("Could not find class " + strategy.getClass().getName());
        }
        Class<?>[] parameterTypes = new Class[]{StrategyParams.class};

        try {
            strategyConstructor = clazz.getConstructor(parameterTypes);
        } catch (NoSuchMethodException nsme) {
            throw new JBookTraderException("Could not find strategy constructor for " + strategy.getClass().getName());
        }

        resultComparator = new ResultComparator(sortCriteria);
        this.minTrades = minTrades;
        this.dataFileName = dataFileName;
        progressExecutor = Executors.newSingleThreadScheduledExecutor();
        executor = Executors.newFixedThreadPool(availableProcessors);
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
        this.totalStrategies = totalStrategies;
    }

    public int getMinTrades() {
        return minTrades;
    }

    public BackTestFileReader getBackTestFileReader() {
        return backTestFileReader;
    }

    void execute(List<Strategy> strategies) throws JBookTraderException {

        int size = strategies.size();
        if (size == 0) {
            return;
        }

        int workers = availableProcessors;
        int strategiesPerWorker = size / workers;
        if (size < workers) {
            workers = 1;
            strategiesPerWorker = size;
        }

        int fromIndex = 0;
        Set<Future<List<OptimizationResult>>> set = new HashSet<Future<List<OptimizationResult>>>();

        for (int worker = 0; worker < workers; worker++) {
            int toIndex = fromIndex + strategiesPerWorker;
            if (worker == workers - 1) {
                toIndex = size;
            }

            List<Strategy> workerStrategies = strategies.subList(fromIndex, toIndex);
            Callable<List<OptimizationResult>> callable = new OptimizerWorker(this, workerStrategies);
            Future<List<OptimizationResult>> future = executor.submit(callable);
            set.add(future);
            fromIndex = toIndex;
        }

        for (Future<List<OptimizationResult>> future : set) {
            try {
                List<OptimizationResult> results = future.get();
                optimizationResults.addAll(results);
            } catch (Exception e) {
                throw new JBookTraderException(e);
            }
        }
        showResults();
    }

    public void cancel() {
        optimizerProgressIndicator.showProgress("Stopping optimization...");
        cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    private void saveToFile() throws JBookTraderException {
        if (optimizationResults.size() == 0) {
            return;
        }

        Dispatcher.enableReport();
        String fileName = strategyName + "Optimizer";
        Report optimizerReport = Dispatcher.createReport(fileName);

        optimizerReport.reportDescription("Strategy parameters:");
        for (StrategyParam param : strategyParams.getAll()) {
            optimizerReport.reportDescription(param.toString());
        }
        optimizerReport.reportDescription("Minimum trades for strategy inclusion: " + minTrades);
        optimizerReport.reportDescription("Back data file: " + dataFileName);

        List<String> otpimizerReportHeaders = new ArrayList<String>();
        StrategyParams params = optimizationResults.iterator().next().getParams();
        for (StrategyParam param : params.getAll()) {
            otpimizerReportHeaders.add(param.getName());
        }

        for (PerformanceMetric performanceMetric : PerformanceMetric.values()) {
            otpimizerReportHeaders.add(performanceMetric.getName());
        }
        optimizerReport.report(otpimizerReportHeaders);

        for (OptimizationResult optimizationResult : optimizationResults) {
            params = optimizationResult.getParams();

            List<Object> columns = new ArrayList<Object>();
            for (StrategyParam param : params.getAll()) {
                columns.add(param.getValue());
            }

            columns.add(optimizationResult.getTrades());
            columns.add(nf0.format(optimizationResult.getExposure()));
            columns.add(nf0.format(optimizationResult.getNetProfit()));
            columns.add(nf0.format(optimizationResult.getMaxDrawdown()));
            columns.add(nf2.format(optimizationResult.getProfitFactor()));
            columns.add(nf0.format(optimizationResult.getKellyCriterion()));
            columns.add(nf2.format(optimizationResult.getPerformanceIndex()));

            optimizerReport.report(columns);
        }
        Dispatcher.disableReport();
    }

    private void showResults() {
        Collections.sort(optimizationResults, resultComparator);
        while (optimizationResults.size() > MAX_RESULTS) {
            optimizationResults.removeLast();
        }
        optimizerProgressIndicator.setResults(optimizationResults);
    }

    private void showFastProgress(long counter, String text) {
        String remainingTime = (counter == totalSteps) ? "00:00:00" : timeEstimator.getTimeLeft(counter);
        optimizerProgressIndicator.setProgress(counter, totalSteps, text, remainingTime);
    }

    public synchronized void iterationsCompleted(long iterationsCompleted) {
        completedSteps += iterationsCompleted;
    }

    protected LinkedList<StrategyParams> getTasks(StrategyParams params) {
        for (StrategyParam param : params.getAll()) {
            param.setValue(param.getMin());
        }

        LinkedList<StrategyParams> tasks = new LinkedList<StrategyParams>();

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
            optimizerProgressIndicator.setResults(optimizationResults);
            optimizerProgressIndicator.enableProgress();
            optimizerProgressIndicator.showProgress("Scanning historical data file...");
            backTestFileReader = new BackTestFileReader(dataFileName);
            backTestFileReader.load();
            lineCount = backTestFileReader.getAll().size();

            if (cancelled) {
                return;
            }

            optimizerProgressIndicator.showProgress("Starting optimization ...");
            progressExecutor.scheduleWithFixedDelay(new ProgressRunner(), 0, 1, TimeUnit.SECONDS);
            long start = System.currentTimeMillis();
            optimize();
            long end = System.currentTimeMillis();
            progressExecutor.shutdownNow();
            executor.shutdownNow();

            if (!cancelled) {
                optimizerProgressIndicator.showProgress("Saving optimization results ...");
                saveToFile();
                long totalTimeInSecs = (end - start) / 1000;
                showFastProgress(totalSteps, "Optimization");
                optimizerProgressIndicator.showMessage("Optimization completed successfully in " + totalTimeInSecs + " seconds.");
            }
        } catch (Throwable t) {
            Dispatcher.getReporter().report(t);
            optimizerProgressIndicator.showError(t.toString());
        } finally {
            progressExecutor.shutdownNow();
            optimizerProgressIndicator.signalCompleted();
        }
    }
}
