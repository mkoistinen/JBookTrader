package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.backtest.BackTestFileReader;
import com.jbooktrader.platform.marketdepth.MarketDepth;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.report.Report;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.util.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Runs a trading strategy in the optimizer mode using a data file containing
 * historical market depth.
 */
public class StrategyOptimizerRunner implements Runnable {
    private static final long MAX_ITERATIONS = 50000000L;
    private static final int MAX_RESULTS = 5000;
    private static final long UPDATE_FREQUENCY = 5000L;// milliseconds
    private final List<Result> results;
    private final OptimizerDialog optimizerDialog;
    private final NumberFormat nf2;
    private boolean cancelled;
    private ResultComparator resultComparator;
    private final StrategyParams strategyParams;
    private ComputationalTimeEstimator timeEstimator;
    private final Constructor<?> strategyConstructor;
    private LinkedList<StrategyParams> tasks;

    public StrategyOptimizerRunner(OptimizerDialog optimizerDialog, Strategy strategy) throws ClassNotFoundException, NoSuchMethodException {

        this.optimizerDialog = optimizerDialog;
        this.strategyParams = strategy.getParams();
        results = Collections.synchronizedList(new ArrayList<Result>());
        nf2 = NumberFormat.getNumberInstance();
        nf2.setMaximumFractionDigits(2);
        nf2.setGroupingUsed(false);
        Class<?> clazz = Class.forName(strategy.getClass().getName());
        Class<?>[] parameterTypes = new Class[]{StrategyParams.class};
        strategyConstructor = clazz.getConstructor(parameterTypes);
    }

    public void cancel() {
        optimizerDialog.showProgress("Stopping running processes...");
        if (tasks != null) {
            tasks.clear();
        }
        cancelled = true;
    }

    private void saveToFile(Strategy strategy) throws IOException, JBookTraderException {
        if (results.size() == 0) {
            return;
        }

        Report.enable();
        String fileName = strategy.getName() + "Optimizer";
        Report optimizerReport = new Report(fileName);

        optimizerReport.reportDescription("Strategy parameters:");
        for (StrategyParam param : strategyParams.getAll()) {
            optimizerReport.reportDescription(param.toString());
        }
        optimizerReport.reportDescription("Minimum trades for strategy inclusion: " + optimizerDialog.getMinTrades());
        optimizerReport.reportDescription("Back data file: " + optimizerDialog.getFileName());

        List<String> otpimizerReportHeaders = new ArrayList<String>();
        StrategyParams params = results.iterator().next().getParams();
        for (StrategyParam param : params.getAll()) {
            otpimizerReportHeaders.add(param.getName());
        }

        otpimizerReportHeaders.add("Total P&L");
        otpimizerReportHeaders.add("Max Drawdown");
        otpimizerReportHeaders.add("Trades");
        otpimizerReportHeaders.add("Profit Factor");
        otpimizerReportHeaders.add("True Kelly");
        optimizerReport.report(otpimizerReportHeaders);

        for (Result result : results) {
            params = result.getParams();

            List<String> columns = new ArrayList<String>();
            for (StrategyParam param : params.getAll()) {
                columns.add(nf2.format(param.getValue()));
            }

            columns.add(nf2.format(result.getTotalProfit()));
            columns.add(nf2.format(result.getMaxDrawdown()));
            columns.add(nf2.format(result.getTrades()));
            columns.add(nf2.format(result.getProfitFactor()));
            columns.add(nf2.format(result.getTrueKelly()));

            optimizerReport.report(columns);
        }
        Report.disable();
    }

    private void showProgress(long counter, int numberOfTasks) {
        synchronized (results) {
            Collections.sort(results, resultComparator);

            while (results.size() > MAX_RESULTS) {
                results.remove(results.size() - 1);
            }

            optimizerDialog.setResults(results);
        }

        String remainingTime = timeEstimator.getTimeLeft(counter);
        optimizerDialog.setProgress(counter, numberOfTasks, "Completed back tests: ", remainingTime);
    }

