package com.jbooktrader.strategy;

import com.ib.client.Contract;
import com.jbooktrader.indicator.TruePrice;
import com.jbooktrader.platform.commission.*;
import com.jbooktrader.platform.indicator.Indicator;
import com.jbooktrader.platform.marketdepth.MarketBook;
import com.jbooktrader.platform.model.JBookTraderException;
import com.jbooktrader.platform.optimizer.StrategyParams;
import com.jbooktrader.platform.schedule.TradingSchedule;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.util.ContractFactory;

/**
 *
 */
public class Arbitrager extends Strategy {

    // Technical indicators
    private final Indicator truePriceInd;

    // Strategy parameters names
    private static final String ENTRY = "Entry";
    private static final String EXIT = "Exit";

    // Strategy parameters values
    private final double entry, exit;


    public Arbitrager(StrategyParams params, MarketBook marketBook) throws JBookTraderException {
        super(marketBook);
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
        entry = params.get(ENTRY, 0.7);
        exit = params.get(EXIT, 0.3);

        // Create technical indicators
        truePriceInd = new TruePrice(marketBook);

        // Specify the title and the chart number for each indicator
        // "0" = the same chart as the price chart; "1+" = separate subchart (below the price chart)
        addIndicator("Depth Balance", truePriceInd, 0);
    }

    /**
     * Returns min/max/step values for each strategy parameter. This method is
     * invoked by the strategy optimizer to obtain the strategy parameter ranges.
     */
    @Override
    public StrategyParams initParams() {
        StrategyParams params = new StrategyParams();
        params.add(ENTRY, 0, 2, 0.1);
        params.add(EXIT, 0, 2, 0.1);
        return params;
    }

    /**
     * This method is invoked by the framework when an order book changes and the technical
     * indicators are recalculated. This is where the strategy itself should be defined.
     */
    @Override
    public void onBookChange() {
        int currentPosition = getPositionManager().getPosition();
        double truePrice = truePriceInd.getValue();
        double price = getLastMarketDepth().getMidPoint();
        double diff = truePrice - price;
        if (diff >= entry) {
            setPosition(1);
        } else if (diff <= -entry) {
            setPosition(-1);
        } else {
            boolean target = (currentPosition > 0 && diff <= -exit);
            target = target || (currentPosition < 0 && diff >= exit);
            if (target) {
                setPosition(0);
            }
        }
    }
}
