package com.jbooktrader.platform.trader;

import com.ib.client.*;
import com.jbooktrader.platform.email.*;
import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.model.ModelListener.*;
import com.jbooktrader.platform.position.*;
import com.jbooktrader.platform.report.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.strategy.*;

import java.util.*;

/**
 * This class acts as a "wrapper" in the IB's API terminology.
 *
 * @author Eugene Kononov
 */
public class Trader extends EWrapperAdapter {
    private final EventReport eventReport;
    private final TraderAssistant traderAssistant;
    private String previousErrorMessage;

    public Trader() {
        traderAssistant = new TraderAssistant(this);
        previousErrorMessage = "";
        eventReport = Dispatcher.getInstance().getEventReport();
    }

    public TraderAssistant getAssistant() {
        return traderAssistant;
    }

    @Override
    public void updateAccountValue(String key, String value, String currency, String accountName) {
        try {
            if (key.equalsIgnoreCase("AccountCode")) {
                synchronized (this) {
                    traderAssistant.setAccountCode(value);
                    notifyAll();
                }
            }
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            eventReport.report(t);
        }
    }

    @Override
    public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {
        String newsBulletin = "Msg ID: " + msgId + " Msg Type: " + msgType + " Msg: " + message + " Exchange: " + origExchange;
        eventReport.report("IB API", newsBulletin);
    }


    @Override
    public void execDetails(int reqId, Contract contract, Execution execution) {
        try {
            int orderId = execution.m_orderId;
            Map<Integer, OpenOrder> openOrders = traderAssistant.getOpenOrders();
            OpenOrder openOrder = openOrders.get(orderId);
            if (openOrder != null) {
                openOrder.add(execution);
                if (openOrder.isFilled()) {
                    Strategy strategy = openOrder.getStrategy();
                    PositionManager positionManager = strategy.getPositionManager();
                    positionManager.update(openOrder);
                    openOrders.remove(orderId);
                    traderAssistant.resetOrderExecutionPending();
                }
            }
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            eventReport.report(t);
        }
    }

    @Override
    public void execDetailsEnd(int reqId) {
        try {
            Map<Integer, OpenOrder> openOrders = traderAssistant.getOpenOrders();
            String msg = "Execution for this order was not found.";
            msg += " In all likelihood, this is because the order was placed while TWS was disconnected from the server.";
            msg += " This order will be removed and another one will be submitted. The strategy will continue to run normally.";

            for (OpenOrder openOrder : openOrders.values()) {
                String orderMsg = "Order " + openOrder.getId() + ": " + msg;
                eventReport.report(openOrder.getStrategy().getName(), orderMsg);
            }

            openOrders.clear();
            traderAssistant.resetOrderExecutionPending();

        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            eventReport.report(t);
        }
    }

    @Override
    public void contractDetails(int id, ContractDetails cd) {
        String lineSep = "<br>";
        StringBuilder details = new StringBuilder("Contract details:").append(lineSep);
        details.append("ID: ").append(id).append(lineSep);
        details.append("Trading class: ").append(cd.m_tradingClass).append(lineSep);
        details.append("Exchanges: ").append(cd.m_validExchanges).append(lineSep);
        details.append("Long name: ").append(cd.m_longName).append(lineSep);
        details.append("Market name: ").append(cd.m_marketName).append(lineSep);
        details.append("Minimum tick: ").append(cd.m_minTick).append(lineSep);
        details.append("Contract month: ").append(cd.m_contractMonth).append(lineSep);
        details.append("Time zone id: ").append(cd.m_timeZoneId).append(lineSep);
        details.append("Trading hours: ").append(cd.m_tradingHours).append(lineSep);
        details.append("Liquid hours: ").append(cd.m_liquidHours).append(lineSep);

        if (cd.m_liquidHours.contains("closed") || cd.m_liquidHours.contains("CLOSED")) {
            traderAssistant.getMarketBook(id).setExchangeOpen(false);
            String msg = "Exchanges " + cd.m_validExchanges + " are either closed or will be closed shortly today ";
            msg += "for ticker " + cd.m_marketName + ". JBT will continue to operate normally, ";
            msg += "but no trades for " + cd.m_marketName + " will be placed.";
            eventReport.report("IB API", msg);
        } else {
            traderAssistant.getMarketBook(id).setExchangeOpen(true);
        }

        eventReport.report("IB API", details.toString());
    }

    @Override
    public void error(Exception e) {
        eventReport.report("IB API", e.toString());
    }

