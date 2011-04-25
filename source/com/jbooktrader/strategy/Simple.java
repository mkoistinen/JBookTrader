package com.jbooktrader.strategy;

import com.ib.client.*;
import com.jbooktrader.indicator.combo.*;
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
    private Indicator tensionInd;

    // Strategy parameters names
    private static final String PERIOD = "Period";
    private static final String SCALE_FACTOR = "Scale Factor";
    private static final String ENTRY = "Entry";


    // Strategy parameters values
    private final int entry;

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

        entry = getParam(ENTRY);
    }

    @Override
    public void setParams() {
        addParam(PERIOD, 2500, 8000, 1, 5385);
        addParam(SCALE_FACTOR, 10, 40, 1, 23);
        addParam(ENTRY, 15, 40, 1, 27);
    }


    @Override
    public void setIndicators() {
        tensionInd = addIndicator(new Tension(getParam(PERIOD), getParam(SCALE_FACTOR)));
    }

    @Override
    public void onBookSnapshot() {
        double tension = tensionInd.getValue();
        if (tension <= -entry) {
            setPosition(-1);
        } else if (tension >= 0) {
            setPosition(0);
        }
    }

}
