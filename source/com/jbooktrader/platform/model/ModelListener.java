package com.jbooktrader.platform.model;

public interface ModelListener {
    enum Event {
        StrategiesStart, StrategiesEnd, StrategyUpdate, Error
    }

    void modelChanged(Event event, Object value);
}
