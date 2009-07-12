package com.jbooktrader.platform.web;

import com.jbooktrader.platform.strategy.*;

import java.util.*;

public abstract class TableLayout {
    protected final StringBuilder response;
    protected final List<Strategy> strategies;

    public TableLayout(StringBuilder response, List<Strategy> strategies) {
        this.response = response;
        this.strategies = strategies;
    }

    public abstract void render();
}

