package com.jbooktrader.indicator.depth;

import com.jbooktrader.platform.indicator.*;

public class DepthBalanceStrengthEMA extends Indicator {

	private final double alpha;
	private double balance;
	
	public DepthBalanceStrengthEMA(int period) {
		this.alpha = 2.0 / (period + 1);
		reset();
	}
	
	@Override
	public void reset() {
		value = 0.0;
	}
	
	@Override
	public void calculate() {
		balance = Math.abs(marketBook.getSnapshot().getBalance());
		value += alpha * (balance - value);
	}
	
}
