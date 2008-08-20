package com.jbooktrader.platform.marketdepth;

import java.util.*;

public enum MarketBookSide {

    Ask(0),
    Bid(1);

    private final int value;
    private static Map<Integer, MarketBookSide> sides = new HashMap<Integer, MarketBookSide>();


    MarketBookSide(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static MarketBookSide getSide(int value) {
        return sides.get(value);
    }

    static {
        for (MarketBookSide side : values()) {
            sides.put(side.getValue(), side);
        }
    }
}