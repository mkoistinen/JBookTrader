package com.jbooktrader.platform.strategy;

import com.ib.client.Contract;
import com.jbooktrader.platform.commission.Commission;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.StrategyParams;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.position.*;
import com.jbooktrader.platform.report.Report;
import com.jbooktrader.platform.schedule.TradingSchedule;
import com.jbooktrader.platform.util.NumberFormatterFactory;

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
    private PerformanceManager performanceManager;
    private final String name;
    private int position;
    private boolean hasValidIndicators;
    private final SimpleDateFormat df;
    private final boolean isOptimizationMode;


    /**
     * Framework calls this method when order book changes.
     */
    public void onBookChange() {
    }

    /**
     * Framework calls this method to obtain strategy parameter ranges.
     */
    public abstract StrategyParams initParams();

    public Strategy(MarketBook marketBook) {
        this.marketBook = marketBook;

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

        nf2 = NumberFormatterFactory.getNumberFormatter(2);
        nf5 = NumberFormatterFactory.getNumberFormatter(5);
        df = new SimpleDateFormat("HH:mm:ss.SSS MM/dd/yy z");

        eventReport = Dispatcher.getReporter();
        isOptimizationMode = (Dispatcher.getMode() == Dispatcher.Mode.OPTIMIZATION);
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
        strategyReportColumns.clear();
        MarketDepth marketDepth = marketBook.getLastMarketDepth();
        strategyReportColumns.add(performanceManager.getTrades());
        strategyReportColumns.add(nf5.format(marketDepth.getBestBid()));
        strategyReportColumns.add(nf5.format(marketDepth.getBestAsk()));
        strategyReportColumns.add(positionManager.getPosition());
        strategyReportColumns.add(nf5.format(positionManager.getAvgFillPrice()));
        strategyReportColumns.add(nf2.format(performanceManager.getTradeCommission()));
        strategyReportColumns.add(nf2.format(performanceManager.getTradeProfit()));
        strategyReportColumns.add(nf2.format(performanceManager.getNetProfit()));

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

    public void trim(long time) {
        for (ChartableIndicator chartableIndicator : indicators) {
            LinkedList<IndicatorValue> indicatorHistory = chartableIndicator.getIndicator().getHistory();
            while (!indicatorHistory.isEmpty() && indicatorHistory.getFirst().getTime() < time) {
                indicatorHistory.removeFirst();
            }
        }

        LinkedList<MarketDepth> marketDepths = marketBook.getAll();
        while (!marketDepths.isEmpty() && marketDepths.getFirst().getTime() < time) {
            marketDepths.removeFirst();
        }

        LinkedList<Position> positionHistory = positionManager.getPositionsHistory();
        while (!positionHistory.isEmpty() && positionHistory.getFirst().getTime() < time) {
            positionHistory.removeFirst();
        }

        LinkedList<ProfitAndLoss> pnlHistory = performanceManager.getProfitAndLossHistory().getHistory();
        while (!pnlHistory.isEmpty() && pnlHistory.getFirst().getTime() < time) {
            pnlHistory.removeFirst();
        }
    }

    protected void addIndicator(String name, Indicator indicator, int chartIndex) {
        ChartableIndicator chartableIndicator = new ChartableIndicator(name, indicator, chartIndex);
        indicators.add(chartableIndicator);
        strategyReportHeaders.add(chartableIndicator.getName());
    }

    public List<ChartableIndicator> getIndicators() {
        return indicators;
    }

    protected void setStrategy(Contract contract, TradingSchedule tradingSchedule, int multiplier, Commission commission) throws JBookTraderException {
        this.contract = contract;
        this.tradingSchedule = tradingSchedule;
        df.setTimeZone(tradingSchedule.getTimeZone());
        performanceManager = new PerformanceManager(this, multiplier, commission);
        positionManager = new PositionManager(this);

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

    public void updateIndicators() throws JBookTraderException {
        hasValidIndicators = true;
        for (ChartableIndicator chartableIndicator : indicators) {
            Indicator indicator = chartableIndicator.getIndicator();
            try {
                indicator.calculate();
                if (!isOptimizationMode) {
                    indicator.addToHistory(indicator.getTime(), indicator.getValue());
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


    public void report(String message) {
        strategyReportColumns.clear();
        strategyReportColumns.add(message);
        strategyReport.report(strategyReportColumns, df.format(getTime()));
    }
}
