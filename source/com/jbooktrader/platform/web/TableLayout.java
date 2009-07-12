package com.jbooktrader.platform.web;

import com.jbooktrader.platform.strategy.*;

import java.util.*;

public abstract class TableLayout {
    protected StringBuilder response;
    protected List<Strategy> strategyList;

    public TableLayout(StringBuilder response, List<Strategy> strategyList) {
        this.response = response;
        this.strategyList = strategyList;
    }

    public abstract void render();
}

