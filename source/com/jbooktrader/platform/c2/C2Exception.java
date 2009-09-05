package com.jbooktrader.platform.c2;

/**
 * Parent class for all exceptions caused by internal problems involving
 * Collective 2 (server or local).
 */
public class C2Exception extends Exception {
    public C2Exception(String message) {
        super(message);
    }

    public C2Exception(Throwable cause) {
        super(cause);
    }
}
