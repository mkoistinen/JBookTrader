package com.jbooktrader.indicator.depth;

import com.jbooktrader.platform.indicator.*;
import java.util.*;

public class DepthBalanceStrengthSMA extends Indicator {

	private final int period;
	
	private LinkedList<Double> history = new LinkedList<Double>();
	private double balance, sum;
	
	public DepthBalanceStrengthSMA(int period) {
		this.period = period;
		reset();
	}
	
	@Override
	public void reset() {
		value = sum = 0.0;
		history.clear();
	}
	
	
	@Override
	public void calculate() {
		
		balance = Math.abs(marketBook.getSnapshot().getBalance());
		sum += balance;
		
		// In with the new
		history.addLast(balance);
		
		// Out with the old
		while (history.size() > period) {
			sum -= history.removeFirst();
		}
		
		if (history.size() > 0) {
			value = sum / history.size();
		}
	}
	
}
