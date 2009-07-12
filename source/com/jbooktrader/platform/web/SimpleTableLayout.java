package com.jbooktrader.platform.web;

import com.ib.client.*;
import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.position.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;

import java.text.*;
import java.util.*;

public class SimpleTableLayout extends TableLayout {

    public SimpleTableLayout(StringBuilder response, List<Strategy> strategyList) {
        super(response, strategyList);
    }

    public void render() {
        response.append("<table>");
        response.append("<tr><th><p>Strategy</p></th><th><p>Symbol</p></th><th><p>Price</p></th><th><p>Position</p></th><th><p>Trades</p></th><th><p>Max DD</p></th><th><p>Net Profit</p></th></tr>");
        response.append("<tr class=\"hidden\"></tr>"); // This is to keep alternating rows working nicely.

        DecimalFormat df0 = NumberFormatterFactory.getNumberFormatter(0);
        DecimalFormat df6 = NumberFormatterFactory.getNumberFormatter(6);

        for (Strategy strategy : strategies) {
            Contract contract = strategy.getContract();
            String symbol = contract.m_symbol;
            if (contract.m_currency != null) {
                symbol += "." + contract.m_currency;
            }

            PositionManager positionManager = strategy.getPositionManager();
            PerformanceManager performanceManager = strategy.getPerformanceManager();
            MarketSnapshot marketSnapshot = strategy.getMarketBook().getSnapshot();
            String price = (marketSnapshot != null) ? df6.format(marketSnapshot.getPrice()) : "";

            response.append("<tr class=\"strategy\">\n");

            List<Object> columns = new ArrayList<Object>();
            columns.add("<a href=\"/reports/" + strategy.getName() + ".htm\" target=\"_new\">" + strategy.getName() + "</a>");
            columns.add(symbol);
            columns.add(price);
            columns.add(positionManager.getPosition());
            columns.add(performanceManager.getTrades());
            columns.add(df0.format(performanceManager.getMaxDrawdown()));
            columns.add(df0.format(performanceManager.getNetProfit()));

            for (Object column : columns) {
                response.append("<td>").append(column).append("</td>");
            }

            response.append("</tr>\n");

        }

        response.append("</table>");
    }


}
