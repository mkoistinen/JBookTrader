package com.jbooktrader.platform.commission;


public class CommissionFactory {

    public static Commission getCommission(double min, double rate) {
        return new Commission(min, rate);
    }

    public static Commission getBundledNorthAmericaStockCommission() {
        return getCommission(1, 0.005);
    }

    public static Commission getBundledNorthAmericaFutureCommission() {
        return getCommission(2.4, 2.4);
    }


    public static Commission getForexCommission() {
        return getCommission(2.5, 0.00002);
    }

}
