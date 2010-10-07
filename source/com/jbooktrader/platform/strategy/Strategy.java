package com.jbooktrader.platform.strategy;

import com.ib.client.*;
import com.jbooktrader.platform.commission.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.model.ModelListener.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.position.*;
import com.jbooktrader.platform.report.*;
import com.jbooktrader.platform.schedule.*;

/**
 * Base class for all classes that implement trading strategies.
 */

public abstract class Strategy implements Comparable<Strategy> {
    private static final long GAP_SIZE = 60 * 60 * 1000;// 1 hour
    private final StrategyParams params;
    private MarketBook marketBook;
    private final EventReport eventReport;
    private final Dispatcher dispatcher;
    private final String name;
    private Contract contract;
    private TradingSchedule tradingSchedule;
    private PositionManager positionManager;
    private PerformanceManager performanceManager;
    private StrategyReportManager strategyReportManager;
    private IndicatorManager indicatorManager;
    private int position;
    private long time;
    private double bidAskSpread;

    /**
     * Framework calls this method when a new snapshot of the limit order book is taken.
     */
    public abstract void onBookSnapshot();

    /**
     * Framework calls this method to set strategy parameter ranges and values.
     */
    protected abstract void setParams();

    protected Strategy(StrategyParams params) {
        this.params = params;
        if (params.size() == 0) {
            setParams();
        }

        name = getClass().getSimpleName();
        dispatcher = Dispatcher.getInstance();
        eventReport = dispatcher.getEventReport();
    }

    public void setMarketBook(MarketBook marketBook) {
        this.marketBook = marketBook;
        indicatorManager.setMarketBook(marketBook);
    }

    public int getPosition() {
        return position;
    }

    protected void setPosition(int position) {
        this.position = position;
    }

    public double getBidAskSpread() {
        return bidAskSpread;
    }

    public void closePosition() {
        position = 0;
        if (positionManager.getPosition() != 0) {
            Mode mode = dispatcher.getMode();
            if (mode == Mode.ForwardTest || mode == Mode.Trade) {
                String msg = "End of trading interval. Closing current position.";
                eventReport.report(name, msg);
            }
        }
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

    public StrategyReportManager getStrategyReportManager() {
        return strategyReportManager;
    }

    public IndicatorManager getIndicatorManager() {
        return indicatorManager;
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

    protected void addIndicator(Indicator indicator) {
        indicatorManager.addIndicator(indicator);
    }

    protected void setStrategy(Contract contract, TradingSchedule tradingSchedule, int multiplier, Commission commission, double bidAskSpread) {
        this.contract = contract;
        contract.m_multiplier = String.valueOf(multiplier);
        this.tradingSchedule = tradingSchedule;
        performanceManager = new PerformanceManager(this, multiplier, commission);
        positionManager = new PositionManager(this);
        strategyReportManager = new StrategyReportManager(this);
        marketBook = dispatcher.getTrader().getAssistant().createMarketBook(this);
        this.bidAskSpread = bidAskSpread;
        indicatorManager = new IndicatorManager();
    }

    public MarketBook getMarketBook() {
        return marketBook;
    }

    public Contract getContract() {
        return contract;
    }

    public String getSymbol() {
        String symbol = contract.m_symbol;
        if (contract.m_currency != null) {
            symbol += "." + contract.m_currency;
        }
        return symbol;
    }


    public String getName() {
        return name;
    }

    public void processInstant(long instant, boolean isInSchedule) {
        time = instant;
        indicatorManager.updateIndicators();

        if (isInSchedule) {
            if (indicatorManager.hasValidIndicators()) {
                onBookSnapshot();
            }
        } else {
            closePosition();// force flat position
        }

        positionManager.trade();
    }


    public void checkForGap(MarketSnapshot newMarketSnapshot) {
        if (!marketBook.isEmpty()) {
            long previousTime = marketBook.getSnapshot().getTime();
            if (newMarketSnapshot.getTime() - previousTime > GAP_SIZE) {
                // This may occur if the trading interval extends beyond the recorded data for the day.
                // For example, trading interval is 10:00-15:00, but the recorded data for the day ends at 14:00.
                // In such cases, position will be closed at 14:00. 
                closePosition();
                positionManager.trade();
            }
        }
    }


    public void process() {
        if (!marketBook.isEmpty()) {
            MarketSnapshot marketSnapshot = marketBook.getSnapshot();
            long instant = marketSnapshot.getTime();
            processInstant(instant, tradingSchedule.contains(instant));
            performanceManager.updatePositionValue(marketSnapshot.getPrice(), positionManager.getPosition());
            dispatcher.fireModelChanged(Event.StrategyUpdate, this);
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


    public int compareTo(Strategy other) {
        return name.compareTo(other.name);
    }

}
