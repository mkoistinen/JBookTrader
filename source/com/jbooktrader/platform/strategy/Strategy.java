package com.jbooktrader.platform.strategy;

import com.ib.client.*;
import com.jbooktrader.platform.commission.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.position.*;
import com.jbooktrader.platform.report.*;
import com.jbooktrader.platform.schedule.*;
import com.jbooktrader.platform.util.*;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Base class for all classes that implement trading strategies.
 */

public abstract class Strategy {
    private final List<String> strategyReportHeaders;
    private final StrategyParams params;
    private final MarketBook marketBook;
    private final DecimalFormat df2, df5;
    private final SimpleDateFormat sdf;
    private final Report eventReport;
    private final String name;
    private final List<Object> strategyReportColumns;
    private final List<ChartableIndicator> indicators;
    private final boolean isOptimizationMode;
    private Report strategyReport;
    private Contract contract;
    private TradingSchedule tradingSchedule;
    private PositionManager positionManager;
    private PerformanceManager performanceManager;
    private boolean isActive, hasValidIndicators;
    private int position, id;
    private long time;


    /**
     * Framework calls this method when order book changes.
     */
    abstract public void onBookChange();

    /**
     * Framework calls this method to set strategy parameter ranges and values.
     */
    abstract protected void setParams();


    protected Strategy(StrategyParams params, MarketBook marketBook) {
        this.params = params;
        this.marketBook = marketBook;
        if (params.size() == 0) {
            setParams();
        }

        strategyReportColumns = new ArrayList<Object>();
        strategyReportHeaders = new ArrayList<String>();
        strategyReportHeaders.add("Time & Date");
        strategyReportHeaders.add("Trade #");
        strategyReportHeaders.add("Low Price");
        strategyReportHeaders.add("High Price");
        strategyReportHeaders.add("Position");
        strategyReportHeaders.add("Avg Fill Price");
        strategyReportHeaders.add("Commission");
        strategyReportHeaders.add("Trade P&L");
        strategyReportHeaders.add("Total P&L");

        name = getClass().getSimpleName();
        indicators = new ArrayList<ChartableIndicator>();

        df2 = NumberFormatterFactory.getNumberFormatter(2);
        df5 = NumberFormatterFactory.getNumberFormatter(5);
        sdf = new SimpleDateFormat("HH:mm:ss.SSS MM/dd/yy z");

        eventReport = Dispatcher.getReporter();
        isOptimizationMode = (Dispatcher.getMode() == Dispatcher.Mode.Optimization);
    }

    public void setReport(Report strategyReport) {
        this.strategyReport = strategyReport;
    }

    public List<String> getStrategyReportHeaders() {
        return strategyReportHeaders;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }


    public boolean hasValidIndicators() {
        return hasValidIndicators;
    }

    public int getPosition() {
        return position;
    }

    protected void setPosition(int position) {
        this.position = position;
    }

    public void closePosition() {
        position = 0;
        if (positionManager.getPosition() != 0) {
            String msg = "End of trading interval. Closing current position.";
            eventReport.report(getName() + ": " + msg);
        }
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" ").append(name).append(" [");
        sb.append(contract.m_symbol).append("-");
        sb.append(contract.m_secType).append("-");
        sb.append(contract.m_exchange).append("]");

        return sb.toString();
    }


    public void report() {
        MarketDepth marketDepth = marketBook.getLastMarketDepth();
        boolean isCompletedTrade = performanceManager.getIsCompletedTrade();

        strategyReportColumns.clear();
        strategyReportColumns.add(isCompletedTrade ? performanceManager.getTrades() : "--");
        strategyReportColumns.add(df5.format(marketDepth.getLowPrice()));
        strategyReportColumns.add(df5.format(marketDepth.getHighPrice()));
        strategyReportColumns.add(positionManager.getPosition());
        strategyReportColumns.add(df5.format(positionManager.getAvgFillPrice()));
        strategyReportColumns.add(df2.format(performanceManager.getTradeCommission()));
        strategyReportColumns.add(isCompletedTrade ? df2.format(performanceManager.getTradeProfit()) : "--");
        strategyReportColumns.add(df2.format(performanceManager.getNetProfit()));

        for (ChartableIndicator chartableIndicator : indicators) {
            strategyReportColumns.add(df2.format(chartableIndicator.getIndicator().getValue()));
        }

        strategyReport.report(strategyReportColumns, sdf.format(getTime()));
    }


    public StrategyParams getParams() {
        return params;
    }

    protected int getParam(String name) {
        return params.get(name).getValue();
    }

    protected void addParam(String name, int min, int max, int step, int value) {
        params.add(name, min, max, step, value);
    }

    public PositionManager getPositionManager() {
        return positionManager;
    }

    public PerformanceManager getPerformanceManager() {
        return performanceManager;
    }

    public TradingSchedule getTradingSchedule() {
        return tradingSchedule;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    protected void addIndicator(String name, Indicator indicator) {
        ChartableIndicator chartableIndicator = new ChartableIndicator(name, indicator);
        indicators.add(chartableIndicator);
        strategyReportHeaders.add(chartableIndicator.getName());
    }

    public List<ChartableIndicator> getIndicators() {
        return indicators;
    }

    protected void setStrategy(Contract contract, TradingSchedule tradingSchedule, int multiplier, Commission commission) {
        this.contract = contract;
        contract.m_multiplier = String.valueOf(multiplier);
        this.tradingSchedule = tradingSchedule;
        sdf.setTimeZone(tradingSchedule.getTimeZone());
        performanceManager = new PerformanceManager(this, multiplier, commission);
        positionManager = new PositionManager(this);
        marketBook.setName(name);
        marketBook.setTimeZone(tradingSchedule.getTimeZone());
    }

    protected MarketDepth getLastMarketDepth() {
        return marketBook.getLastMarketDepth();
    }

    public MarketBook getMarketBook() {
        return marketBook;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Contract getContract() {
        return contract;
    }

    public String getName() {
        return name;
    }

    public void updateIndicators() throws JBookTraderException {
        hasValidIndicators = true;
        for (ChartableIndicator chartableIndicator : indicators) {
            Indicator indicator = chartableIndicator.getIndicator();
            try {
                double value = indicator.calculate();
                long time = marketBook.getLastMarketDepth().getTime();
                if (!isOptimizationMode) {
                    chartableIndicator.add(time, value);
                }
            } catch (IndexOutOfBoundsException aie) {
                hasValidIndicators = false;
                // This exception will occur if book size is insufficient to calculate
                // the indicator.
            } catch (Exception e) {
                throw new JBookTraderException(e);
            }
        }
    }


    public void process(MarketDepth marketDepth) throws IOException, JBookTraderException {
        if (isActive()) {
            long instant = marketDepth.getTime();
            setTime(instant);
            updateIndicators();
            if (hasValidIndicators()) {
                onBookChange();
            }

            if (!tradingSchedule.contains(instant)) {
                closePosition(); // force flat position
            }

            if (tradingSchedule.approximatelyContains(instant)) {
                marketBook.save(marketDepth);
            }

            positionManager.trade();
            performanceManager.update(marketDepth.getMidPrice(), positionManager.getPosition());
            Dispatcher.fireModelChanged(ModelListener.Event.StrategyUpdate, this);
        }
    }

    public void report(String message) {
        if (strategyReport != null) {
            strategyReportColumns.clear();
            strategyReportColumns.add(message);
            strategyReport.report(strategyReportColumns, sdf.format(getTime()));
        }
    }
}
