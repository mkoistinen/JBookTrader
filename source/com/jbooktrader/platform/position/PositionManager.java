package com.jbooktrader.platform.position;

import com.ib.client.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.report.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.trader.*;

import java.util.*;

/**
 * Position manager keeps track of current positions and executions.
 *
 * @author Eugene Kononov
 */
public class PositionManager {
    private final LinkedList<Position> positionsHistory;
    private final Strategy strategy;
    private final EventReport eventReport;
    private final TraderAssistant traderAssistant;
    private final PerformanceManager performanceManager;
    private int currentPosition, targetPosition;
    private double avgFillPrice, expectedFillPrice;

    public PositionManager(Strategy strategy) {
        this.strategy = strategy;
        positionsHistory = new LinkedList<>();
        eventReport = Dispatcher.getInstance().getEventReport();
        traderAssistant = Dispatcher.getInstance().getTrader().getAssistant();
        performanceManager = strategy.getPerformanceManager();
    }

    public LinkedList<Position> getPositionsHistory() {
        return positionsHistory;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public int getTargetPosition() {
        return targetPosition;
    }

    public void setTargetPosition(int targetPosition) {
        boolean isNewTargetPosition = (targetPosition != this.targetPosition);
        this.targetPosition = targetPosition;
        if (targetPosition != currentPosition && isNewTargetPosition) {
            Mode mode = Dispatcher.getInstance().getMode();
            if (mode == Mode.ForwardTest || mode == Mode.Trade || mode == Mode.ForceClose) {
                eventReport.report(strategy.getName(), "Target position is set to " + targetPosition);
            }
        }
    }

    public double getAvgFillPrice() {
        return avgFillPrice;
    }

    public void setExpectedFillPrice(double expectedFillPrice) {
        this.expectedFillPrice = expectedFillPrice;
    }

    public double getExpectedFillPrice() {
        return expectedFillPrice;
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
        currentPosition += quantity;
        avgFillPrice = openOrder.getAvgFillPrice();


        performanceManager.updateOnTrade(quantity, avgFillPrice, currentPosition);

        Mode mode = Dispatcher.getInstance().getMode();
        if (mode == Mode.BackTest) {
            positionsHistory.add(new Position(strategy.getMarketBook().getSnapshot().getTime(), currentPosition, avgFillPrice));
        }

        if (mode != Mode.Optimization) {
            strategy.getStrategyReportManager().report();
        }

        if (mode == Mode.ForwardTest || mode == Mode.Trade || mode == Mode.ForceClose) {
            StringBuilder msg = new StringBuilder();
            msg.append("Order ").append(openOrder.getId()).append(" is filled.  ");
            msg.append("Average fill price: ").append(avgFillPrice).append(". ");
            msg.append("Position: ").append(getCurrentPosition());
            eventReport.report(strategy.getName(), msg.toString());
        }
    }

    public void trade() {
        if (strategy.getMarketBook().isExchangeOpen()) {
            int quantity = targetPosition - currentPosition;
            if (quantity != 0) {
                String action = (quantity > 0) ? "BUY" : "SELL";
                Contract contract = strategy.getContract();
                traderAssistant.placeMarketOrder(contract, Math.abs(quantity), action, strategy);
            }
        }
    }
}
