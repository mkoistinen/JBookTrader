package com.jbooktrader.platform.trader;

import com.ib.client.*;
import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;
import static com.jbooktrader.platform.model.Dispatcher.Mode.*;
import com.jbooktrader.platform.position.*;
import static com.jbooktrader.platform.preferences.JBTPreferences.*;
import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.report.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;

import javax.swing.*;
import java.util.*;


public class TraderAssistant {
    private final String host;
    private final int port, clientID;
    private final Map<Integer, Strategy> strategies;
    private final Map<Integer, OpenOrder> openOrders;
    private final Map<String, Integer> tickers;
    private final HashSet<Integer> subscribedTickers;
    private final Map<Integer, MarketBook> marketBooks;
    private final Report eventReport;
    private final Trader trader;

    private EClientSocket socket;
    private int nextStrategyID, tickerId, orderID, serverVersion;
    private String accountCode;// used to determine if TWS is running against real or paper trading account
    private boolean isConnected;
    private double bidAskSpread;

    public TraderAssistant(Trader trader) {
        this.trader = trader;

        eventReport = Dispatcher.getReporter();
        strategies = new HashMap<Integer, Strategy>();
        openOrders = new HashMap<Integer, OpenOrder>();
        tickers = new HashMap<String, Integer>();
        marketBooks = new HashMap<Integer, MarketBook>();
        subscribedTickers = new HashSet<Integer>();

        PreferencesHolder prefs = PreferencesHolder.getInstance();
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

    public Collection<Strategy> getAllStrategies() {
        return strategies.values();
    }


    public MarketBook getMarketBook(int tickerId) {
        return marketBooks.get(tickerId);
    }

    public Map<Integer, MarketBook> getAllMarketBooks() {
        return marketBooks;
    }


    public Strategy getStrategy(String name) {
        Strategy strategy = null;
        for (Map.Entry<Integer, Strategy> mapEntry : strategies.entrySet()) {
            Strategy thisStrategy = mapEntry.getValue();
            if (thisStrategy.getName().equals(name)) {
                strategy = thisStrategy;
                break;
            }
        }
        return strategy;
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
            socket.setServerLogLevel(3);
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
            for (OpenOrder openOrder : openOrders.values()) {
                openOrder.reset();
                eventReport.report("Requesting executions for open order " + openOrder.getId());
                socket.reqExecutions(openOrder.getId(), new ExecutionFilter());
            }
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            eventReport.report(t);
        }
    }

    public String makeInstrument(Contract contract) {
        String instrument = contract.m_symbol;
        if (contract.m_currency != null) {
            instrument += "-" + contract.m_currency;
        }
        if (contract.m_exchange != null) {
            instrument += "-" + contract.m_exchange;
        }
        if (contract.m_secType != null) {
            instrument += "-" + contract.m_secType;
        }
        if (contract.m_expiry != null) {
            instrument += "-" + contract.m_expiry;
        }

        return instrument;
    }

    public synchronized MarketBook createMarketBook(Strategy strategy) {
        Contract contract = strategy.getContract();

        String instrument = makeInstrument(contract);
        Integer ticker = tickers.get(instrument);
        MarketBook marketBook;
        if (ticker == null) {
            marketBook = new MarketBook(instrument, strategy.getTradingSchedule().getTimeZone());
            tickerId++;
            tickers.put(instrument, tickerId);
            marketBooks.put(tickerId, marketBook);
        } else {
            marketBook = marketBooks.get(ticker);
        }

        return marketBook;
    }

    private synchronized void requestMarketData(Strategy strategy) {
        Contract contract = strategy.getContract();
        String instrument = makeInstrument(contract);
        Integer ticker = tickers.get(instrument);
        if (!subscribedTickers.contains(ticker)) {
            subscribedTickers.add(ticker);
            socket.reqContractDetails(strategy.getContract().m_conId, strategy.getContract());
            String msg = "Requested contract details for instrument " + instrument;
            eventReport.report(msg);
            socket.reqMktDepth(ticker, contract, 10);
            msg = "Requested market depth for instrument " + instrument;
            eventReport.report(msg);
        }
    }

    public synchronized void addStrategy(Strategy strategy) {
        strategy.getIndicatorManager().setMarketBook(strategy.getMarketBook());
        nextStrategyID++;
        strategies.put(nextStrategyID, strategy);
        Dispatcher.Mode mode = Dispatcher.getMode();
        if (mode == ForwardTest || mode == Trade) {
            String msg = strategy.getName() + ": strategy started. " + strategy.getTradingSchedule();
            eventReport.report(msg);
            strategy.setCollective2();
            requestMarketData(strategy);
            StrategyRunner.getInstance().addListener(strategy);
            strategy.setIsActive(true);
            Dispatcher.strategyStarted();
        }
    }

    public synchronized void removeAllStrategies() {
        strategies.clear();
        openOrders.clear();
        tickers.clear();
        subscribedTickers.clear();
        marketBooks.clear();
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public void setBidAskSpread(double bidAskSpread) {
        this.bidAskSpread = bidAskSpread;
    }

    private synchronized void placeOrder(Contract contract, Order order, Strategy strategy) {
        try {
            orderID++;
            Dispatcher.Mode mode = Dispatcher.getMode();
            if (mode == Trade || mode == ForwardTest) {
                String msg = strategy.getName() + ": Placing order " + orderID;
                eventReport.report(msg);
            }

            openOrders.put(orderID, new OpenOrder(orderID, order, strategy));

            if (mode == Trade) {
                socket.placeOrder(orderID, contract, order);
            } else {
                Execution execution = new Execution();
                execution.m_shares = order.m_totalQuantity;
                double price = strategy.getMarketBook().getSnapshot().getPrice();
                execution.m_price = order.m_action.equalsIgnoreCase("BUY") ? (price + bidAskSpread / 2) : (price - bidAskSpread / 2);
                execution.m_orderId = orderID;
                trader.execDetails(0, contract, execution);
            }
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            eventReport.report(t);
        }
    }

    public void placeMarketOrder(Contract contract, int quantity, String action, Strategy strategy) {
        Order order = new Order();
        order.m_overridePercentageConstraints = true;
        order.m_action = action;
        order.m_totalQuantity = quantity;
        order.m_orderType = "MKT";
        placeOrder(contract, order, strategy);
    }


    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public boolean isConnected() {
        Dispatcher.Mode mode = Dispatcher.getMode();
        return mode == BackTest || mode == Optimization || isConnected;
    }

    public void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    private void checkAccountType() throws JBookTraderException {
        socket.reqAccountUpdates(true, "");

        try {
            synchronized (trader) {
                while (accountCode == null) {
                    trader.wait();
                }
            }
        } catch (InterruptedException ie) {
            throw new JBookTraderException(ie);
        }

        socket.reqAccountUpdates(false, "");
        boolean isRealTrading = !accountCode.startsWith("D") && Dispatcher.getMode() == Trade;
        if (isRealTrading) {
            String lineSep = System.getProperty("line.separator");
            String warning = "Connected to a real (not simulated) IB account. ";
            warning += "Running " + JBookTrader.APP_NAME + " in trading mode against a real" + lineSep;
            warning += "account may cause significant losses in your account. ";
            warning += "Are you sure you want to proceed?";
            int response = JOptionPane.showConfirmDialog(null, warning, JBookTrader.APP_NAME, JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.NO_OPTION) {
                disconnect();
            }
        }
    }

}
