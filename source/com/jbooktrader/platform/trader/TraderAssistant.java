package com.jbooktrader.platform.trader;

import com.ib.client.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.position.*;
import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.report.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.ui.*;

import javax.swing.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;

import static com.jbooktrader.platform.preferences.JBTPreferences.*;

/**
 * @author Eugene Kononov
 */
public class TraderAssistant {
    private final static int CONTRACT_MONTHS = 7;
    private final Map<Integer, Strategy> strategies;
    private final Map<Integer, OpenOrder> openOrders;
    private final Map<String, Integer> tickers;
    private final Map<Integer, Integer> volumes;
    private final Map<Integer, String> expirations;
    private final Map<Integer, String> subscribedTickers;
    private final Map<Integer, MarketBook> marketBooks;
    private final EventReport eventReport;
    private final Trader trader;
    private final Dispatcher dispatcher;
    private final String faSubAccount;
    private final long maxDisconnectionTimeSeconds;

    private EClientSocket socket;
    private int nextStrategyID, tickerId, orderID, serverVersion;
    private String accountCode;// used to determine if TWS is running against real or paper trading account
    private boolean isOrderExecutionPending;
    private boolean isMarketDataActive;
    private long disconnectionTime;
    private final BlockingQueue<String> queue;

    public TraderAssistant(Trader trader) {
        this.trader = trader;
        dispatcher = Dispatcher.getInstance();
        eventReport = dispatcher.getEventReport();
        strategies = new HashMap<>();
        openOrders = new HashMap<>();
        tickers = new HashMap<>();
        volumes = new HashMap<>();
        expirations = new HashMap<>();
        marketBooks = new HashMap<>();
        subscribedTickers = new HashMap<>();
        faSubAccount = PreferencesHolder.getInstance().get(SubAccount);
        maxDisconnectionTimeSeconds = Long.parseLong(PreferencesHolder.getInstance().get(MaxDisconnectionPeriod));
        queue = new ArrayBlockingQueue<>(1);
    }

