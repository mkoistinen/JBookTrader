package com.jbooktrader.platform.optimizer;

import java.util.*;

/**
 */
public class StrategyParams {
    private final List<StrategyParam> params;

    public StrategyParams() {
        params = new ArrayList<StrategyParam>();
    }

    public boolean equals(Object o) {
        if (!(o instanceof StrategyParams)) {
            return false;
        }

        StrategyParams that = (StrategyParams) o;
        boolean allSame = true;
        for (StrategyParam param : params) {
            int value = param.getValue();
            int thatValue = that.get(param.getName()).getValue();
            if (value != thatValue) {
                allSame = false;
                break;
            }
        }

        return allSame;
    }

    public int hashCode() {
        int hashCode = 17;
        for (StrategyParam param : params) {
            int value = param.getValue();
            hashCode = 37 * hashCode + value;
        }
        return hashCode;
    }


    // copy constructor
    public StrategyParams(StrategyParams params) {
        this.params = new ArrayList<StrategyParam>();
        for (StrategyParam param : params.getAll()) {
            StrategyParam paramCopy = new StrategyParam(param);
            this.params.add(paramCopy);
        }
    }

    public List<StrategyParam> getAll() {
        return params;
    }

    public void add(String name, int min, int max, int step, int value) {
        StrategyParam param = new StrategyParam(name, min, max, step, value);
        params.add(param);
    }

    public int size() {
        return params.size();
    }

    public StrategyParam get(int index) {
        return params.get(index);
    }


    public StrategyParam get(String name) {
        for (StrategyParam param : params) {
            if (param.getName().equals(name)) {
                return param;
            }
        }
        throw new RuntimeException("Parameter " + name + " is not defined.");
    }
}
