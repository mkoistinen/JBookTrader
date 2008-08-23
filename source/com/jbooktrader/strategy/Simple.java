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
public class Simple extends Strategy {

    // Technical indicators
    private final Indicator balanceEmaInd, rsiInd;

    // Strategy parameters names
    private static final String PERIOD = "Period";
    private static final String ENTRY = "Entry";
    //private static final String RSI_ENTRY = "RsiEntry";

    // Strategy parameters values
    private final int entry;


    public Simple(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);
        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("ES", "GLOBEX");
        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("9:35", "15:55", "America/New_York");
        int multiplier = 50;// contract multiplier
        Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission);

        entry = getParam(ENTRY);
        //rsiEntry = getParam(RSI_ENTRY);

        // Create technical indicators
        rsiInd = new PriceRSI(getParam(PERIOD));
        balanceEmaInd = new BalanceEMA(getParam(PERIOD));
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
        addParam(PERIOD, 5, 100, 5, 15);
        //addParam(RSI_PERIOD, 25, 200, 25, 95);
        addParam(ENTRY, 30, 90, 5, 72);
        //addParam(RSI_ENTRY, 10, 45, 5, 16);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        double balance = balanceEmaInd.getValue() - (rsiInd.getValue() - 50);
        if (balance >= entry) {
            setPosition(1);
        } else if (balance <= -entry) {
            setPosition(-1);
        }
    }
}
