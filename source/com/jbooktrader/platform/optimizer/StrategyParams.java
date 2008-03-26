package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.model.JBookTraderException;

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


    public boolean equals(Object o) {
        if (!(o instanceof StrategyParams)) {
            return false;
        }

        StrategyParams that = (StrategyParams) o;
        boolean allSame = true;
        for (StrategyParam param : params) {
            int value = param.getValue();
            int thatValue = that.get(param.getName());
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

    public void add(String name, int min, int max, int step, int value) {
        StrategyParam param = new StrategyParam(name, min, max, step, value);
        params.add(param);
        paramsLookUp.put(name, param);
    }

    public int size() {
        return params.size();
    }

    public StrategyParam get(int index) {
        return params.get(index);
    }

    public int get(String name) {
        StrategyParam param = paramsLookUp.get(name);
        if (param == null) {
            throw new RuntimeException("Parameter " + name + " is not defined.");
        }
        return param.getValue();
    }

    public int getMin(String name) throws JBookTraderException {
        StrategyParam param = paramsLookUp.get(name);
        if (param == null) {
            throw new JBookTraderException("Parameter " + name + " is not defined.");
        }
        return param.getMin();
    }

    public int getMax(String name) throws JBookTraderException {
        StrategyParam param = paramsLookUp.get(name);
        if (param == null) {
            throw new JBookTraderException("Parameter " + name + " is not defined.");
        }
        return param.getMax();
    }

}
