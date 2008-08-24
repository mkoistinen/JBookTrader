package com.jbooktrader.platform.marketdepth;

import java.util.*;

public enum MarketBookOperation {

    Insert(0),
    Update(1),
    Delete(2);

    private final int value;
    private static final Map<Integer, MarketBookOperation> operations = new HashMap<Integer, MarketBookOperation>();


    MarketBookOperation(int value) {
        this.value = value;
    }

    private int getValue() {
        return value;
    }

    public static MarketBookOperation getOperation(int value) {
        return operations.get(value);
    }

    static {
        for (MarketBookOperation operation : values()) {
            operations.put(operation.getValue(), operation);
        }
    }
}