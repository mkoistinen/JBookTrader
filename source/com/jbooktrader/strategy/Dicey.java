package com.jbooktrader.strategy;

import com.ib.client.*;
import com.jbooktrader.indicator.volume.*;
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
public class Dicey extends Strategy {

    // Technical indicators
    private final Indicator directionalVolumeInd;

    // Strategy parameters names
    private static final String PERIOD = "Period";
    private static final String ENTRY = "Entry";

    // Strategy parameters values
    private final int entry;


    public Dicey(StrategyParams optimizationParams) throws JBookTraderException {
        super(optimizationParams);

        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("ES", "GLOBEX");
        int multiplier = 50;// contract multiplier

        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("9:35", "15:55", "America/New_York");

        Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission);

        entry = getParam(ENTRY);
        directionalVolumeInd = new DirectionalVolume(getParam(PERIOD));
        addIndicator(directionalVolumeInd);

    }

    /**
     * Adds parameters to strategy. Each parameter must have 5 values:
     * name: identifier
     * min, max, step: range for optimizer
     * value: used in backtesting and trading
     */
    @Override
    public void setParams() {
        addParam(PERIOD, 50, 300, 1, 207);
        addParam(ENTRY, 20, 45, 1, 29);
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        double directionalVolume = directionalVolumeInd.getValue();
        if (directionalVolume >= entry) {
            setPosition(-1);
        } else if (directionalVolume <= -entry) {
            setPosition(1);
        }
    }
}
