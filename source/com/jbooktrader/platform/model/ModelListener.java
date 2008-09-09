package com.jbooktrader.platform.model;

public interface ModelListener {
    enum Event {
        StrategiesStart, StrategiesEnd, StrategyUpdate, ModeChanged, Error
    }

    void modelChanged(Event event, Object value);
}
