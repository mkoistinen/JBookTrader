package com.jbooktrader.indicator.balance;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.util.movingwindow.*;

/**
 * Balance volatility
 *
 * @author Eugene Kononov
 */
public class BalanceVolatility extends Indicator {
    private final MovingWindowStDev balances;

    public BalanceVolatility(int period) {
        super(period);
        balances = new MovingWindowStDev(period);
    }

    @Override
    public void calculate() {
        balances.add(marketBook.getSnapshot().getBalance());
        if (balances.isFull()) {
            value = balances.getStdev() / 10;
        }
    }

    @Override
    public void reset() {
        value = 0;
        balances.clear();
    }
}
