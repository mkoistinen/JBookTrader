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
public class Rider extends Strategy {

    // Technical indicators
    private final Indicator balanceVelocityInd;

    // Strategy parameters names
    private static final String FAST_PERIOD = "FastPeriod";
    private static final String SLOW_PERIOD = "SlowPeriod";
    private static final String ENTRY = "Entry";
    private static final String EXIT = "Exit";

    // Strategy parameters values
    private final int entry, exit;


    public Rider(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);

        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("ES", "GLOBEX");
        int multiplier = 50;// contract multiplier

        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("9:35", "15:55", "America/New_York");

        Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission);

        entry = getParam(ENTRY);
        exit = getParam(EXIT);
        Indicator balanceInd = new Balance();
        balanceVelocityInd = new Velocity(balanceInd, getParam(FAST_PERIOD), getParam(SLOW_PERIOD));
        addIndicator(balanceInd);
        addIndicator(balanceVelocityInd);


    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(FAST_PERIOD, 80, 300, 50, 125);
        addParam(SLOW_PERIOD, 300, 900, 50, 600);
        addParam(ENTRY, 0, 30, 5, 16);
        addParam(EXIT, 0, 30, 5, 10);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        double balanceVelocity = balanceVelocityInd.getValue();
        if (balanceVelocity >= entry) {
            setPosition(1);
        } else if (balanceVelocity <= -entry) {
            setPosition(-1);
        } else {
            int currentPosition = getPositionManager().getPosition();
            if (currentPosition > 0 && balanceVelocity <= -exit) {
                setPosition(0);
            }
            if (currentPosition < 0 && balanceVelocity >= exit) {
                setPosition(0);
            }
        }
    }
}
