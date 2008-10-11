package com.jbooktrader.platform.c2;

public enum C2Action {
    BuyToOpen("BTO"),
    SellToOpen("STO"),
    BuyToClose("BTC"),
    SellToClose("STC");

    private final String code;

    private C2Action(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}