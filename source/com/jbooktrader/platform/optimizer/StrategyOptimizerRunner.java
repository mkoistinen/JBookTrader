package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.backtest.BackTestFileReader;
import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.performance.PerformanceManager;
import com.jbooktrader.platform.position.PositionManager;
import com.jbooktrader.platform.report.Report;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.util.*;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.text.NumberFormat;
import java.util.*;

/**
 * Runs a trading strategy in the optimizer mode using a data file containing
 * historical market depth.
 */
public class StrategyOptimizerRunner implements Runnable {
    private static final long MAX_HISTORY_PERIOD = 24 * 60 * 60 * 1000L; // 24 hours
    private static final int SUBTASK_SIZE = 1000;
    private static final long MAX_ITERATIONS = 50000000L;
    private static final int MAX_RESULTS = 5000;
    private static final long UPDATE_FREQUENCY = 1000000L;// lines
    private final List<Result> results;
    private final OptimizerDialog optimizerDialog;
    private final NumberFormat nf2;
    private boolean cancelled;
    private ResultComparator resultComparator;
    private final StrategyParams strategyParams;
    private final String strategyName;
    private ComputationalTimeEstimator timeEstimator;
    private final Constructor<?> strategyConstructor;

    public StrategyOptimizerRunner(OptimizerDialog optimizerDialog, Strategy strategy) throws ClassNotFoundException, NoSuchMethodException {

        this.optimizerDialog = optimizerDialog;
        this.strategyName = strategy.getName();
        this.strategyParams = strategy.getParams();
        results = Collections.synchronizedList(new ArrayList<Result>());
        nf2 = NumberFormatterFactory.getNumberFormatter(2);
        Class<?> clazz = Class.forName(strategy.getClass().getName());
        Class<?>[] parameterTypes = new Class[]{StrategyParams.class, MarketBook.class};
        strategyConstructor = clazz.getConstructor(parameterTypes);
    }

    public void cancel() {
        cancelled = true;
    }

    private void saveToFile() throws IOException, JBookTraderException {
        if (results.size() == 0) {
            return;
        }

        Report.enable();
        String fileName = strategyName + "Optimizer";
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

            columns.add(nf2.format(result.getNetProfit()));
            columns.add(nf2.format(result.getMaxDrawdown()));
            columns.add(nf2.format(result.getTrades()));
            columns.add(nf2.format(result.getProfitFactor()));
            columns.add(nf2.format(result.getTrueKelly()));

            optimizerReport.report(columns);
        }
        Report.disable();
    }

    private void showProgress(long counter, int numberOfTasks, String text) {
        synchronized (results) {
            Collections.sort(results, resultComparator);

            while (results.size() > MAX_RESULTS) {
                results.remove(results.size() - 1);
            }

            optimizerDialog.setResults(results);
        }

        String remainingTime = timeEstimator.getTimeLeft(counter);
        optimizerDialog.setProgress(counter, numberOfTasks, text, remainingTime);
    }

    public void run() {
        try {

            optimizerDialog.enableProgress();
            optimizerDialog.showProgress("Scanning historical data file...");
            BackTestFileReader backTestFileReader = new BackTestFileReader(optimizerDialog.getFileName());
            int lineCount = backTestFileReader.getTotalLineCount();

            if (cancelled) {
                return;
            }

            String errorMsg = backTestFileReader.getError();
            if (errorMsg != null) {
                throw new JBookTraderException(errorMsg);
            }

            for (StrategyParam param : strategyParams.getAll()) {
                param.setValue(param.getMin());
            }

            optimizerDialog.enableProgress();
            int minTrades = optimizerDialog.getMinTrades();
            resultComparator = new ResultComparator(optimizerDialog.getSortCriteria());

            boolean allTasksAssigned = false;
            cancelled = false;


            LinkedList<StrategyParams> tasks = new LinkedList<StrategyParams>();
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


            timeEstimator = new ComputationalTimeEstimator(System.currentTimeMillis(), lineCount);
            optimizerDialog.showProgress("Creating " + tasks.size() + " strategies...");

            ArrayList<ArrayList<Strategy>> subTasks = new ArrayList<ArrayList<Strategy>>();
            ArrayList<Strategy> subtask = new ArrayList<Strategy>();
            subTasks.add(subtask);
            MarketBook marketBook = new MarketBook();
            for (StrategyParams params : tasks) {
                if (subtask.size() >= SUBTASK_SIZE) {
                    subtask = new ArrayList<Strategy>();
                    subTasks.add(subtask);
                }
                Strategy strategy = (Strategy) strategyConstructor.newInstance(params, marketBook);
                strategy.setParams(params);
                subtask.add(strategy);
            }


            String progressText = "Backtesting " + tasks.size() + " strategies: ";
            showProgress(0, lineCount * tasks.size(), progressText);
            long startTime = System.currentTimeMillis();
            timeEstimator = new ComputationalTimeEstimator(startTime, lineCount * tasks.size());
            long completed = 0;

            for (ArrayList<Strategy> strategies : subTasks) {
                backTestFileReader.reset();
                marketBook.getAll().clear();

                MarketDepth marketDepth;
                while ((marketDepth = backTestFileReader.getNextMarketDepth()) != null) {

                    marketBook.add(marketDepth);
                    long time = marketDepth.getTime();
                    boolean inSchedule = strategies.get(0).getTradingSchedule().contains(time);

                    for (Strategy strategy : strategies) {

                        strategy.updateIndicators();
                        if (strategy.hasValidIndicators()) {
                            strategy.onBookChange();
                        }

                        if (!inSchedule) {
                            strategy.closePosition();// force flat position
                        }

                        strategy.getPositionManager().trade();
                        strategy.trim(time - MAX_HISTORY_PERIOD);

                        completed++;
                        if (completed % UPDATE_FREQUENCY == 0) {
                            showProgress(completed, lineCount * tasks.size(), progressText);
                        }
                        if (cancelled) {
                            return;
                        }
                    }
                }

                for (Strategy strategy : strategies) {
                    strategy.closePosition();
                    strategy.getPositionManager().trade();

                    PerformanceManager performanceManager = strategy.getPerformanceManager();
                    int trades = performanceManager.getTrades();

                    if (trades >= minTrades) {
                        Result result = new Result(strategy.getParams(), performanceManager);
                        results.add(result);
                        showProgress(completed, lineCount * tasks.size(), progressText);
                    }
                }

                strategies.clear();
            }

            showProgress(completed, lineCount * tasks.size(), progressText);
            long totalTimeInSecs = (System.currentTimeMillis() - startTime) / 1000;
            saveToFile();
            MessageDialog.showMessage(optimizerDialog, "Optimization completed successfully in " + totalTimeInSecs + " seconds.");
        } catch (Throwable t) {
            Dispatcher.getReporter().report(t);
            MessageDialog.showError(optimizerDialog, t.toString());
        } finally {
            optimizerDialog.signalCompleted();
        }
    }
}
