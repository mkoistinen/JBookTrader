package com.jbooktrader.strategy;

import com.ib.client.*;
import com.jbooktrader.indicator.balance.*;
import com.jbooktrader.indicator.price.*;
import com.jbooktrader.platform.commission.*;
import com.jbooktrader.platform.indicator.*;
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


    public Balancer(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);
        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("ES", "GLOBEX");
        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("9:35", "15:55", "America/New_York");
        int multiplier = 50;// contract multiplier
        Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission);

        balanceEntry = getParam(BALANCE_ENTRY);
        rsiEntry = getParam(RSI_ENTRY);

        // Create technical indicators
        rsiInd = new PriceRSI(getParam(RSI_PERIOD));
        balanceEmaInd = new BalanceEMA(getParam(EMA_PERIOD));
        addIndicator(rsiInd);
        addIndicator(balanceEmaInd);

    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(EMA_PERIOD, 10, 35, 5, 30);
        addParam(RSI_PERIOD, 70, 140, 25, 82);
        addParam(BALANCE_ENTRY, 17, 32, 5, 20);
        addParam(RSI_ENTRY, 13, 28, 5, 25);
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
