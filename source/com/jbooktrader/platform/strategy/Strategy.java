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

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for all classes that implement trading strategies.
 */

public abstract class Strategy implements Comparable<Strategy> {
    private final StrategyParams params;
    private final EventReport eventReport;
    private final Dispatcher dispatcher;
    private final String name;
    private MarketBook marketBook;
    private Contract contract;
    private TradingSchedule tradingSchedule;
    private PositionManager positionManager;
    private PerformanceManager performanceManager;
    private StrategyReportManager strategyReportManager;
    private IndicatorManager indicatorManager;
    private double bidAskSpread;
    private Date lastContractCheck;

    private static int HOUR_IN_MS = 1000*60*60;

    private final static Logger LOGGER = Logger.getLogger(Strategy.class.getName());

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

    /**
     * Framework calls this to check and replace contract if needed
     * optional override
     */
    public Contract getNewContract() {
        String msg = "getNewContract was not implemented by Strategy, override getNewContract to be sure you end up with proper contract every day.";
        eventReport.report(name, msg);
        return contract; // default impl returns existing contract, no check is done
    }

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

    protected void setPosition(int position) {
        positionManager.setTargetPosition(position);
    }

    public double getBidAskSpread() {
        return bidAskSpread;
    }

    public void closePosition() {
        setPosition(0);
        if (positionManager.getCurrentPosition() != 0) {
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
        lastContractCheck = new Date(); // set this, as our contract is valid now, just created
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
            // check if we have a current position, because it may take time to close, if zero, replace contract if needed
            if (positionManager.getCurrentPosition() == 0)
            {
                // checking only after trading hours, and after positions are all closed
                Date now = new Date();
                if (lastContractCheck == null || (lastContractCheck.getTime() < now.getTime() - HOUR_IN_MS)) {
                    LOGGER.log(Level.FINEST,"Checking contract");
                    Contract newContract = getNewContract();
                    lastContractCheck = now;
                    if (!contract.m_expiry.equals(newContract.m_expiry)) {
                        // need to switch contracts, should be ok, we have no open positions and we are not in side trading hours
                        String msg = "Switching Contract from" + contract.m_symbol+"-"+contract.m_expiry + "to " + newContract.m_symbol+"-"+newContract.m_expiry;
                        eventReport.report(name, msg);
                        contract = newContract;
                        marketBook = dispatcher.getTrader().getAssistant().createMarketBook(this);   // create new market book for security
                        // for now, we left the old marketbook in TraderAssistant, because we don't know if another Strategy needs it
                    }
                }
            }
        }
    }


    public void process() {
        if (!marketBook.isEmpty()) {
            indicatorManager.updateIndicators();
            MarketSnapshot marketSnapshot = marketBook.getSnapshot();
            long instant = marketSnapshot.getTime();
            processInstant(tradingSchedule.contains(instant));
            performanceManager.updatePositionValue(marketSnapshot.getPrice(), positionManager.getCurrentPosition());
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
