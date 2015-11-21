package com.jbooktrader.platform.marketdepth;

import java.util.*;

/**
 * @author Eugene Kononov
 */
public enum MarketDepthSide {

    Ask(0), Bid(1);

    private final int value;
    private static final Map<Integer, MarketDepthSide> sides = new HashMap<>();


    MarketDepthSide(int value) {
        this.value = value;
    }

    public static MarketDepthSide getSide(int value) {
        return sides.get(value);
    }

    static {
        for (MarketDepthSide side : values()) {
            sides.put(side.value, side);
        }
    }
}
