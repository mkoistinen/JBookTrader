package com.jbooktrader.platform.position;

import com.ib.client.*;
import com.jbooktrader.platform.c2.*;
import com.jbooktrader.platform.model.*;
import static com.jbooktrader.platform.model.Dispatcher.Mode.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.report.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.trader.*;
import com.jbooktrader.platform.util.*;

import java.text.*;
import java.util.*;

/**
 * Position manager keeps track of current positions and executions.
 */
public class PositionManager {
    private static final String LINE_SEP = System.getProperty("line.separator");
    private final NumberFormat nf2;
    private final LinkedList<Position> positionsHistory;
    private final Strategy strategy;
    private final Report eventReport;
    private final TraderAssistant traderAssistant;
    private final PerformanceManager performanceManager;
    private final SimpleDateFormat simpleDateFormat;

    private int position;
    private double avgFillPrice;
    private volatile boolean orderExecutionPending;

    public PositionManager(Strategy strategy) {
        this.strategy = strategy;
        positionsHistory = new LinkedList<Position>();
        eventReport = Dispatcher.getReporter();
        traderAssistant = Dispatcher.getTrader().getAssistant();
        performanceManager = strategy.getPerformanceManager();
        nf2 = NumberFormatterFactory.getNumberFormatter(2);
        simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        simpleDateFormat.setTimeZone(strategy.getTradingSchedule().getTimeZone());
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
        orderExecutionPending = false;

        Dispatcher.Mode mode = Dispatcher.getMode();
        if (mode != Optimization) {
            positionsHistory.add(new Position(openOrder.getDate(), position, avgFillPrice));
            strategy.getStrategyReportManager().report();
        }

        if (mode == ForwardTest || mode == Trade) {
            StringBuilder msg = new StringBuilder();
            msg.append(strategy.getName()).append(": ");
            msg.append("Order ").append(openOrder.getId()).append(" is filled.  ");
            msg.append("Avg Fill Price: ").append(avgFillPrice).append(". ");
            msg.append("Position: ").append(getPosition());
            eventReport.report(msg.toString());

            // remote email notification, if enabled
            boolean isCompletedTrade = performanceManager.getIsCompletedTrade();
            String notification = "Event type: Trade" + LINE_SEP;
            notification += "Time sent: " + simpleDateFormat.format(System.currentTimeMillis()) + LINE_SEP;
            notification += "Strategy: " + strategy.getName() + LINE_SEP;
            notification += "Position: " + position + LINE_SEP;
            notification += "Price: " + avgFillPrice + LINE_SEP;
            notification += "Trades: " + nf2.format(performanceManager.getTrades()) + LINE_SEP;
            String tradeNetProfit = isCompletedTrade ? nf2.format(performanceManager.getTradeProfit()) : "--";
            notification += "Trade net profit: " + tradeNetProfit + LINE_SEP;
            notification += "Total net profit: " + nf2.format(performanceManager.getNetProfit()) + LINE_SEP;

            SecureMailSender.getInstance().send(notification);
        }
    }

    public void trade() {
        if (traderAssistant.isConnected() && !orderExecutionPending) {
            int newPosition = strategy.getPosition();
            int quantity = newPosition - position;
            if (quantity != 0) {
                if (strategy.isC2enabled()) {
                    Dispatcher.Mode mode = Dispatcher.getMode();
                    if (mode == Trade || mode == ForwardTest) {
                        Collective2Gateway c2g = new Collective2Gateway(strategy.getC2SystemId());
                        c2g.send(position, newPosition);
                    }
                }

                orderExecutionPending = true;
                String action = (quantity > 0) ? "BUY" : "SELL";
                Contract contract = strategy.getContract();
                traderAssistant.placeMarketOrder(contract, Math.abs(quantity), action, strategy);

            }
        }
    }
}
