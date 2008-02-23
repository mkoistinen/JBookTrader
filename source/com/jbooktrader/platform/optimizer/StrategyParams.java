package com.jbooktrader.platform.optimizer;

import java.util.*;

/**
 */
public class StrategyParams {
    private final List<StrategyParam> params;
    private final Map<String, StrategyParam> paramsLookUp;

    public StrategyParams() {
        params = new ArrayList<StrategyParam>();
        paramsLookUp = new HashMap<String, StrategyParam>();
    }

    // copy constructor
    public StrategyParams(StrategyParams params) {
        this.params = new ArrayList<StrategyParam>();
        this.paramsLookUp = new HashMap<String, StrategyParam>();
        for (StrategyParam param : params.getAll()) {
            StrategyParam paramCopy = new StrategyParam(param);
            this.params.add(paramCopy);
            this.paramsLookUp.put(paramCopy.getName(), paramCopy);
        }
    }

    public List<StrategyParam> getAll() {
        return params;
    }

    public void add(String name, double min, double max, double step) {
        StrategyParam param = new StrategyParam(name, min, max, step);
        params.add(param);
        paramsLookUp.put(name, param);
    }

    public int size() {
        return params.size();
    }

    public StrategyParam get(int index) {
        return params.get(index);
    }

    public double get(String name, double defaultValue) {
        double value = defaultValue;
        StrategyParam param = paramsLookUp.get(name);
        if (param != null) {
            value = param.getValue();
        }

        return value;
    }
}
