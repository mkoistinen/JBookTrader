package com.jbooktrader.platform.marketdepth;

import java.util.*;

/**
 * @author Eugene Kononov
 */
public enum MarketDepthOperation {

    Insert(0), Update(1), Delete(2);

    private final int value;
    private static final Map<Integer, MarketDepthOperation> operations = new HashMap<>();


    MarketDepthOperation(int value) {
        this.value = value;
    }

    public static MarketDepthOperation getOperation(int value) {
        return operations.get(value);
    }

    static {
        for (MarketDepthOperation operation : values()) {
            operations.put(operation.value, operation);
        }
    }
}