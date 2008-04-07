package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.backtest.*;
import com.jbooktrader.platform.bar.*;
import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.performance.*;
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
    private static final String LINE_SEP = System.getProperty("line.separator");
    protected static final long MAX_HISTORY_PERIOD = 4 * 60 * 60 * 1000L;// 4 hours
    private static final int MAX_SIZE = 24 * 60;
    private static final long MAX_STRATEGIES = 50000L;
    private static final int MAX_RESULTS = 5000;
    private static final long UPDATE_FREQUENCY = 1000000L;// lines
    protected final List<Result> results;
    protected final OptimizerDialog optimizerDialog;
    private final NumberFormat nf2;
    protected boolean cancelled;
    private ResultComparator resultComparator;
    protected final StrategyParams strategyParams;
    private final String strategyName;
    private ComputationalTimeEstimator timeEstimator;
    protected final Constructor<?> strategyConstructor;
    private final TradingSchedule tradingSchedule;
    private BackTestFileReader backTestFileReader;
    protected int lineCount;
    protected MarketBook marketBook;
    protected PriceHistory priceHistory;
    private final int minTrades;
    private long completedSteps, totalSteps;

    OptimizerRunner(OptimizerDialog optimizerDialog, Strategy strategy, StrategyParams params) throws ClassNotFoundException, NoSuchMethodException {
        this.optimizerDialog = optimizerDialog;
        this.strategyName = strategy.getName();
        this.strategyParams = params;
        tradingSchedule = strategy.getTradingSchedule();
        results = new ArrayList<Result>();
        nf2 = NumberFormatterFactory.getNumberFormatter(2);
        Class<?> clazz = Class.forName(strategy.getClass().getName());
        Class<?>[] parameterTypes = new Class[]{StrategyParams.class, MarketBook.class, PriceHistory.class};
        strategyConstructor = clazz.getConstructor(parameterTypes);
        resultComparator = new ResultComparator(optimizerDialog.getSortCriteria());
        marketBook = new MarketBook();
        priceHistory = new PriceHistory();
        minTrades = optimizerDialog.getMinTrades();
    }

    protected abstract void optimize() throws JBookTraderException;

    void setTotalSteps(long totalSteps) {
        this.totalSteps = totalSteps;
        if (timeEstimator == null) {
            timeEstimator = new ComputationalTimeEstimator(System.currentTimeMillis(), totalSteps);
        }
        timeEstimator.setTotalIterations(totalSteps);
    }


    void execute(List<Strategy> strategies) throws JBookTraderException {
        backTestFileReader.reset();
        marketBook.getAll().clear();

        MarketDepth marketDepth;
        while ((marketDepth = backTestFileReader.getNextMarketDepth()) != null) {
            priceHistory.update(marketDepth);
            if (priceHistory.size() > MAX_SIZE) {
                priceHistory.getAll().removeFirst();
            }

            marketBook.add(marketDepth);
            long time = marketDepth.getTime();
            boolean inSchedule = tradingSchedule.contains(time);

            for (Strategy strategy : strategies) {
                strategy.setTime(time);
                strategy.updateIndicators();
                if (strategy.hasValidIndicators()) {
                    strategy.onBookChange();
                }

                if (!inSchedule) {
                    strategy.closePosition();// force flat position
                }

                strategy.getPositionManager().trade();

                completedSteps++;
                if (completedSteps % UPDATE_FREQUENCY == 0) {
                    showFastProgress(completedSteps, totalSteps, "Optimizing");
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
                showProgress(completedSteps, totalSteps, "Optimizing");
            }
        }
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

    private void showProgress(long counter, long numberOfTasks, String text) {
        Collections.sort(results, resultComparator);
        while (results.size() > MAX_RESULTS) {
            results.remove(results.size() - 1);
        }
        optimizerDialog.setResults(results);
        String remainingTime = timeEstimator.getTimeLeft(counter);
        optimizerDialog.setProgress(counter, numberOfTasks, text, remainingTime);
    }

    private void showFastProgress(long counter, long numberOfTasks, String text) {
        String remainingTime = (counter == numberOfTasks) ? "00:00:00" : timeEstimator.getTimeLeft(counter);
        optimizerDialog.setProgress(counter, numberOfTasks, text, remainingTime);
    }


    LinkedList<StrategyParams> getTasks(StrategyParams params) throws JBookTraderException {
        for (StrategyParam param : params.getAll()) {
            param.setValue(param.getMin());
        }

        LinkedList<StrategyParams> tasks = new LinkedList<StrategyParams>();

        boolean allTasksAssigned = false;
        while (!allTasksAssigned) {
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

        int numberOfTasks = tasks.size();
        if (numberOfTasks > MAX_STRATEGIES) {
            String message = "The range of parameters for this optimization run requires running " + numberOfTasks + " strategies." + LINE_SEP;
            message += "The maximum number of strategies that can run simultaneously is " + MAX_STRATEGIES + "." + LINE_SEP;
            message += "Reduce the range of parameters." + LINE_SEP;
            throw new JBookTraderException(message);
        }

        return tasks;
    }

    public void run() {
        try {
            results.clear();
            optimizerDialog.setResults(results);
            optimizerDialog.enableProgress();
            optimizerDialog.showProgress("Scanning historical data file...");
            backTestFileReader = new BackTestFileReader(optimizerDialog.getFileName());
            lineCount = backTestFileReader.getTotalLineCount();

            if (cancelled) {
                return;
            }

            optimizerDialog.showProgress("Starting optimization...");
            long start = System.currentTimeMillis();

            optimize();

            if (!cancelled) {
                showFastProgress(100, 100, "Optimization");
                saveToFile();
                long totalTimeInSecs = (System.currentTimeMillis() - start) / 1000;
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
