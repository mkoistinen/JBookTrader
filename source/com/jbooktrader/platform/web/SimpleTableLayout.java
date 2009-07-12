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
        response.append("<tr><th>Strategy</th><th>Symbol</th><th>Price</th><th>Position</th><th>Trades</th><th>Max DD</th><th class=\"last\">Net Profit</th></tr>");

        DecimalFormat df0 = NumberFormatterFactory.getNumberFormatter(0);
        DecimalFormat df6 = NumberFormatterFactory.getNumberFormatter(6);

        double totalNetProfit = 0.0;
        int totalTrades = 0;
        
        int strategyRowCount = 0;
        
        for (Strategy strategy : strategies) {
            Contract contract = strategy.getContract();
            String symbol = contract.m_symbol;
            totalNetProfit += strategy.getPerformanceManager().getNetProfit();
            totalTrades += strategy.getPerformanceManager().getTrades();
            
            if (contract.m_currency != null) {
                symbol += "." + contract.m_currency;
            }

            PositionManager positionManager = strategy.getPositionManager();
            PerformanceManager performanceManager = strategy.getPerformanceManager();
            MarketSnapshot marketSnapshot = strategy.getMarketBook().getSnapshot();
            String price = (marketSnapshot != null) ? df6.format(marketSnapshot.getPrice()) : "<span class=\"na\">n/a</span>";

            if (strategyRowCount % 2 == 0)
            	response.append("<tr class=\"strategy\">\n");
            else
            	response.append("<tr class=\"strategy oddRow\">\n");
            	
            List<Object> columns = new ArrayList<Object>();
            columns.add("<a href=\"/reports/" + strategy.getName() + ".htm\" target=\"_new\">" + strategy.getName() + "</a>");
            columns.add(symbol);
            columns.add(price);
            columns.add(positionManager.getPosition());
            columns.add(performanceManager.getTrades());
            columns.add(df0.format(performanceManager.getMaxDrawdown()));

            for (Object column : columns) {
                response.append("<td>").append(column).append("</td>");
            }

            response.append("<td class=\"last\">").append(df0.format(performanceManager.getNetProfit())).append("</td>");
            response.append("</tr>\n");

            strategyRowCount++;
        }

        response.append("<tr class=\"summary\">");
        response.append("<td colspan=\"4\">All Strategies</td>");
        response.append("<td colspan=\"1\">").append(totalTrades).append("</td>");
        response.append("<td class=\"last\" colspan=\"2\">").append(df0.format(totalNetProfit)).append("</td>");
        response.append("</tr>\n");
        response.append("</table>");
    }


}
