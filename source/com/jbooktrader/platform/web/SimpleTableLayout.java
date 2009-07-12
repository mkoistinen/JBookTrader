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

        DecimalFormat df0 = NumberFormatterFactory.getNumberFormatter(0);
        DecimalFormat df6 = NumberFormatterFactory.getNumberFormatter(6);
        StringBuilder block = new StringBuilder();

        for (Strategy strategy : strategyList) {
            Contract contract = strategy.getContract();
            String symbol = contract.m_symbol;
            if (contract.m_currency != null) {
                symbol += "." + contract.m_currency;
            }

            PositionManager positionManager = strategy.getPositionManager();
            PerformanceManager performanceManager = strategy.getPerformanceManager();
            MarketSnapshot marketSnapshot = strategy.getMarketBook().getSnapshot();
            String price = (marketSnapshot != null) ? df6.format(marketSnapshot.getPrice()): "";

            block.append("<tr class=\"strategy\">\n");
            block.append("<td><a href=\"/reports/").append(strategy.getName()).append(".htm\" target=\"_new\">").append(strategy.getName()).append("</a></td>");
            block.append("<td>").append(symbol).append("</td>");
            block.append("<td>").append(price).append("</td>");
            block.append("<td>").append(positionManager.getPosition()).append("</td>");
            block.append("<td>").append(performanceManager.getTrades()).append("</td>");
            block.append("<td>").append(df0.format(performanceManager.getMaxDrawdown())).append("</td>");
            block.append("<td>").append(df0.format(performanceManager.getNetProfit())).append("</td>");
            block.append("</tr>\n");

        }

        response.append("<tr class=\"hidden\"></tr>"); // This is to keep alternating rows working nicely.
        response.append(block);
        response.append("</table>");
    }


}
