package com.jbooktrader.platform.util.contract;

import com.ib.client.*;

/**
 * Provides convenience methods to create stock, futures, and Forex contracts
 *
 * @author Eugene Kononov
 */
public class ContractFactory {

    public static Contract makeContract(String symbol, String securityType, String exchange, String currency) {
        Contract contract = new Contract();

        contract.m_symbol = symbol;
        contract.m_secType = securityType;
        contract.m_exchange = exchange;
        contract.m_currency = currency;

        return contract;
    }

    public static Contract makeStockContract(String symbol, String exchange, String currency) {
        return makeContract(symbol, "STK", exchange, currency);
    }

    public static Contract makeStockContract(String symbol, String exchange) {
        return makeStockContract(symbol, exchange, null);
    }

    public static Contract makeFutureContract(String symbol, String exchange, String currency) {
        return makeContract(symbol, "FUT", exchange, currency);
    }

    public static Contract makeFutureContract(String symbol, String exchange) {
        return makeFutureContract(symbol, exchange, null);
    }

    public static Contract makeCashContract(String symbol, String currency) {
        return makeContract(symbol, "CASH", "IDEALPRO", currency);
    }

    public static Contract makeIndexContract(String symbol, String exchange) {
        return makeContract(symbol, "IND", exchange, null);
    }

}
