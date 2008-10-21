package com.jbooktrader.platform.strategy;

import com.ib.client.*;
import com.jbooktrader.platform.c2.*;
import com.jbooktrader.platform.commission.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;
import static com.jbooktrader.platform.model.Dispatcher.Mode.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.position.*;
import com.jbooktrader.platform.report.*;
import com.jbooktrader.platform.schedule.*;

/**
 * Base class for all classes that implement trading strategies.
 */

public abstract class Strategy {
    private final StrategyParams params;
    private MarketBook marketBook;
    private final Report eventReport;
    private final String name;
    private Contract contract;
    private TradingSchedule tradingSchedule;
    private PositionManager positionManager;
    private PerformanceManager performanceManager;
    private StrategyReportManager strategyReportManager;
    private IndicatorManager indicatorManager;
    private boolean isActive;
    private int position;
    private long time;
    private boolean isC2enabled;
    private String c2SystemId;

    /**
     * Framework calls this method when order book changes.
     */
    abstract public void onBookChange();

    /**
     * Framework calls this method to set strategy parameter ranges and values.
     */
    abstract protected void setParams();


    protected Strategy(StrategyParams params) {
        this.params = params;
        if (params.size() == 0) {
            setParams();
        }

        name = getClass().getSimpleName();
        eventReport = Dispatcher.getReporter();
    }

    public void setMarketBook(MarketBook marketBook) {
        this.marketBook = marketBook;
        indicatorManager.setMarketBook(marketBook);
    }

    public boolean isActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
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
            Dispatcher.Mode mode = Dispatcher.getMode();
            if (mode == ForwardTest || mode == Trade) {
                String msg = "End of trading interval. Closing current position.";
                eventReport.report(getName() + ": " + msg);
            }
        }
    }

    public void setCollective2() {
        C2TableModel c2TableModel = new C2TableModel();
        C2Value c2Value = c2TableModel.getStrategy(name);
        isC2enabled = c2Value.getIsEnabled();
        c2SystemId = c2Value.getId();
    }


    public StrategyParams getParams() {
        return params;
    }

    protected int getParam(String name) throws JBookTraderException {
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

    protected void setStrategy(Contract contract, TradingSchedule tradingSchedule, int multiplier, Commission commission) {
        this.contract = contract;
        contract.m_multiplier = String.valueOf(multiplier);
        this.tradingSchedule = tradingSchedule;
        performanceManager = new PerformanceManager(this, multiplier, commission);
        positionManager = new PositionManager(this);
        strategyReportManager = new StrategyReportManager(this);
        marketBook = Dispatcher.getTrader().getAssistant().createMarketBook(this);
        indicatorManager = new IndicatorManager();
    }

    public MarketBook getMarketBook() {
        return marketBook;
    }

    public Contract getContract() {
        return contract;
    }

    public String getName() {
        return name;
    }


    public boolean isC2enabled() {
        return isC2enabled;
    }

    public String getC2SystemId() {
        return c2SystemId;
    }

    public void process() {
        if (isActive() && marketBook.size() > 0) {
            MarketSnapshot marketSnapshot = marketBook.getLastMarketSnapshot();
            long instant = marketSnapshot.getTime();
            setTime(instant);
            indicatorManager.updateIndicators();

            if (tradingSchedule.contains(instant)) {
                if (indicatorManager.hasValidIndicators()) {
                    onBookChange();
                }
            } else {
                closePosition();// force flat position
            }

            positionManager.trade();
            performanceManager.updatePositionValue(marketSnapshot.getPrice(), positionManager.getPosition());
            Dispatcher.fireModelChanged(ModelListener.Event.StrategyUpdate, this);
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
}
