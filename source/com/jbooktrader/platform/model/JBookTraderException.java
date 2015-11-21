package com.jbooktrader.platform.model;

/**
 * @author Eugene Kononov
 */
public class JBookTraderException extends Exception {
    public JBookTraderException(String message) {
        super(message);
    }

    public JBookTraderException(Throwable e) {
        super(e.getMessage(), e);
    }

    public JBookTraderException(String message, Throwable cause) {
        super(message, cause);
    }
}
