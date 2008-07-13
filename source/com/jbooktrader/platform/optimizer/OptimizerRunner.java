package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.backtest.*;
import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.position.*;
import com.jbooktrader.platform.report.*;
import com.jbooktrader.platform.schedule.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;

import java.io.*;
import java.lang.reflect.*;
import java.text.*;
import java.util.*;

/**
 * Runs a trading strategy in the optimizer mode using a data file containing
 * historical market depth.
 */
public abstract class OptimizerRunner implements Runnable {
    private static final int MAX_RESULTS = 10000; // max number of rows in the "optimization results" table
    private static final long UPDATE_FREQUENCY = 4000000L; // lines

    private final NumberFormat nf2, nf0;
    private final String strategyName;
    private final TradingSchedule tradingSchedule;
    private final int minTrades;
    private final OptimizerDialog optimizerDialog;
    private ResultComparator resultComparator;
    private ComputationalTimeEstimator timeEstimator;
    private BackTestFileReader backTestFileReader;
    private long completedSteps;

    protected final List<OptimizationResult> optimizationResults;
    protected final StrategyParams strategyParams;
    protected final Constructor<?> strategyConstructor;
    protected boolean cancelled;
    protected int lineCount;
    protected MarketBook marketBook;

    OptimizerRunner(OptimizerDialog optimizerDialog, Strategy strategy, StrategyParams params) throws ClassNotFoundException, NoSuchMethodException {
        this.optimizerDialog = optimizerDialog;
        this.strategyName = strategy.getName();
        this.strategyParams = params;
        tradingSchedule = strategy.getTradingSchedule();
        optimizationResults = new ArrayList<OptimizationResult>();
        nf2 = NumberFormatterFactory.getNumberFormatter(2);
        nf0 = NumberFormatterFactory.getNumberFormatter(0);
        Class<?> clazz = Class.forName(strategy.getClass().getName());
        Class<?>[] parameterTypes = new Class[]{StrategyParams.class, MarketBook.class};
        strategyConstructor = clazz.getConstructor(parameterTypes);
        resultComparator = new ResultComparator(optimizerDialog.getSortCriteria());
        marketBook = new MarketBook();
        minTrades = optimizerDialog.getMinTrades();
    }

    protected abstract void optimize() throws JBookTraderException;

    void setTotalSteps(long totalSteps) {
        if (timeEstimator == null) {
            timeEstimator = new ComputationalTimeEstimator(System.currentTimeMillis(), totalSteps);
        }
        timeEstimator.setTotalIterations(totalSteps);
    }


    void execute(List<Strategy> strategies, int count, long totalSteps) throws JBookTraderException {
        String progressText = "Optimizing";
        if (count > 0) {
            progressText += " " + count + " strategies";
        }

        marketBook.getAll().clear();
        setTotalSteps(totalSteps);

        for (MarketDepth marketDepth : backTestFileReader.getAll()) {
            marketBook.add(marketDepth);

            long time = marketDepth.getTime();
            boolean inSchedule = tradingSchedule.contains(time);

            for (Strategy strategy : strategies) {
                PerformanceManager performanceManager = strategy.getPerformanceManager();
                PositionManager positionManager = strategy.getPositionManager();
                performanceManager.update(marketDepth.getMidPrice(), positionManager.getPosition());
                strategy.setTime(time);
                strategy.updateIndicators();
                if (strategy.hasValidIndicators()) {
                    strategy.onBookChange();
                }

                if (!inSchedule) {
                    strategy.closePosition();// force flat position
                }

                positionManager.trade();

                completedSteps++;
                if (completedSteps % UPDATE_FREQUENCY == 0) {
                    showFastProgress(completedSteps, totalSteps, progressText);
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
                OptimizationResult optimizationResult = new OptimizationResult(strategy.getParams(), performanceManager);
                optimizationResults.add(optimizationResult);
            }
        }

        showResults();
    }


    public void cancel() {
        cancelled = true;
    }

    private void saveToFile() throws IOException, JBookTraderException {
        if (optimizationResults.size() == 0) {
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
        Report.disable();
    }

    private void showResults() {
        Collections.sort(optimizationResults, resultComparator);
        while (optimizationResults.size() > MAX_RESULTS) {
            optimizationResults.remove(optimizationResults.size() - 1);
        }
        optimizerDialog.setResults(optimizationResults);
    }

    private void showFastProgress(long counter, long numberOfTasks, String text) {
        String remainingTime = (counter == numberOfTasks) ? "00:00:00" : timeEstimator.getTimeLeft(counter);
        optimizerDialog.setProgress(counter, numberOfTasks, text, remainingTime);
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
            optimizerDialog.setResults(optimizationResults);
            optimizerDialog.enableProgress();
            optimizerDialog.showProgress("Scanning historical data file...");
            backTestFileReader = new BackTestFileReader(optimizerDialog.getFileName());
            backTestFileReader.load();
            lineCount = backTestFileReader.getAll().size();

            if (cancelled) {
                return;
            }

            long start = System.currentTimeMillis();
            optimizerDialog.showProgress("Starting optimization ...");
            optimize();

            if (!cancelled) {
                optimizerDialog.showProgress("Saving optimization results ...");
                saveToFile();
                long totalTimeInSecs = (System.currentTimeMillis() - start) / 1000;
                showFastProgress(100, 100, "Optimization");
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