    @Override
    public void error(String error) {
        eventReport.report("IB API", error);
    }

    @Override
    public void error(int id, int errorCode, String errorMsg) {
        try {
            String msg = errorCode + ": " + errorMsg;
            if (id != -1) {
                msg += " (for id " + id + ")";
            }

            if (msg.equals(previousErrorMessage)) {
                // ignore duplicate error messages
                return;
            }

            previousErrorMessage = msg;
            boolean isReportable = (errorCode != 200 && errorCode != 300);
            if (isReportable) {
                eventReport.report("IB API", msg);
            }

            // Errors 1101 and 1102 are sent when connectivity is restored.
            boolean isConnectivityRestored = (errorCode == 1101 || errorCode == 1102);
            if (isConnectivityRestored) {
                if (!traderAssistant.getOpenOrders().isEmpty()) {
                    eventReport.report(JBookTrader.APP_NAME, "Checking for executions while TWS was disconnected from the IB server.");
                    traderAssistant.requestExecutions();
                }
            }

            if (errorCode == 317) {// Market depth data has been reset
                traderAssistant.getMarketBook(id).getMarketDepth().reset();
                eventReport.report(JBookTrader.APP_NAME, "Market data for book " + id + " has been reset.");
            }


            if (errorCode == 202) { // Order Canceled - reason:Can't handle negative priced order
                traderAssistant.getOpenOrders().remove(id);
                traderAssistant.resetOrderExecutionPending();
                String reportMsg = "Removed order " + id + " because IB reported error " + errorCode + ". ";
                reportMsg += "Another order will be submitted. The strategy will continue to run normally.";
                eventReport.report(JBookTrader.APP_NAME, reportMsg);
            }

            if (errorCode == 201) { // Order rejected: insufficient margin
                OpenOrder openOrder = traderAssistant.getOpenOrders().get(id);
                Strategy strategy = openOrder.getStrategy();
                strategy.disable();
                traderAssistant.getOpenOrders().remove(id);
                traderAssistant.resetOrderExecutionPending();
                String reportMsg = "Removed order " + id + " because it was rejected. Error code: " + errorCode + ". ";
                reportMsg += "Strategy " + strategy.getName() + " has been disabled. ";
                reportMsg += "Other strategies will continue to run normally.";
                eventReport.report(JBookTrader.APP_NAME, reportMsg);
            }


            if (errorCode == 2104) { // Market data farm connection is OK
                traderAssistant.setIsMarketDataActive(true);
            }

            if (errorCode == 1100) { // Connectivity between IB and Trader Workstation has been lost.
                traderAssistant.setIsMarketDataActive(false);
            }

            // 200: bad contract
            if (errorCode == 200) {
                traderAssistant.volumeResponse(id, -1);
            }

            // 309: market depth requested for more than 3 symbols
            if (errorCode == 309) {
                Dispatcher.getInstance().fireModelChanged(Event.Error, "IB reported: " + errorMsg);
            }


            boolean isNotificationNeeded = (errorCode < 2103 || errorCode > 2106) && (errorCode != 2100 && errorCode != 200 && errorCode != 300);
            if (isNotificationNeeded) {
                Notifier.getInstance().send(msg);
            }
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            eventReport.report(t);
        }
    }

    @Override
    public void tickSnapshotEnd(int tickerId) {
        try {
            traderAssistant.volumeResponse(tickerId, 0);
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            eventReport.report(t);
        }
    }


    @Override
    public void updateMktDepth(int tickerId, int position, int operation, int side, double price, int size) {
        try {
            MarketDepth marketDepth = traderAssistant.getMarketBook(tickerId).getMarketDepth();
            marketDepth.update(position, MarketDepthOperation.getOperation(operation), MarketDepthSide.getSide(side), price, size);
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            eventReport.report(t);
        }
    }

    @Override
    public void tickSize(int tickerId, int tickType, int size) {
        try {
            if (tickType == TickType.VOLUME && size != 0) {
                traderAssistant.volumeResponse(tickerId, size);
                MarketBook marketBook = traderAssistant.getMarketBook(tickerId);
                if (marketBook != null) {
                    MarketDepth marketDepth = traderAssistant.getMarketBook(tickerId).getMarketDepth();
                    marketDepth.update(size);
                }
            }
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            eventReport.report(t);
        }
    }

    @Override
    public void nextValidId(int orderId) {
        traderAssistant.setOrderID(orderId);
    }

}
