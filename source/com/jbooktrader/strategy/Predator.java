package com.jbooktrader.strategy;

import com.ib.client.Contract;
import com.jbooktrader.indicator.*;
import com.jbooktrader.platform.commission.*;
import com.jbooktrader.platform.indicator.Indicator;
import com.jbooktrader.platform.model.JBookTraderException;
import com.jbooktrader.platform.optimizer.StrategyParams;
import com.jbooktrader.platform.schedule.TradingSchedule;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.util.ContractFactory;

/**
 *
 */
public class Predator extends Strategy {

    // Technical indicators
    private final Indicator depthBalanceInd, smoothedDepthBalanceInd;

    // Strategy parameters names
    private static final String PERIOD = "Period";
    private static final String ENTRY = "Entry";
    private static final String EXIT = "Exit";


    // Strategy parameters values
    private final double entry, exit;
    private final int period;


    public Predator(StrategyParams params) throws JBookTraderException {
        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("ES", "GLOBEX");
        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("9:20", "16:10", "America/New_York");
        int multiplier = 50;// contract multiplier
        Commission commission = CommissionFactory.getBundledNorthAmericaFutureCommission();
        setStrategy(contract, tradingSchedule, multiplier, commission);

        // Initialize strategy parameter values. If the strategy is running in the optimization
        // mode, the parameter values will be taken from the "params" object. Otherwise, the
        // "params" object will be empty and the parameter values will be initialized to the
        // specified default values.
        period = (int) params.get(PERIOD, 20);
        entry = params.get(ENTRY, 29);
        exit = params.get(EXIT, 29);


        // Create technical indicators
        depthBalanceInd = new DepthBalance(marketBook);
        smoothedDepthBalanceInd = new SmoothedDepthBalance(marketBook, period);

        // Specify the title and the chart number for each indicator
        // "0" = the same chart as the price chart; "1+" = separate subchart (below the price chart)
        addIndicator("Depth Balance", depthBalanceInd, 1);
        addIndicator("Smoothed Depth Balance", smoothedDepthBalanceInd, 1);
    }

    /**
     * Returns min/max/step values for each strategy parameter. This method is
     * invoked by the strategy optimizer to obtain the strategy parameter ranges.
     */
    @Override
    public StrategyParams initParams() {
        StrategyParams params = new StrategyParams();
        params.add(PERIOD, 1, 50, 1);
        params.add(ENTRY, 5, 50, 1);
        params.add(EXIT, 0, 50, 10);
        return params;
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        int currentPosition = getPositionManager().getPosition();
        double smoothedDepthBalance = smoothedDepthBalanceInd.getValue();
        if (smoothedDepthBalance >= entry) {
            setPosition(1);
        } else if (smoothedDepthBalance <= -entry) {
            setPosition(-1);
        } else {
            boolean target = (currentPosition > 0 && smoothedDepthBalance <= -exit);
            target = target || (currentPosition < 0 && smoothedDepthBalance >= exit);
            if (target) {
                setPosition(0);
            }
        }
    }
}
