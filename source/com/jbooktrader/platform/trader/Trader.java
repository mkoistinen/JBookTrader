package com.jbooktrader.platform.trader;

import com.ib.client.*;
import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.position.*;
import com.jbooktrader.platform.report.*;
import com.jbooktrader.platform.strategy.*;

import java.util.*;

/**
 * This class acts as a "wrapper" in the IB's API terminology.
 */
public class Trader extends EWrapperAdapter {
    private final Report eventReport;
    private final TraderAssistant traderAssistant;
    private String previousErrorMessage;

    public Trader() {
        traderAssistant = new TraderAssistant(this);
        previousErrorMessage = "";
        eventReport = Dispatcher.getReporter();
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
        eventReport.report(newsBulletin);
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

            for (OpenOrder openOrder : openOrders.values()) {
                String msg = "Execution for order " + openOrder.getId() + " was not found.";
                msg += " In all likelihood, this is because the order was placed while TWS was disconnected from the server.";
                msg += " This order will be removed and another one will be submitted. The strategy will continue to run normally.";
                eventReport.report(msg);
                Strategy strategy = openOrder.getStrategy();
                PositionManager positionManager = strategy.getPositionManager();
                positionManager.resetOrderExecutionPending();
            }

            openOrders.clear();
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            eventReport.report(t);
        }
    }

    @Override
    public void contractDetails(int id, ContractDetails contractDetails) {
        String lineSep = "<br>";
        StringBuilder details = new StringBuilder("Contract details:").append(lineSep);
        details.append("Trading class: ").append(contractDetails.m_tradingClass).append(lineSep);
        details.append("Valid exchanges: ").append(contractDetails.m_validExchanges).append(lineSep);
        details.append("Long name: ").append(contractDetails.m_longName).append(lineSep);
        details.append("Market name: ").append(contractDetails.m_marketName).append(lineSep);
        details.append("Min tick: ").append(contractDetails.m_minTick).append(lineSep);
        eventReport.report(details.toString());
    }

    @Override
    public void error(Exception e) {
        eventReport.report(e.toString());
    }

    @Override
    public void error(String error) {
        eventReport.report(error);
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
            eventReport.report(msg);

            if (errorCode == 1100) {// Connectivity between IB and TWS has been lost.
                traderAssistant.setIsConnected(false);
            }

            // Errors 1101 and 1102 are sent when connectivity is restored. However, sometimes TWS fails
            // to send them, and in those situations we would be waiting forever. To avoid this, we also
            // listen for error code 2104 which indicates that market data is restored, which we can
            // interpret as restored connection.
            boolean isConnectivityRestored = (errorCode == 1101 || errorCode == 1102 || errorCode == 2104);
            if (isConnectivityRestored) {
                if (!traderAssistant.getOpenOrders().isEmpty()) {
                    eventReport.report("Checking for executions while TWS was disconnected from the IB server.");
                    traderAssistant.requestExecutions();
                }
                traderAssistant.setIsConnected(true);
            }

            if (errorCode == 317) {// Market depth data has been reset
                traderAssistant.getMarketBook(id).getMarketDepth().reset();
                eventReport.report("Market data for book " + id + " has been reset.");
            }

            // 200: bad contract
            // 309: market depth requested for more than 3 symbols
            boolean isInvalidRequest = (errorCode == 200 || errorCode == 309);
            if (isInvalidRequest) {
                Dispatcher.fireModelChanged(ModelListener.Event.Error, "IB reported: " + errorMsg);
            }


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
    public void nextValidId(int orderId) {
        traderAssistant.setOrderID(orderId);
    }

}
