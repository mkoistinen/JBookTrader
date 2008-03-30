package com.jbooktrader.platform.optimizer;

import java.util.*;

/**
 * Comparator for strategy optimization results.
 */
public class ResultComparator implements Comparator<Result> {
    private final ResultsTableModel.Column sortColumn;

    public ResultComparator(ResultsTableModel.Column sortColumn) {
        this.sortColumn = sortColumn;
    }

    public int compare(Result r1, Result r2) {
        int res = 0;

        switch (sortColumn) {
            case PL:
                // highest to lowest
                res = ((Double) r2.getNetProfit()).compareTo(r1.getNetProfit());
                break;
            case PF:
                // highest to lowest
                res = ((Double) r2.getProfitFactor()).compareTo(r1.getProfitFactor());
                break;
            case MaxDD:
                // lowest to highest
                res = ((Double) r1.getMaxDrawdown()).compareTo(r2.getMaxDrawdown());
                break;
            case Trades:
                // lowest to highest
                res = ((Integer) r1.getTrades()).compareTo(r2.getTrades());
                break;
            case TrueKelly:
                // highest to lowest
                res = ((Double) r2.getTrueKelly()).compareTo(r1.getTrueKelly());
                break;
        }

        return res;
    }
}