    public Map<Integer, OpenOrder> getOpenOrders() {
        return openOrders;
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

    public void volumeResponse(int id, int volume) throws InterruptedException {
        if (queue.peek() == null && expirations.containsKey(id)) {
            if (!volumes.containsKey(id)) {
                volumes.put(id, volume);
            }
            if (volumes.size() == CONTRACT_MONTHS) {
                queue.put("done");
            }
        }
    }

    public void setMostLiquidContract(Contract contract) throws InterruptedException {
        if (!contract.m_secType.equalsIgnoreCase("FUT")) {
            return;
        }

        queue.clear();
        expirations.clear();
        volumes.clear();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMM");
        int lastTickerId = tickerId + CONTRACT_MONTHS;

        while (tickerId < lastTickerId) {
            tickerId++;
            contract.m_expiry = dateFormat.format(calendar.getTime());
            expirations.put(tickerId, contract.m_expiry);
            socket.reqMktData(tickerId, contract, "", true);
            calendar.add(Calendar.MONTH, 1);
        }

        queue.take();

        String mostLiquidExpiration = null;
        int maxVolume = 0;
        boolean isValidContract = false;
        for (Map.Entry<Integer, Integer> entry : volumes.entrySet()) {
            Integer ticker = entry.getKey();
            Integer volume = entry.getValue();
            if (volume != -1) {
                isValidContract = true;
            }
            if (volume > maxVolume) {
                mostLiquidExpiration = expirations.get(ticker);
                maxVolume = volume;
            }
        }

        if (!isValidContract) {
            String msg = "Contract " + contract.m_symbol + " for exchange " + contract.m_exchange + " does not exist.";
            msg += " Make sure that the ticker symbol and the exchange are specified correctly.";
            eventReport.report(JBookTrader.APP_NAME, msg);
            throw new RuntimeException(msg);
        }

        if (mostLiquidExpiration == null) {
            String msg = "Unable to determine the most liquid " + contract.m_symbol + " contract because no trading volume was reported. Please try again when trading resumes.";
            eventReport.report(JBookTrader.APP_NAME, msg);
            throw new RuntimeException(msg);
        }

        contract.m_expiry = mostLiquidExpiration;
        eventReport.report(JBookTrader.APP_NAME, "The most liquid " + contract.m_symbol + " contract was determined as " + mostLiquidExpiration + ". Volume: " + maxVolume + ".");
    }

    public synchronized void requestMarketData(Strategy strategy) throws InterruptedException {

        Contract contract = strategy.getContract();
        String instrument = makeInstrument(contract);
        Integer ticker = tickers.get(instrument);
        if (!subscribedTickers.containsKey(ticker)) {
            setMostLiquidContract(strategy.getContract());
            subscribedTickers.put(ticker, strategy.getContract().m_expiry);
            socket.reqContractDetails(ticker, strategy.getContract());
            eventReport.report(JBookTrader.APP_NAME, "Requested contract details for instrument " + instrument);
            socket.reqMktDepth(ticker, contract, 10);
            eventReport.report(JBookTrader.APP_NAME, "Requested book data for instrument " + instrument);
            socket.reqMktData(ticker, contract, "", false);
            eventReport.report(JBookTrader.APP_NAME, "Requested market data for instrument " + instrument);
        } else {
            strategy.getContract().m_expiry = subscribedTickers.get(ticker);
        }
        Dispatcher.getInstance().fireModelChanged(ModelListener.Event.ExpirationUpdate, strategy);
    }

    public synchronized void addStrategy(Strategy strategy) throws InterruptedException {
        try {
            Mode mode = dispatcher.getMode();
            if (mode == Mode.ForwardTest || mode == Mode.Trade) {
                requestMarketData(strategy);
            }

            strategy.setIndicatorManager(new IndicatorManager());
            strategy.setIndicators();
            nextStrategyID++;
            strategies.put(nextStrategyID, strategy);

            if (mode == Mode.ForwardTest || mode == Mode.Trade) {
                String msg = "Strategy started. Trading schedule: " + strategy.getTradingSchedule();
                eventReport.report(strategy.getName(), msg);
                StrategyRunner.getInstance().addListener(strategy);
            }
        } catch (Exception e) {
            MessageDialog.showMessage(e.getMessage());
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

    public void setIsMarketDataActive(boolean isMarketDataActive) throws JBookTraderException {
        this.isMarketDataActive = isMarketDataActive;
        if (!isMarketDataActive) {
            disconnectionTime = dispatcher.getNTPClock().getTime();
        }

        if (isMarketDataActive) {
            long reconnectionTime = dispatcher.getNTPClock().getTime();
            long elapsedDisconnectionTimeSeconds = (reconnectionTime - disconnectionTime) / 1000;
            if (disconnectionTime != 0 && elapsedDisconnectionTimeSeconds > maxDisconnectionTimeSeconds) {
                dispatcher.setMode(Mode.ForceClose);
            }
            disconnectionTime = 0;
        }

    }

    public boolean getIsMarketDataActive() {
        return isMarketDataActive;
    }


    private synchronized void placeOrder(Contract contract, Order order, Strategy strategy) {
        try {
            if (isOrderExecutionPending) {
                return;
            }

            long remainingTime = strategy.getTradingSchedule().getRemainingTime(strategy.getMarketBook().getSnapshot().getTime());
            long remainingMinutes = remainingTime / (1000 * 60);
            if (strategy.getPositionManager().getTargetPosition() != 0 && remainingMinutes < 15) {
                return;
            }


            isOrderExecutionPending = true;
            orderID++;

            Mode mode = dispatcher.getMode();
            if (mode == Mode.Trade || mode == Mode.ForwardTest || mode == Mode.ForceClose) {
                String msg = "Placing order " + orderID;
                eventReport.report(strategy.getName(), msg);
            }

            openOrders.put(orderID, new OpenOrder(orderID, order, strategy));

            double midPrice = strategy.getMarketBook().getSnapshot().getPrice();
            double bidAskSpread = strategy.getBidAskSpread();
            double expectedFillPrice = order.m_action.equalsIgnoreCase("BUY") ? (midPrice + bidAskSpread / 2) : (midPrice - bidAskSpread / 2);
            strategy.getPositionManager().setExpectedFillPrice(expectedFillPrice);

            if (mode == Mode.Trade || mode == Mode.ForceClose) {
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
        order.m_account = faSubAccount;
        placeOrder(contract, order, strategy);
    }


    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    private void checkAccountType() throws JBookTraderException {
        socket.reqAccountUpdates(true, faSubAccount);

        try {
            synchronized (trader) {
                while (accountCode == null) {
                    trader.wait();
                }
            }
        } catch (InterruptedException ie) {
            throw new JBookTraderException(ie);
        }

        socket.reqAccountUpdates(false, faSubAccount);
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
