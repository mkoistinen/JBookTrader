package com.jbooktrader.platform.model;

public class JBookTraderException extends RuntimeException {
    public JBookTraderException(String message) {
        super(message);
    }

    public JBookTraderException(Exception e) {
        super(e);
    }

    public JBookTraderException(String message, Throwable cause) {
        super(message, cause);
    }
}
