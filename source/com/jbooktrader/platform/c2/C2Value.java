package com.jbooktrader.platform.c2;

public class C2Value {
    private final String systemId;
    private final boolean isEnabled;

    C2Value(String systemId, boolean isEnabled) {
        this.systemId = systemId;
        this.isEnabled = isEnabled;
    }

    public String getId() {
        return systemId;
    }

    public boolean getIsEnabled() {
        return isEnabled;
    }

}