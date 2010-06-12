package com.jbooktrader.strategy;

import com.jbooktrader.indicator.velocity.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.strategy.base.*;

/**
 *
 */
public class ProfitTaker extends StrategyES {

	// Technical indicators
	private final Indicator balanceVelocityInd;

	// Strategy parameters names
	private static final String SLOW_PERIOD = "SlowPeriod";
	private static final String ENTRY = "Entry";
	private static final String TARGET = "Target";

	// Strategy parameters values
	private final int entry, target;

	public ProfitTaker(StrategyParams optimizationParams) throws JBookTraderException {
		super(optimizationParams);

		entry = getParam(ENTRY);
		target = getParam(TARGET);
		balanceVelocityInd = new BalanceVelocity(30, getParam(SLOW_PERIOD));
		addIndicator(balanceVelocityInd);
	}

	/**
	 * Adds parameters to strategy. Each parameter must have 5 values: name:
	 * identifier min, max, step: range for optimizer value: used in backtesting
	 * and trading
	 */
	@Override
	public void setParams() {
		addParam(SLOW_PERIOD, 100, 4000, 100, 3400);
		addParam(ENTRY, 10, 30, 1, 15);
		addParam(TARGET, 1, 20, 1, 5);
	}

	/**
	 * Framework invokes this method when a new snapshot of the limit order book
	 * is taken and the technical indicators are recalculated. This is where the
	 * strategy itself (i.e., its entry and exit conditions) should be defined.
	 */
	@Override
	public void onBookSnapshot() {
		double balanceVelocity = balanceVelocityInd.getValue();
		int currentPosition = getPositionManager().getPosition();
		double priceDiff = getPositionManager().getAvgFillPrice() - getMarketBook().getSnapshot().getPrice();
		double profit = 0;
		if (currentPosition > 0) {
			profit = -priceDiff;
		} else if (currentPosition < 0) {
			profit = priceDiff;
		}

		if (profit > target) {
			setPosition(0);
		} else {
			if (balanceVelocity >= entry) {
				setPosition(1);
			} else if (balanceVelocity <= -entry) {
				setPosition(-1);
			}
		}
	}
}
