package com.jbooktrader.platform.backtest;


import com.jbooktrader.platform.marketdepth.MarketDepth;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.report.Report;
import com.jbooktrader.platform.strategy.*;

import java.io.IOException;
import java.util.List;

/**
 * Runs a trading strategy in the backtesting mode using a data file containing
 * historical market depth. There is a one-to-one map between the strategy class
 * and the strategy runner. That is, if 5 strategies are selected to run,
 * there will be 5 instances of the StrategyRunner created.
 */
public class BackTestStrategyRunner extends StrategyRunner {
    private final BackTester backTester;

    public BackTestStrategyRunner(Strategy strategy, List<MarketDepth> marketDepths) throws IOException, JBookTraderException {
        super(strategy);
        boolean isOptimizationMode = (Dispatcher.getMode() == Dispatcher.Mode.OPTIMIZATION);

        if (!isOptimizationMode) {
            Dispatcher.getTrader().getAssistant().addStrategy(strategy);
            eventReport.report(strategy.getName() + ": strategy started");
            Report strategyReport = new Report(strategy.getName());
            strategyReport.report(strategy.getStrategyReportHeaders());
            strategy.setReport(strategyReport);
        }
        backTester = new BackTester(strategy, marketDepths);
    }

    @Override
    public void execute() {
        backTester.execute();
    }

}
