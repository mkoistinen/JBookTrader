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

    public int get(String name) throws JBookTraderException {
        StrategyParam param = paramsLookUp.get(name);
        if (param == null) {
            throw new JBookTraderException("Parameter " + name + " is not defined.");
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
