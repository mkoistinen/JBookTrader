package com.jbooktrader.strategy;

import com.ib.client.*;
import com.jbooktrader.indicator.balance.*;
import com.jbooktrader.platform.commission.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.platform.schedule.*;
import com.jbooktrader.platform.util.*;
import com.jbooktrader.strategy.base.*;

/**
 *
 */
public class Simple extends StrategyES {

    // Technical indicators
    private Indicator balanceEmaInd;

    // Strategy parameters names
    private static final String PERIOD = "Period";
    private static final String LONG_ENTRY = "Long Entry";
    private static final String SHORT_ENTRY = "Short Entry";
    private static final String EXIT = "Exit";

    // Strategy parameters values
    private final int longEntry, shortEntry, exit;


    public Simple(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);
        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("ES", "GLOBEX");
        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("10:00", "15:30", "America/New_York");
        int multiplier = 50;// contract multiplier
        double bidAskSpread = 0.25; // prevalent spread between best bid and best ask
        Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission, bidAskSpread);

        longEntry = getParam(LONG_ENTRY);
        shortEntry = getParam(SHORT_ENTRY);
        exit = getParam(EXIT);
    }

    @Override
    public void setIndicators() {
        balanceEmaInd = addIndicator(new BalanceEMA(getParam(PERIOD)));
    }


    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(PERIOD, 1, 50, 1, 27);
        addParam(LONG_ENTRY, 30, 55, 1, 35);
        addParam(SHORT_ENTRY, 20, 30, 1, 25);
        addParam(EXIT, 1, 10, 1, 6);
    }

    /**
     * Framework invokes this method when a new snapshot of the limit order book is taken
     * and the technical indicators are recalculated. This is where the strategy itself
     * (i.e., its entry and exit conditions) should be defined.
     */
    @Override
    public void onBookSnapshot() {
        double balanceEma = balanceEmaInd.getValue();
        if (balanceEma >= longEntry) {
            setPosition(1);
        } else if (balanceEma <= -shortEntry) {
            setPosition(-1);
        } else if (Math.abs(balanceEma) < exit) {
            setPosition(0);
        }
    }
}
