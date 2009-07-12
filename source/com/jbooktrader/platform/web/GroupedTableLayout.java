package com.jbooktrader.platform.web;

import com.ib.client.*;
import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.position.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;

import java.text.*;
import java.util.*;

public class GroupedTableLayout extends TableLayout {

    public GroupedTableLayout(StringBuilder response, List<Strategy> strategyList) {
        super(response, strategyList);
    }

    public void render() {
        response.append("<table>");
        response.append("<tr><th>Strategy</th><th>Position</th><th>Trades</th><th>Max DD</th><th class=\"last\">Net Profit</th></tr>");

        DecimalFormat df0 = NumberFormatterFactory.getNumberFormatter(0);
        DecimalFormat df6 = NumberFormatterFactory.getNumberFormatter(6);

        // First, make a list of the securities in use.
        HashMap<String, String> symbols = new HashMap<String, String>();

        for (Strategy strategy : Dispatcher.getTrader().getAssistant().getAllStrategies()) {
            Contract contract = strategy.getContract();
            String symbol = contract.m_symbol;
            if (contract.m_currency != null) {
                symbol += "." + contract.m_currency;
            }
            
            MarketSnapshot marketSnapshot = strategy.getMarketBook().getSnapshot();
            String price = (marketSnapshot != null) ? df6.format(marketSnapshot.getPrice()) : "<span class=\"na\">n/a</span>";

            if (!symbols.containsKey(symbol)) {
                symbols.put(symbol, price);
            }
        }

        // Sort the securities alphabetically...
        List<String> symbolKeys = new ArrayList<String>(symbols.keySet());
        Collections.sort(symbolKeys);

        int totalTrades = 0;
        double totalNetProfit = 0;

        for (String symbol : symbolKeys) {
            StringBuilder symbolBlock = new StringBuilder();
            int symbolPosition = 0;
            double symbolNetProfit = 0.0;
            int strategyRowCount = 0; // Reset the odd/even counter on each symbol

            for (Strategy strategy : strategies) {
                String strategySymbol = strategy.getContract().m_symbol;
                if (strategy.getContract().m_secType.equals("CASH")) {
                    strategySymbol += "." + strategy.getContract().m_currency;
                }

                if (strategySymbol.equals(symbol)) {
                    PositionManager positionManager = strategy.getPositionManager();
                    PerformanceManager performanceManager = strategy.getPerformanceManager();
                    totalNetProfit += performanceManager.getNetProfit();
                    totalTrades += performanceManager.getTrades();
                    symbolPosition += positionManager.getPosition();
                    symbolNetProfit += performanceManager.getNetProfit();

                    if (strategyRowCount % 2 == 0)
                    	symbolBlock.append("<tr class=\"strategy\">\n");
                    else
                    	symbolBlock.append("<tr class=\"strategy oddRow\">\n");

                    List<Object> columns = new ArrayList<Object>();
                    columns.add("<a href=\"/reports/" + strategy.getName() + ".htm\" target=\"_new\">" + strategy.getName() + "</a>");
                    columns.add(positionManager.getPosition());
                    columns.add(performanceManager.getTrades());
                    columns.add(df0.format(performanceManager.getMaxDrawdown()));

                    for (Object column : columns) {
                        symbolBlock.append("<td>").append(column).append("</td>");
                    }
                    
                    symbolBlock.append("<td class=\"last\">").append(df0.format(performanceManager.getNetProfit())).append("</td>");
                    symbolBlock.append("</tr>\n");
                    
                    strategyRowCount++;
                }
            }

            response.append("<tr class=\"symbol\">");
            response.append("<td>").append(symbol).append(" (").append(symbols.get(symbol)).append(")</td>");
            response.append("<td>").append(symbolPosition).append("</td>");
            response.append("<td colspan=\"2\">&nbsp;</td>");
            response.append("<td class=\"last\">").append(df0.format(symbolNetProfit)).append("</td></tr>\n");
            response.append(symbolBlock);
        }


        response.append("<tr class=\"summary\">");
        response.append("<td colspan=\"2\">All Strategies</td>");
        response.append("<td colspan=\"1\">").append(totalTrades).append("</td>");
        response.append("<td class=\"last\" colspan=\"2\">").append(df0.format(totalNetProfit)).append("</td>");
        response.append("</tr>\n");
        response.append("</table>");
    }

}
