package com.jbooktrader.platform.commission;


/**
 * @author Eugene Kononov
 */
public class CommissionFactory {

    private static Commission getCommission(double rate, double min) {
        return new Commission(rate, min);
    }

    /**
     * For commissions and fees, see
     * http://individuals.interactivebrokers.com/en/accounts/fees/commission.php?ib_entity=llc
     *
     * @param rate       Commission per contract or per share
     * @param min        Minimum commission per order
     * @param maxPercent Maximum commission as percent of the trade amount
     * @return Commission for the trade
     */
    private static Commission getCommission(double rate, double min, double maxPercent) {
        return new Commission(rate, min, maxPercent);
    }


    public static Commission getBundledNorthAmericaStockCommission() {
        return getCommission(0.005, 1, 0.005);
    }

    /**
     * Futures commissions: http://individuals.interactivebrokers.com/en/p.php?f=commission#futures1
     */
    public static Commission getBundledNorthAmericaFutureCommission() {
        return getCommission(2.01, 2.01);
    }


    public static Commission getForexCommission() {
        return getCommission(0.00002, 2.5);
    }

}
