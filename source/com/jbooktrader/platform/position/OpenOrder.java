package com.jbooktrader.platform.position;

import com.ib.client.*;
import com.jbooktrader.platform.strategy.*;

/**
 * Encapsulates the order execution information.
 *
 * @author Eugene Kononov
 */
public class OpenOrder {
    private final int id;
    private final Order order;
    private final Strategy strategy;
    private int sharesFilled;
    private boolean isFilled;
    private double avgFillPrice;

    public OpenOrder(int id, Order order, Strategy strategy) {
        this.id = id;
        this.order = order;
        this.strategy = strategy;
    }

    public int getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public void add(Execution execution) {
        sharesFilled += execution.m_shares;
        avgFillPrice += execution.m_price * execution.m_shares;

        if (sharesFilled == order.m_totalQuantity) {
            avgFillPrice /= sharesFilled;
            isFilled = true;
        }
    }

    public void reset() {
        sharesFilled = 0;
        avgFillPrice = 0;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public boolean isFilled() {
        return isFilled;
    }

    public int getSharesFilled() {
        return sharesFilled;
    }

    public double getAvgFillPrice() {
        return avgFillPrice;
    }

}
