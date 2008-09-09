package com.jbooktrader.platform.performance;

import com.jbooktrader.platform.util.*;

import java.util.*;

/**
 * Holds net profit history for a strategy.
 */
public class NetProfitHistory {
    private final List<TimedValue> history;

    public NetProfitHistory() {
        history = new LinkedList<TimedValue>();
    }

    public void clear() {
        history.clear();
    }

    public void add(TimedValue netProfit) {
        history.add(netProfit);
    }

    public List<TimedValue> getHistory() {
        return history;
    }

}
