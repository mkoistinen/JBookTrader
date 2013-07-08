package com.jbooktrader.platform.strategy;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.position.*;
import com.jbooktrader.platform.report.*;
import com.jbooktrader.platform.util.format.*;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Strategy report manager
 *
 * @author Eugene Kononov
 */
public class StrategyReportManager {
    private final List<String> strategyReportHeaders;
    private final Strategy strategy;
    private final DecimalFormat df2, df5;
    private final SimpleDateFormat dateFormat, timeFormat;
    private final List<String> strategyReportColumns;
    private final PositionManager positionManager;
    private final PerformanceManager performanceManager;
    private StrategyReport strategyReport;

    public StrategyReportManager(Strategy strategy) {
        this.strategy = strategy;
        positionManager = strategy.getPositionManager();
        performanceManager = strategy.getPerformanceManager();

        df2 = NumberFormatterFactory.getNumberFormatter(2);
        df5 = NumberFormatterFactory.getNumberFormatter(5);
        TimeZone timeZone = strategy.getTradingSchedule().getTimeZone();
        dateFormat = new SimpleDateFormat("MM/dd/yy");
        dateFormat.setTimeZone(timeZone);
        timeFormat = new SimpleDateFormat("HH:mm:ss.SSS z");
        timeFormat.setTimeZone(timeZone);


        strategyReportColumns = new ArrayList<>();
        strategyReportHeaders = new ArrayList<>();
        strategyReportHeaders.add("Date");
        strategyReportHeaders.add("Time");
        strategyReportHeaders.add("Trade #");
        strategyReportHeaders.add("Position");
        strategyReportHeaders.add("Average Fill");
        strategyReportHeaders.add("Expected Fill");
        strategyReportHeaders.add("Commission");
        strategyReportHeaders.add("Trade Profit");
        strategyReportHeaders.add("Total Profit");
    }

    public void report() {
        if (strategyReport == null) {
            try {
                strategyReport = new StrategyReport(strategy.getName());
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
            strategyReport.reportHeaders(strategyReportHeaders);
        }

        boolean isCompletedTrade = performanceManager.getIsCompletedTrade();

        strategyReportColumns.clear();
        strategyReportColumns.add(isCompletedTrade ? String.valueOf(performanceManager.getTrades()) : "&nbsp;");
        strategyReportColumns.add(String.valueOf(positionManager.getCurrentPosition()));
        double averageFillPrice = positionManager.getAvgFillPrice();
        double expectedFillPrice = positionManager.getExpectedFillPrice();
        String decoratedExpectedFillPrice = df5.format(expectedFillPrice);
        if (averageFillPrice != expectedFillPrice) {
            decoratedExpectedFillPrice = "<b>" + decoratedExpectedFillPrice + "</b>";
        }
        strategyReportColumns.add(df5.format(averageFillPrice));
        strategyReportColumns.add(decoratedExpectedFillPrice);
        strategyReportColumns.add(df2.format(performanceManager.getTradeCommission()));
        strategyReportColumns.add(isCompletedTrade ? df2.format(performanceManager.getTradeProfit()) : "&nbsp;");
        strategyReportColumns.add(isCompletedTrade ? df2.format(performanceManager.getNetProfit()) : "&nbsp;");

        Mode mode = Dispatcher.getInstance().getMode();
        boolean useNTPTime = (mode == Mode.ForwardTest || mode == Mode.Trade || mode == Mode.ForceClose);

        long now = useNTPTime ? Dispatcher.getInstance().getNTPClock().getTime() : strategy.getMarketBook().getSnapshot().getTime();
        String date = dateFormat.format(now);
        String time = timeFormat.format(now);
        strategyReport.report(strategyReportColumns, date, time);
    }

}
