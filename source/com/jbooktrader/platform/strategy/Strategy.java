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
    private double bidAskSpread;

    /**
     * Framework calls this method when a new snapshot of the limit order book is taken.
     */
    public abstract void onBookSnapshot();

    /**
     * Framework calls this method to set strategy parameter ranges and values.
     */
    protected abstract void setParams();

    /**
     * Framework calls this method to instantiate indicators.
     */
    public abstract void setIndicators();

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
    }

    public void setIndicatorManager(IndicatorManager indicatorManager) {
        this.indicatorManager = indicatorManager;
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
            positionManager.trade();
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

    protected Indicator addIndicator(Indicator indicator) {
        return indicatorManager.addIndicator(indicator);
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

    public void processInstant(boolean isInSchedule) {
        if (isInSchedule) {
            if (indicatorManager.hasValidIndicators()) {
                onBookSnapshot();
                positionManager.trade();
            }
        } else {
            closePosition();// force flat position
        }
    }


    public void process() {
        if (!marketBook.isEmpty()) {
            MarketSnapshot marketSnapshot = marketBook.getSnapshot();
            long instant = marketSnapshot.getTime();
            processInstant(tradingSchedule.contains(instant));
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
