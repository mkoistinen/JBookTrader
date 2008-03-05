package com.jbooktrader.platform.commission;

/**
 */
public class Commission {
    private final double rate, minimum, maximumPercent;

    /**
     *
     * @param rate Commission per contract or per share
     * @param minimum Minimum commission per order
     * @param maximumPercent Maximum commission as percent of the trade amount
     *
     * For commissions and fees, see
     * http://individuals.interactivebrokers.com/en/accounts/fees/commission.php?ib_entity=llc
     */
    public Commission(double rate, double minimum, double maximumPercent) {
        this.rate = rate;
        this.minimum = minimum;
        this.maximumPercent = maximumPercent;
    }

    public Commission(double rate, double minimum) {
        this(rate, minimum, 0);
    }


    public double getCommission(int contracts, double price) {
        double commission = rate * contracts;
        commission = Math.max(commission, minimum);
        if (maximumPercent > 0) {
            double maximumCommission = contracts * price * maximumPercent;
            commission = Math.min(commission, maximumCommission);
        }

        return commission;
    }
}
