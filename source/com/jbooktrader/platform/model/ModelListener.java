package com.jbooktrader.platform.model;

public interface ModelListener {
    enum Event {
        STRATEGIES_START, STRATEGIES_END, STRATEGY_UPDATE, ERROR
    }

    void modelChanged(Event event, Object value);
}
