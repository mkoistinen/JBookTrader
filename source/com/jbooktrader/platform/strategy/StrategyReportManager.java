package com.jbooktrader.platform.strategy;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.position.*;
import com.jbooktrader.platform.report.*;
import com.jbooktrader.platform.util.*;

import java.text.*;
import java.util.*;

/**
 * Strategy report manager
 */
public class StrategyReportManager {
    private final List<String> strategyReportHeaders;
    private final Strategy strategy;
    private final DecimalFormat df2, df5;
    private final SimpleDateFormat sdf;
    private final List<Object> strategyReportColumns;
    private final PositionManager positionManager;
    private final PerformanceManager performanceManager;
    private Report strategyReport;

    public StrategyReportManager(Strategy strategy) {
        this.strategy = strategy;
        positionManager = strategy.getPositionManager();
        performanceManager = strategy.getPerformanceManager();

        df2 = NumberFormatterFactory.getNumberFormatter(2);
        df5 = NumberFormatterFactory.getNumberFormatter(5);
        sdf = new SimpleDateFormat("HH:mm:ss MM/dd/yy z");
        sdf.setTimeZone(strategy.getTradingSchedule().getTimeZone());


        strategyReportColumns = new ArrayList<Object>();
        strategyReportHeaders = new ArrayList<String>();
        strategyReportHeaders.add("Time & Date");
        strategyReportHeaders.add("Trade #");
        strategyReportHeaders.add("Best Bid");
        strategyReportHeaders.add("Best Ask");
        strategyReportHeaders.add("Position");
        strategyReportHeaders.add("Avg Fill Price");
        strategyReportHeaders.add("Commission");
        strategyReportHeaders.add("Trade Net Profit");
        strategyReportHeaders.add("Total Net Profit");

    }

    public void report() {
        if (strategyReport == null) {
            strategyReport = new Report(strategy.getName());
            strategyReport.report(strategyReportHeaders);
        }

        MarketSnapshot marketSnapshot = strategy.getMarketBook().getLastMarketSnapshot();
        boolean isCompletedTrade = performanceManager.getIsCompletedTrade();

        strategyReportColumns.clear();
        strategyReportColumns.add(isCompletedTrade ? performanceManager.getTrades() : "--");
        strategyReportColumns.add(df5.format(marketSnapshot.getBestBid()));
        strategyReportColumns.add(df5.format(marketSnapshot.getBestAsk()));
        strategyReportColumns.add(positionManager.getPosition());
        strategyReportColumns.add(df5.format(positionManager.getAvgFillPrice()));
        strategyReportColumns.add(df2.format(performanceManager.getTradeCommission()));
        strategyReportColumns.add(isCompletedTrade ? df2.format(performanceManager.getTradeProfit()) : "--");
        strategyReportColumns.add(df2.format(performanceManager.getNetProfit()));


        for (ChartableIndicator chartableIndicator : strategy.getIndicators()) {
            strategyReportColumns.add(df2.format(chartableIndicator.getIndicator().getValue()));
        }

        strategyReport.report(strategyReportColumns, sdf.format(strategy.getTime()));
    }


    public void addHeader(Indicator indicator) {
        strategyReportHeaders.add(indicator.getClass().getSimpleName());
    }

}
