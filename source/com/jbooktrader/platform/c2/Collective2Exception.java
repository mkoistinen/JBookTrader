package com.jbooktrader.platform.c2;

/**
 * Parent class for all exceptions caused by internal problems involving
 * Collective 2 (server or local).
 */
public class Collective2Exception extends Exception {
    public Collective2Exception(final String message) {
        super(message);
    }

    public Collective2Exception(final Throwable cause) {
        super(cause);
    }
}
