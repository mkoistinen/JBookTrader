package com.jbooktrader.platform.optimizer;

import java.util.*;

/**
 * @author Eugene Kononov
 */
public class StrategyParams {
    private final List<StrategyParam> params;

    public StrategyParams() {
        params = new LinkedList<>();
    }

    public String getKey() {
        StringBuilder key = new StringBuilder();
        for (StrategyParam param : params) {
            if (key.length() > 0) {
                key.append("/");
            }
            key.append(param.getValue());
        }

        return key.toString();
    }

    // copy constructor
    public StrategyParams(StrategyParams params) {
        this.params = new LinkedList<>();
        for (StrategyParam param : params.getAll()) {
            StrategyParam paramCopy = new StrategyParam(param);
            this.params.add(paramCopy);
        }
    }

    public void add(String name, int min, int max, int step, int value) {
        StrategyParam param = new StrategyParam(name, min, max, step, value);
        params.add(param);
    }

    public List<StrategyParam> getAll() {
        return params;
    }

    public void add(StrategyParam strategyParam) {
        params.add(strategyParam);
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
