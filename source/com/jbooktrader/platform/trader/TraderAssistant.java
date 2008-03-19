package com.jbooktrader.platform.trader;

import com.ib.client.*;
import com.jbooktrader.platform.marketdepth.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.position.OpenOrder;
import static com.jbooktrader.platform.preferences.JBTPreferences.*;
import com.jbooktrader.platform.preferences.PreferencesHolder;
import com.jbooktrader.platform.report.Report;
import com.jbooktrader.platform.startup.JBookTrader;
import com.jbooktrader.platform.strategy.Strategy;

import javax.swing.*;
import java.util.*;


public class TraderAssistant {
    private final String host, advisorAccountID;
    private final int port, clientID;

    private static EClientSocket socket;
    private final Map<Integer, Strategy> strategies;
    private final Map<Integer, OpenOrder> openOrders;
    private final Report eventReport;
    private int nextStrategyID, orderID, serverVersion;
    private String accountCode;// used to determine if TWS is running against real or paper trading account
    private final Trader trader;
    private boolean isConnected;

    public TraderAssistant(Trader trader) throws JBookTraderException {
        this.trader = trader;

        eventReport = Dispatcher.getReporter();
        strategies = new HashMap<Integer, Strategy>();
        openOrders = new HashMap<Integer, OpenOrder>();

        PreferencesHolder prefs = PreferencesHolder.getInstance();

        boolean isAdvisorAccountUsed = prefs.get(AccountType).equalsIgnoreCase("advisor");
        advisorAccountID = (isAdvisorAccountUsed) ? prefs.get(AdvisorAccount) : "";

        host = prefs.get(Host);
        port = Integer.valueOf(prefs.get(Port));
        clientID = Integer.valueOf(prefs.get(ClientID));
    }

    public Map<Integer, OpenOrder> getOpenOrders() {
        return openOrders;
    }

    public Strategy getStrategy(int strategyId) {
        return strategies.get(strategyId);
    }

    public void connect() throws JBookTraderException {
        if (socket == null || !socket.isConnected()) {
            eventReport.report("Connecting to TWS");

            socket = new EClientSocket(trader);
            socket.eConnect(host, port, clientID);
            if (!socket.isConnected()) {
                throw new JBookTraderException("Could not connect to TWS. See report for details.");
            }

            // IB Log levels: 1=SYSTEM 2=ERROR 3=WARNING 4=INFORMATION 5=DETAIL
            socket.setServerLogLevel(2);
            socket.reqNewsBulletins(true);
            serverVersion = socket.serverVersion();
            isConnected = true;


            eventReport.report("Connected to TWS");
            checkAccountType();
        }
    }

    public int getServerVersion() {
        return serverVersion;
    }


    public void disconnect() {
        for (int srategyId : strategies.keySet()) {
            Strategy strategy = strategies.get(srategyId);
            strategy.setIsActive(false);
            MarketBook marketBook = strategy.getMarketBook();
            synchronized (marketBook) {
                marketBook.notifyAll();
            }
        }
        if (socket != null && socket.isConnected()) {
            socket.cancelNewsBulletins();
            socket.eDisconnect();
        }
    }

    /**
     * While TWS was disconnected from the IB server, some order executions may have occured.
     * To detect executions, request them explicitly after the reconnection.
     */
    public void requestExecutions() {
        try {
            eventReport.report("Requested executions.");
            for (OpenOrder openOrder : openOrders.values()) {
                openOrder.reset();
            }
            socket.reqExecutions(new ExecutionFilter());
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            eventReport.report(t);
        }
    }


    public void requestMarketDepth(Strategy strategy, int rows) {
        socket.reqMktDepth(strategy.getId(), strategy.getContract(), rows);
        String msg = strategy.getName() + ": " + "Requested market depth";
        eventReport.report(msg);
    }

    public synchronized void addStrategy(Strategy strategy) {
        nextStrategyID++;
        strategy.setId(nextStrategyID);
        strategies.put(nextStrategyID, strategy);
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    private synchronized void placeOrder(Contract contract, Order order, Strategy strategy) {
        try {
            orderID++;
            String msg = strategy.getName() + ": Placed order " + orderID;
            openOrders.put(orderID, new OpenOrder(orderID, order, strategy));

            if (Dispatcher.getMode() == Dispatcher.Mode.TRADE) {
                socket.placeOrder(orderID, contract, order);
                eventReport.report(msg);
            } else {
                MarketDepth md = strategy.getMarketBook().getLastMarketDepth();
                Execution execution = new Execution();
                execution.m_shares = order.m_totalQuantity;
                execution.m_price = order.m_action.equalsIgnoreCase("BUY") ? md.getBestAsk() : md.getBestBid();
                eventReport.report(msg);
                trader.execDetails(orderID, contract, execution);
            }

        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            eventReport.report(t);
        }
    }

    public void placeMarketOrder(Contract contract, int quantity, String action, Strategy strategy) {
        Order order = new Order();
        order.m_action = action;
        order.m_totalQuantity = quantity;
        order.m_orderType = "MKT";
        if (advisorAccountID.length() != 0) {
            order.m_account = advisorAccountID;
        }
        placeOrder(contract, order, strategy);
    }


    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public boolean isConnected() {
        Dispatcher.Mode mode = Dispatcher.getMode();

        if (mode == Dispatcher.Mode.BACK_TEST || mode == Dispatcher.Mode.OPTIMIZATION) {
            return true;
        } else {
            return isConnected;
        }
    }

    public void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    private boolean checkAccountType() throws JBookTraderException {
        boolean isAccepted = true;
        socket.reqAccountUpdates(true, advisorAccountID);

        try {
            synchronized (trader) {
                while (accountCode == null) {
                    trader.wait();
                }
            }
        } catch (InterruptedException ie) {
            throw new JBookTraderException(ie);
        }

        socket.reqAccountUpdates(false, advisorAccountID);
        boolean isRealTrading = !accountCode.startsWith("D") && Dispatcher.getMode() == Dispatcher.Mode.TRADE;
        if (isRealTrading) {
            String lineSep = System.getProperty("line.separator");
            String warning = "Connected to a real (not simulated) IB account. ";
            warning += "Running " + JBookTrader.APP_NAME + " in trading mode against a real" + lineSep;
            warning += "account may cause significant losses in your account. ";
            warning += "Are you sure you want to proceed?";
            int response = JOptionPane.showConfirmDialog(null, warning, JBookTrader.APP_NAME, JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.NO_OPTION) {
                isAccepted = false;
                disconnect();
            }
        }

        return isAccepted;
    }

}
