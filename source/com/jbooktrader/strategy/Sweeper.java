package com.jbooktrader.strategy;

import com.ib.client.*;
import com.jbooktrader.indicator.balance.*;
import com.jbooktrader.indicator.derivative.*;
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
public class Sweeper extends Strategy {

    // Technical indicators
    private final Indicator balanceInd, balanceVelocityInd, balanceAccelerationInd;

    // Strategy parameters names
    private static final String FAST_PERIOD = "FastPeriod";
    private static final String SLOW_PERIOD = "SlowPeriod";
    private static final String ENTRY = "Entry";

    // Strategy parameters values
    private final int entry;


    public Sweeper(StrategyParams optimizationParams, MarketBook marketBook) throws JBookTraderException {
        super(optimizationParams, marketBook);

        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("ES", "GLOBEX");
        int multiplier = 50;// contract multiplier

        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("9:20", "16:10", "America/New_York");

        Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission);

        entry = getParam(ENTRY);

        balanceInd = new Balance(marketBook);
        balanceVelocityInd = new Velocity(balanceInd, getParam(FAST_PERIOD), getParam(SLOW_PERIOD));
        balanceAccelerationInd = new Acceleration(balanceInd, getParam(FAST_PERIOD), getParam(SLOW_PERIOD), getParam(SLOW_PERIOD));
        addIndicator("balance", balanceInd);
        addIndicator("velocity", balanceVelocityInd);
        addIndicator("acceleration", balanceAccelerationInd);
    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(FAST_PERIOD, 25, 55, 1, 36);
        addParam(SLOW_PERIOD, 500, 1500, 50, 1100);
        addParam(ENTRY, 20, 50, 1, 39);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        double balanceVelocity = balanceVelocityInd.getValue();
        double balanceAcceleration = balanceAccelerationInd.getValue();
        double strength = balanceVelocity + balanceAcceleration;
        if (strength >= entry) {
            setPosition(1);
        } else if (strength <= -entry) {
            setPosition(-1);
        }
    }
}
