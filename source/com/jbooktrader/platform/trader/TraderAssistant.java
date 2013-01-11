package com.jbooktrader.platform.trader;

import com.ib.client.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.portfolio.*;
import com.jbooktrader.platform.position.*;
import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.report.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.strategy.*;

import javax.swing.*;
import java.util.*;

import static com.jbooktrader.platform.preferences.JBTPreferences.*;

/**
 * @author Eugene Kononov
 */
public class TraderAssistant {
    private final Map<Integer, Strategy> strategies;
    private final Map<Integer, OpenOrder> openOrders;
    private final Map<String, Integer> tickers;
    private final Set<Integer> subscribedTickers;
    private final Map<Integer, MarketBook> marketBooks;
    private final EventReport eventReport;
    private final Trader trader;
    private final Dispatcher dispatcher;

    private EClientSocket socket;
    private int nextStrategyID, tickerId, orderID, serverVersion;
    private String accountCode;// used to determine if TWS is running against real or paper trading account
    private boolean isOrderExecutionPending;
    private boolean isMarketDataActive;


    public TraderAssistant(Trader trader) {
        this.trader = trader;
        dispatcher = Dispatcher.getInstance();
        eventReport = dispatcher.getEventReport();
        strategies = new HashMap<Integer, Strategy>();
        openOrders = new HashMap<Integer, OpenOrder>();
        tickers = new HashMap<String, Integer>();
        marketBooks = new HashMap<Integer, MarketBook>();
        subscribedTickers = new HashSet<Integer>();

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
        for (Map.Entry<Integer, Strategy> mapEntry : strategies.entrySet()) {
            Strategy strategy = mapEntry.getValue();
            if (strategy.getName().equals(name)) {
                return strategy;
            }
        }
        return null;
    }

    public void connect() throws JBookTraderException {
        if (socket == null || !socket.isConnected()) {
            eventReport.report(JBookTrader.APP_NAME, "Connecting to TWS");

            socket = new EClientSocket(trader);
            PreferencesHolder prefs = PreferencesHolder.getInstance();
            String host = prefs.get(Host);
            int port = prefs.getInt(Port);
            int clientID = prefs.getInt(ClientID);

            socket.eConnect(host, port, clientID);
            if (!socket.isConnected()) {
                throw new JBookTraderException("Could not connect to TWS. See report for details.");
            }

            // IB Log levels: 1=SYSTEM 2=ERROR 3=WARNING 4=INFORMATION 5=DETAIL
            socket.setServerLogLevel(3);
            socket.reqNewsBulletins(true);
            serverVersion = socket.serverVersion();
            eventReport.report(JBookTrader.APP_NAME, "Connected to TWS");
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
                eventReport.report(openOrder.getStrategy().getName(), "Requesting executions for open order " + openOrder.getId());
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

    public synchronized void requestMarketData(Strategy strategy) {
        Contract contract = strategy.getContract();
        String instrument = makeInstrument(contract);
        Integer ticker = tickers.get(instrument);
        if (!subscribedTickers.contains(ticker)) {
            subscribedTickers.add(ticker);
            socket.reqContractDetails(ticker, strategy.getContract());
            eventReport.report(JBookTrader.APP_NAME, "Requested contract details for instrument " + instrument);
            socket.reqMktDepth(ticker, contract, 10);
            eventReport.report(JBookTrader.APP_NAME, "Requested book data for instrument " + instrument);
            socket.reqMktData(ticker, contract, "", false);
            eventReport.report(JBookTrader.APP_NAME, "Requested market data for instrument " + instrument);
        }
    }

    public synchronized void cancelMarketData(Strategy strategy) {
        Contract contract = strategy.getContract();
        String instrument = makeInstrument(contract);
        Integer ticker = tickers.get(instrument);
        if (subscribedTickers.contains(ticker)) {
            socket.cancelMktDepth(ticker);
            eventReport.report(JBookTrader.APP_NAME, "Cancelled book data for instrument " + instrument);
            socket.cancelMktData(ticker);
            eventReport.report(JBookTrader.APP_NAME, "Cancelled market data for instrument " + instrument);
            subscribedTickers.remove(ticker);
        }
    }


    public synchronized void addStrategy(Strategy strategy) {
        strategy.setIndicatorManager(new IndicatorManager());
        strategy.setIndicators();
        nextStrategyID++;
        strategies.put(nextStrategyID, strategy);
        Mode mode = dispatcher.getMode();
        if (mode == Mode.ForwardTest || mode == Mode.Trade) {
            String msg = "Strategy started. Trading schedule: " + strategy.getTradingSchedule();
            eventReport.report(strategy.getName(), msg);
            requestMarketData(strategy);
            StrategyRunner.getInstance().addListener(strategy);
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

    public void resetOrderExecutionPending() {
        isOrderExecutionPending = false;
    }

    public void setIsMarketDataActive(boolean isMarketDataActive) {
        this.isMarketDataActive = isMarketDataActive;
    }

    public boolean getIsMarketDataActive() {
        return isMarketDataActive;
    }


    private synchronized void placeOrder(Contract contract, Order order, Strategy strategy) {
        try {
            if (isOrderExecutionPending) {
                return;
            }

            Mode mode = dispatcher.getMode();
            if (mode == Mode.Trade || mode == Mode.ForwardTest) {
                PortfolioManager portfolioManager = dispatcher.getPortfolioManager();
                if (!portfolioManager.canTrade(strategy.getName(), order.m_action)) {
                    return;
                }
            }

            long remainingTime = strategy.getTradingSchedule().getRemainingTime(strategy.getMarketBook().getSnapshot().getTime());
            long remainingMinutes = remainingTime / (1000 * 60);
            if (strategy.getPositionManager().getTargetPosition() != 0 && remainingMinutes < 15) {
                return;
            }


            isOrderExecutionPending = true;
            orderID++;

            if (mode == Mode.Trade || mode == Mode.ForwardTest) {
                String msg = "Placing order " + orderID;
                eventReport.report(strategy.getName(), msg);
            }

            openOrders.put(orderID, new OpenOrder(orderID, order, strategy));

            double midPrice = strategy.getMarketBook().getSnapshot().getPrice();
            double bidAskSpread = strategy.getBidAskSpread();
            double expectedFillPrice = order.m_action.equalsIgnoreCase("BUY") ? (midPrice + bidAskSpread / 2) : (midPrice - bidAskSpread / 2);
            strategy.getPositionManager().setExpectedFillPrice(expectedFillPrice);

            if (mode == Mode.Trade) {
                socket.placeOrder(orderID, contract, order);
            } else {
                Execution execution = new Execution();
                execution.m_shares = order.m_totalQuantity;
                execution.m_price = expectedFillPrice;
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
        boolean isRealTrading = !accountCode.startsWith("D") && Dispatcher.getInstance().getMode() == Mode.Trade;
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
