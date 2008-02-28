package com.jbooktrader.platform.position;

import com.ib.client.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.performance.PerformanceManager;
import com.jbooktrader.platform.report.Report;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.trader.TraderAssistant;

import java.util.*;

/**
 * Position manager keeps track of current positions and executions.
 */
public class PositionManager {
    private final List<Position> positionsHistory;
    private final Strategy strategy;
    private final Report eventReport;
    private final TraderAssistant traderAssistant;
    private final PerformanceManager performanceManager;

    private int position;
    private double avgFillPrice;
    private volatile boolean orderExecutionPending;


    public PositionManager(Strategy strategy) throws JBookTraderException {
        this.strategy = strategy;
        positionsHistory = new ArrayList<Position>();
        eventReport = Dispatcher.getReporter();
        traderAssistant = Dispatcher.getTrader().getAssistant();
        performanceManager = strategy.getPerformanceManager();
    }

    public List<Position> getPositionsHistory() {
        return positionsHistory;
    }

    public int getPosition() {
        return position;
    }


    public void setAvgFillPrice(double avgFillPrice) {
        this.avgFillPrice = avgFillPrice;
    }

    public double getAvgFillPrice() {
        return avgFillPrice;
    }

    public synchronized void update(OpenOrder openOrder) {
        Order order = openOrder.getOrder();
        String action = order.m_action;
        int sharesFilled = openOrder.getSharesFilled();
        int quantity = 0;

        if (action.equals("SELL")) {
            quantity = -sharesFilled;
        }

        if (action.equals("BUY")) {
            quantity = sharesFilled;
        }

        // current position after the execution
        position += quantity;
        avgFillPrice = openOrder.getAvgFillPrice();

        positionsHistory.add(new Position(openOrder.getDate(), position, avgFillPrice));
        performanceManager.update(quantity, avgFillPrice, position);

        if ((Dispatcher.getMode() != Dispatcher.Mode.OPTIMIZATION)) {
            StringBuilder msg = new StringBuilder();
            msg.append(strategy.getName()).append(": ");
            msg.append("Order ").append(openOrder.getId()).append(" is filled.  ");
            msg.append("Avg Fill Price: ").append(avgFillPrice).append(". ");
            msg.append("Position: ").append(getPosition());
            eventReport.report(msg.toString());
            strategy.report();
        }


        orderExecutionPending = false;

    }

    public void trade() {
        if (traderAssistant.isConnected() && !orderExecutionPending) {
            int newPosition = strategy.getPosition();
            int quantity = newPosition - position;
            if (quantity != 0) {
                orderExecutionPending = true;
                String action = (quantity > 0) ? "BUY" : "SELL";
                Contract contract = strategy.getContract();
                traderAssistant.placeMarketOrder(contract, Math.abs(quantity), action, strategy);
            }
        }
    }
}
