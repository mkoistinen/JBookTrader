package com.jbooktrader.platform.c2;

/**
 * Parent class for all exceptions caused by internal problems involving
 * Collective 2 (server or local).
 */
public class C2Exception extends Exception {
    public C2Exception(final String message) {
        super(message);
    }

    public C2Exception(final Throwable cause) {
        super(cause);
    }
}
