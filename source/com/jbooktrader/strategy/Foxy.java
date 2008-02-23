package com.jbooktrader.strategy;

import com.ib.client.Contract;
import com.jbooktrader.indicator.MarketDepthRatio;
import com.jbooktrader.platform.indicator.Indicator;
import com.jbooktrader.platform.model.JBookTraderException;
import com.jbooktrader.platform.optimizer.StrategyParams;
import com.jbooktrader.platform.schedule.TradingSchedule;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.util.ContractFactory;

/**
 *
 */
public class Foxy extends Strategy {

    // Technical indicators
    private final Indicator marketDepthRatioInd;

    // Strategy parameters names
    private static final String ENTRY = "Entry";
    private static final String EXIT = "Exit";

    // Strategy parameters values
    private final double entry, exit;


    public Foxy(StrategyParams params) throws JBookTraderException {
        // Specify the contract to trade
        Contract contract = ContractFactory.makeFutureContract("ES", "GLOBEX");
        // Define trading schedule
        TradingSchedule tradingSchedule = new TradingSchedule("9:20", "16:10", "America/New_York");
        int multiplier = 50;// contract multiplier
        double commissionRate = 2.4;// commission per contract
        setStrategy(contract, tradingSchedule, multiplier, commissionRate);

        // Initialize strategy parameter values. If the strategy is running in the optimization
        // mode, the parameter values will be taken from the "params" object. Otherwise, the
        // "params" object will be empty and the parameter values will be initialized to the
        // specified default values.
        entry = params.get(ENTRY, 2.75);
        exit = params.get(EXIT, 1.5);

        // Create technical indicators
        marketDepthRatioInd = new MarketDepthRatio(marketBook);

        // Specify the title and the chart number for each indicator
        // "0" = the same chart as the price chart; "1+" = separate subchart (below the price chart)
        addIndicator("Market Depth Ratio", marketDepthRatioInd, 1);
    }

    /**
     * Returns min/max/step values for each strategy parameter. This method is
     * invoked by the strategy optimizer to obtain the strategy parameter ranges.
     */
    @Override
    public StrategyParams initParams() {
        StrategyParams params = new StrategyParams();
        params.add(ENTRY, 2, 5, 0.25);
        params.add(EXIT, 1, 3, 0.25);
        return params;
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        int currentPosition = getPositionManager().getPosition();
        double ratio = marketDepthRatioInd.getValue();
        if (ratio >= entry) {
            setPosition(1);
        } else if (ratio <= (1.0 / entry)) {
            setPosition(-1);
        } else {
            boolean target = (currentPosition > 0 && ratio <= (1.0 / exit));
            target = target || (currentPosition < 0 && ratio >= exit);
            if (target) {
                setPosition(0);
            }
        }
    }
}
