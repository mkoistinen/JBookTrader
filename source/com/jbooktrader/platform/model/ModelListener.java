package com.jbooktrader.platform.model;

/**
 * @author Eugene Kononov
 */
public interface ModelListener {
    enum Event {
        StrategyUpdate, ExpirationUpdate, ModeChanged, TimeUpdate, Error
    }

    void modelChanged(Event event, Object value);
}
