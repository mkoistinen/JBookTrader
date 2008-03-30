package com.jbooktrader.platform.performance;

import java.util.*;

/**
 * Holds P&L history.
 */
public class ProfitAndLossHistory {
    private final LinkedList<ProfitAndLoss> history;

    public ProfitAndLossHistory() {
        history = new LinkedList<ProfitAndLoss>();
    }

    public void clear() {
        history.clear();
    }

    public void add(ProfitAndLoss profitAndLoss) {
        history.add(profitAndLoss);
    }

    public LinkedList<ProfitAndLoss> getHistory() {
        return history;
    }

}
