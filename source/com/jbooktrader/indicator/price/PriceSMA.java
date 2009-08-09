package com.jbooktrader.indicator.price;

import com.jbooktrader.platform.indicator.*;
import java.util.*;

public class PriceSMA extends Indicator {

	private final int period;
	
	private LinkedList<Double> history = new LinkedList<Double>();
	private double price, sum;
	
	public PriceSMA(int period) {
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
		
		price = marketBook.getSnapshot().getPrice();
		sum += price;
		
		// In with the new
		history.addLast(price);
		
		// Out with the old
		while (history.size() > period) {
			sum -= history.removeFirst();
		}
		
		if (history.size() > 0) {
			value = sum / history.size();
		}
	}
	
}