    private void showLoadProgress(long counter, int totalCount) {
        optimizerDialog.setProgress(counter, totalCount, "Reading historical data file: ");
    }


    public void run() {
        try {

            optimizerDialog.enableProgress();
            BackTestFileReader backTestFileReader = new BackTestFileReader(optimizerDialog.getFileName());
            int lineCount = backTestFileReader.getLineCount();
            backTestFileReader.start();

            while (backTestFileReader.isAlive()) {
                showLoadProgress(backTestFileReader.getLinesRead(), lineCount);
                Thread.sleep(500);
            }

            String errorMsg = backTestFileReader.getError();
            if (errorMsg != null) {
                throw new JBookTraderException(errorMsg);
            }


            List<MarketDepth> marketDepths = backTestFileReader.getMarketDepths();


            Strategy strategy = (Strategy) strategyConstructor.newInstance(new StrategyParams());

            for (StrategyParam param : strategyParams.getAll()) {
                param.setValue(param.getMin());
            }

            optimizerDialog.enableProgress();
            int minTrades = optimizerDialog.getMinTrades();
            resultComparator = new ResultComparator(optimizerDialog.getSortCriteria());

            boolean allTasksAssigned = false;
            cancelled = false;


            tasks = new LinkedList<StrategyParams>();
            optimizerDialog.showProgress("Distributing the tasks...");

            while (!allTasksAssigned) {
                StrategyParams strategyParamsCopy = new StrategyParams(strategyParams);
                tasks.add(strategyParamsCopy);

                StrategyParam lastParam = strategyParams.get(strategyParams.size() - 1);
                lastParam.setValue(lastParam.getValue() + lastParam.getStep());

                for (int paramNumber = strategyParams.size() - 1; paramNumber >= 0; paramNumber--) {
                    StrategyParam param = strategyParams.get(paramNumber);
                    if (param.getValue() > param.getMax()) {
                        param.setValue(param.getMin());
                        if (paramNumber == 0) {
                            allTasksAssigned = true;
                            break;
                        } else {
                            int prevParamNumber = paramNumber - 1;
                            StrategyParam prevParam = strategyParams.get(prevParamNumber);
                            prevParam.setValue(prevParam.getValue() + prevParam.getStep());
                        }
                    }
                }
            }

            int numberOfTasks = tasks.size();
            if (numberOfTasks > MAX_ITERATIONS) {
                optimizerDialog.showMaxIterationsLimit(numberOfTasks, MAX_ITERATIONS);
                return;
            }

            CountDownLatch remainingTasks = new CountDownLatch(numberOfTasks);

            PropertiesHolder properties = PropertiesHolder.getInstance();
            int maxThreads = Integer.valueOf(properties.getProperty("optimizer.maxThreads"));
            for (int thread = 0; thread < maxThreads; thread++) {
                new Thread(new OptimizerWorker(marketDepths, strategyConstructor, tasks, results, minTrades, remainingTasks)).start();
            }

            optimizerDialog.showProgress("Estimating remaining time...");
            long startTime = System.currentTimeMillis();
            timeEstimator = new ComputationalTimeEstimator(startTime, numberOfTasks);

            long remaining;
            do {
                Thread.sleep(UPDATE_FREQUENCY);
                remaining = remainingTasks.getCount();
                showProgress(numberOfTasks - remaining, numberOfTasks);// results in progress
            } while (remaining != 0 && !cancelled);

            if (!cancelled) {
                showProgress(numberOfTasks, numberOfTasks);// final results
                long endTime = System.currentTimeMillis();
                long totalTimeInSecs = (endTime - startTime) / 1000;
                saveToFile(strategy);
                MessageDialog.showMessage(optimizerDialog, "Optimization completed successfully in " + totalTimeInSecs + " seconds.");
            }
        } catch (Throwable t) {
            Dispatcher.getReporter().report(t);
            MessageDialog.showError(optimizerDialog, t.toString());
        } finally {
            optimizerDialog.signalCompleted();
        }
    }
}
