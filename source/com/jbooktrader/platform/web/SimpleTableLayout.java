package com.jbooktrader.platform.web;

import com.ib.client.*;
import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;

import java.text.*;
import java.util.*;

public class SimpleTableLayout {
    private final static DecimalFormat df0 = NumberFormatterFactory.getNumberFormatter(0);
    private final static DecimalFormat df6 = NumberFormatterFactory.getNumberFormatter(6);

    private final StringBuilder response;
    private final List<Strategy> strategies;

    public SimpleTableLayout(StringBuilder response, List<Strategy> strategies) {
        this.response = response;
        this.strategies = strategies;
    }

    public void render() {
        response.append("<table>");
        response.append("<tr><th>Strategy</th><th>Symbol</th><th>Price</th><th>Position</th><th>Trades</th><th class=\"last\">Net Profit</th></tr>");

        int strategyRowCount = 0;

        for (Strategy strategy : strategies) {
            String strategyName = strategy.getName();

            Contract contract = strategy.getContract();
            String symbol = contract.m_symbol;
            if (contract.m_currency != null) {
                symbol += "." + contract.m_currency;
            }

            MarketSnapshot marketSnapshot = strategy.getMarketBook().getSnapshot();
            String price = (marketSnapshot != null) ? df6.format(marketSnapshot.getPrice()) : "<span class=\"na\">n/a</span>";

            if (strategyRowCount % 2 == 0) {
                response.append("<tr class=\"strategy\">\n");
            } else {
                response.append("<tr class=\"strategy oddRow\">\n");
            }

            response.append("<td><a href=\"/reports/" + strategyName + ".htm\" target=\"_new\">" + strategy.getName() + "</a></td>");
            response.append("<td>" + symbol + "</td>");
            response.append("<td>" + price + "</td>");
            response.append("<td>" + strategy.getPositionManager().getPosition() + "</td>");
            PerformanceManager pm = strategy.getPerformanceManager();
            response.append("<td>" + pm.getTrades() + "</td>");
            response.append("<td class=\"last\">" + df0.format(pm.getNetProfit()) + "</td>");
            response.append("</tr>\n");

            strategyRowCount++;
        }

        response.append("</table>");
    }

}
