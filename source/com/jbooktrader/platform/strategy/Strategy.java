package com.jbooktrader.platform.strategy;

import com.ib.client.Contract;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.StrategyParams;
import com.jbooktrader.platform.position.PositionManager;
import com.jbooktrader.platform.report.Report;
import com.jbooktrader.platform.schedule.TradingSchedule;

import java.text.*;
import java.util.*;

/**
 * Base class for all classes that implement trading strategies.
 */

public abstract class Strategy {
    private static final String NOT_APPLICABLE = "N/A";

    private final List<String> strategyReportHeaders;
    private StrategyParams params;


    protected final MarketBook marketBook;
    private final MarketDepth marketDepth;
    private int Id;
    private final DecimalFormat nf2, nf5;
    private Report strategyReport;
    private final Report eventReport;
    private final List<Object> strategyReportColumns = new ArrayList<Object>();

    private boolean isActive;
    private long time;
    private Contract contract;


    private TradingSchedule tradingSchedule;
    private final List<ChartableIndicator> indicators;
    private PositionManager positionManager;
    private final String name;
    private int position;
    private boolean hasValidIndicators;
    private final SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss.SSS MM/dd/yy z");


    /**
     * Framework calls this method when order book changes.
     */
    public void onBookChange() {
    }

    /**
     * Framework calls this method to obtain strategy parameter ranges.
     */
    public abstract StrategyParams initParams();

    public Strategy() {
        strategyReportHeaders = new ArrayList<String>();
        strategyReportHeaders.add("Time & Date");
        strategyReportHeaders.add("Trade #");
        strategyReportHeaders.add("Bid");
        strategyReportHeaders.add("Ask");
        strategyReportHeaders.add("Position");
        strategyReportHeaders.add("Avg Fill Price");
        strategyReportHeaders.add("Commission");
        strategyReportHeaders.add("Trade P&L");
        strategyReportHeaders.add("Total P&L");

        name = getClass().getSimpleName();
        indicators = new ArrayList<ChartableIndicator>();
        params = new StrategyParams();


        marketDepth = new MarketDepth();
        marketBook = new MarketBook();

        nf2 = (DecimalFormat) NumberFormat.getNumberInstance();
        nf2.setMaximumFractionDigits(2);
        nf2.setGroupingUsed(false);
        nf5 = (DecimalFormat) NumberFormat.getNumberInstance();
        nf5.setMaximumFractionDigits(5);
        nf5.setGroupingUsed(false);

        eventReport = Dispatcher.getReporter();
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

    public void setIActive(boolean isActive) {
        this.isActive = isActive;
    }


    public boolean hasValidIndicators() {
        return hasValidIndicators;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
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
        strategyReportColumns.clear();
        MarketDepth marketDepth = marketBook.getLastMarketDepth();
        strategyReportColumns.add(positionManager.getTrades());
        strategyReportColumns.add(nf5.format(marketDepth.getBestBid()));
        strategyReportColumns.add(nf5.format(marketDepth.getBestAsk()));
        strategyReportColumns.add(positionManager.getPosition());
        strategyReportColumns.add(nf5.format(positionManager.getAvgFillPrice()));
        strategyReportColumns.add(nf2.format(positionManager.getCommission()));
        strategyReportColumns.add(nf2.format(positionManager.getProfitAndLoss()));
        strategyReportColumns.add(nf2.format(positionManager.getTotalProfitAndLoss()));

        for (ChartableIndicator chartableIndicator : indicators) {
            String formattedValue = NOT_APPLICABLE;
            if (!chartableIndicator.isEmpty()) {
                synchronized (nf2) {
                    formattedValue = nf2.format(chartableIndicator.getIndicator().getValue());
                }
            }
            strategyReportColumns.add(formattedValue);
        }

        strategyReport.report(strategyReportColumns, df.format(getTime()));
    }


    public void setParams(StrategyParams params) {
        this.params = params;
    }

    public StrategyParams getParams() {
        return params;
    }

    public PositionManager getPositionManager() {
        return positionManager;
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


    protected void addIndicator(String name, Indicator indicator, int chartIndex) {
        ChartableIndicator chartableIndicator = new ChartableIndicator(name, indicator, chartIndex);
        indicators.add(chartableIndicator);
        strategyReportHeaders.add(chartableIndicator.getName());
    }

    public List<ChartableIndicator> getIndicators() {
        return indicators;
    }

    protected void setStrategy(Contract contract, TradingSchedule tradingSchedule, int multiplier, double commission) throws JBookTraderException {
        this.contract = contract;
        this.tradingSchedule = tradingSchedule;
        df.setTimeZone(tradingSchedule.getTimeZone());
        positionManager = new PositionManager(this, multiplier, commission);
    }

    public MarketDepth getLastMarketDepth() {
        return marketBook.getLastMarketDepth();
    }

    public MarketDepth getMarketDepth() {
        return marketDepth;
    }


    public MarketBook getMarketBook() {
        return marketBook;
    }

    public void setId(int Id) {
        this.Id = Id;
    }

    public int getId() {
        return Id;
    }

    public Contract getContract() {
        return contract;
    }

    public String getName() {
        return name;
    }

    public void updateIndicators() {
        hasValidIndicators = true;
        for (ChartableIndicator chartableIndicator : indicators) {
            Indicator indicator = chartableIndicator.getIndicator();
            try {
                indicator.calculate();
                indicator.addToHistory(indicator.getTime(), indicator.getValue());
            } catch (ArrayIndexOutOfBoundsException aie) {
                hasValidIndicators = false;
                // This exception will occur if book size is insufficient to calculate
                // the indicator.
            } catch (Exception e) {
                hasValidIndicators = false;
                eventReport.report(e);
            }
        }
    }


    public void report(String message) {
        strategyReportColumns.clear();
        strategyReportColumns.add(message);
        strategyReport.report(strategyReportColumns, df.format(getTime()));
    }
}
