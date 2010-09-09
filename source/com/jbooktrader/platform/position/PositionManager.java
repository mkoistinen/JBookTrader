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
 */
public class PositionManager {
    private final LinkedList<Position> positionsHistory;
    private final Strategy strategy;
    private final EventReport eventReport;
    private final TraderAssistant traderAssistant;
    private final PerformanceManager performanceManager;
    private int position;
    private double avgFillPrice, expectedFillPrice;

    public PositionManager(Strategy strategy) {
        this.strategy = strategy;
        positionsHistory = new LinkedList<Position>();
        eventReport = Dispatcher.getInstance().getEventReport();
        traderAssistant = Dispatcher.getInstance().getTrader().getAssistant();
        performanceManager = strategy.getPerformanceManager();
    }

    public LinkedList<Position> getPositionsHistory() {
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
        position += quantity;
        avgFillPrice = openOrder.getAvgFillPrice();


        performanceManager.updateOnTrade(quantity, avgFillPrice, position);

        Mode mode = Dispatcher.getInstance().getMode();
        if (mode == Mode.BackTest) {
            positionsHistory.add(new Position(openOrder.getDate(), position, avgFillPrice));
        }

        if (mode != Mode.Optimization) {
            strategy.getStrategyReportManager().report();
        }

        if (mode == Mode.ForwardTest || mode == Mode.Trade) {
            StringBuilder msg = new StringBuilder();
            msg.append("Order ").append(openOrder.getId()).append(" is filled.  ");
            msg.append("Avg Fill Price: ").append(avgFillPrice).append(". ");
            msg.append("Position: ").append(getPosition());
            eventReport.report(strategy.getName(), msg.toString());
        }
    }

    public void trade() {
        int newPosition = strategy.getPosition();
        int quantity = newPosition - position;
        if (quantity != 0) {
            Mode mode = Dispatcher.getInstance().getMode();
            if (mode == Mode.Trade || mode == Mode.ForwardTest) {
                Dispatcher.getInstance().getC2Manager().sendSignal(strategy.getName(), position, newPosition);
            }

            String action = (quantity > 0) ? "BUY" : "SELL";
            Contract contract = strategy.getContract();
            traderAssistant.placeMarketOrder(contract, Math.abs(quantity), action, strategy);
        }
    }
}
