package com.jbooktrader.platform.util;

import com.ib.client.*;

/**
 * Provides convenience methods to create stock, futures, and Forex contracts
 */
public class ContractFactory {

    public static Contract makeContract(String symbol, String securityType, String exchange, String expiry, String currency) {
        Contract contract = new Contract();

        contract.m_symbol = symbol;
        contract.m_secType = securityType;
        contract.m_exchange = exchange;
        contract.m_expiry = expiry;
        contract.m_currency = currency;

        return contract;
    }

    public static Contract makeStockContract(String symbol, String exchange, String currency) {
        return makeContract(symbol, "STK", exchange, null, currency);
    }

    public static Contract makeStockContract(String symbol, String exchange) {
        return makeStockContract(symbol, exchange, null);
    }

    public static Contract makeFutureContract(String symbol, String exchange, String expiry, String currency) {
        return makeContract(symbol, "FUT", exchange, expiry, currency);
    }

    public static Contract makeFutureContract(String symbol, String exchange, String expiry) {
        return makeFutureContract(symbol, exchange, expiry, null);
    }

    public static Contract makeFutureContract(String symbol, String exchange) {
        return makeFutureContract(symbol, exchange, MostLiquidContract.getMostLiquid());
    }

    public static Contract makeCashContract(String symbol, String currency) {
        return makeContract(symbol, "CASH", "IDEALPRO", null, currency);
    }
}
