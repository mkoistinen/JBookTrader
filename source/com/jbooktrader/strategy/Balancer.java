package com.jbooktrader.strategy;

import com.ib.client.*;
import com.jbooktrader.indicator.balance.*;
import com.jbooktrader.indicator.price.*;
import com.jbooktrader.platform.commission.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.platform.schedule.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;

/**
 *
 */
public class Balancer extends Strategy {

    // Technical indicators
    private final Indicator balanceEmaInd, rsiInd;

    // Strategy parameters names
    private static final String EMA_PERIOD = "EmaPeriod";
    private static final String RSI_PERIOD = "RsiPeriod";
    private static final String BALANCE_ENTRY = "BalanceEntry";
    private static final String RSI_ENTRY = "RsiEntry";

    // Strategy parameters values
    private final int balanceEntry, rsiEntry;


    public Balancer(StrategyParams optimizationParams, MarketBook marketBook) throws JBookTraderException {
        super(optimizationParams, marketBook);
        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("ES", "GLOBEX");
        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("9:20", "16:10", "America/New_York");
        int multiplier = 50;// contract multiplier
        Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission);

        balanceEntry = getParam(BALANCE_ENTRY);
        rsiEntry = getParam(RSI_ENTRY);

        // Create technical indicators
        rsiInd = new PriceRSI(marketBook, getParam(RSI_PERIOD));
        balanceEmaInd = new BalanceEMA(marketBook, getParam(EMA_PERIOD));
        addIndicator("PriceRSI", rsiInd);
        addIndicator("BalanceEMA", balanceEmaInd);

    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        // cme: 13-120-25-15
        addParam(EMA_PERIOD, 2, 30, 1, 18);
        addParam(RSI_PERIOD, 50, 300, 50, 128);
        addParam(BALANCE_ENTRY, 15, 45, 5, 27);
        addParam(RSI_ENTRY, 5, 30, 5, 15);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        double rsi = rsiInd.getValue() - 50;
        double balanceEma = balanceEmaInd.getValue();
        if (balanceEma >= balanceEntry && rsi <= -rsiEntry) {
            setPosition(1);
        } else if (balanceEma <= -balanceEntry && rsi >= rsiEntry) {
            setPosition(-1);
        }
    }
}
