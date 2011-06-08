package com.jbooktrader.strategy.common;

import com.ib.client.*;
import com.jbooktrader.indicator.balance.*;
import com.jbooktrader.indicator.volume.*;
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
public abstract class Pendulum extends StrategyES {

    // Technical indicators
    protected Indicator volumeAccelerationInd, balanceVelocityInd, balanceAccelerationInd;

    // Strategy parameters names
    protected static final String PERIOD = "Period";
    protected static final String TAU = "Tau";
    protected static final String BALANCE_ENTRY = "BalEntry";
    protected static final String VOLUME_ENTRY = "VolEntry";
    protected static final String EXIT = "Exit";

    // Strategy parameters values
    protected final int volumeEntry, balanceEntry, exit;
    protected final double tau;


    public Pendulum(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);
        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("ES", "GLOBEX");
        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("10:00", "15:30", "America/New_York");
        int multiplier = 50;// contract multiplier
        double bidAskSpread = 0.25; // prevalent spread between best bid and best ask
        Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission, bidAskSpread);

        volumeEntry = getParam(VOLUME_ENTRY);
        exit = getParam(EXIT);
        tau = getParam(TAU) / 10.0;
        balanceEntry = getParam(BALANCE_ENTRY);
    }

    @Override
    public void setIndicators() {
        volumeAccelerationInd = addIndicator(new VolumeAcceleration(getParam(PERIOD)));
        balanceAccelerationInd = addIndicator(new BalanceAcceleration(getParam(PERIOD)));
        balanceVelocityInd = addIndicator(new BalanceVelocity(1, getParam(PERIOD)));
    }

    @Override
    public void onBookSnapshot() {
        double volumeAcceleration = volumeAccelerationInd.getValue();
        double finalVelocity = balanceVelocityInd.getValue() + tau * balanceAccelerationInd.getValue();
        if (volumeAcceleration <= -volumeEntry) {
            if (finalVelocity >= balanceEntry) {
                setPosition(1);
            }
        } else if (volumeAcceleration >= exit) {
            setPosition(0);
        }
    }
}
