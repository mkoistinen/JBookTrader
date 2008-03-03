package com.jbooktrader.platform.optimizer;

import java.util.Comparator;

/**
 * Comparator for strategy optimization results.
 */
public class ResultComparator implements Comparator<Result> {
    enum SortKey {
        TOTAL_PROFIT, PROFIT_FACTOR, DRAWDOWN, TRADES, TRUE_KELLY
    }

    private final SortKey sortKey;

    public ResultComparator(SortKey sortKey) {
        this.sortKey = sortKey;
    }

    public int compare(Result r1, Result r2) {
        int res = 0;

        switch (sortKey) {
            case TOTAL_PROFIT:
                // highest to lowest
                res = ((Double) r2.getTotalProfit()).compareTo(r1.getTotalProfit());
                break;
            case PROFIT_FACTOR:
                // highest to lowest
                res = ((Double) r2.getProfitFactor()).compareTo(r1.getProfitFactor());
                break;
            case DRAWDOWN:
                // lowest to highest
                res = ((Double) r1.getMaxDrawdown()).compareTo(r2.getMaxDrawdown());
                break;
            case TRADES:
                // lowest to highest
                res = ((Integer) r1.getTrades()).compareTo(r2.getTrades());
                break;
            case TRUE_KELLY:
                // highest to lowest
                res = ((Double) r2.getTrueKelly()).compareTo(r1.getTrueKelly());
                break;

        }

        return res;
    }
}
