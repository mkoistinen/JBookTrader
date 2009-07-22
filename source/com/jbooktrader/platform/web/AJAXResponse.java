package com.jbooktrader.platform.web;

import com.ib.client.*;
import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.position.*;
import com.jbooktrader.platform.startup.JBookTrader;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;

import java.text.*;
import java.util.*;

import java.io.*;
import java.net.*;

public class AJAXResponse extends TableLayout {

    public AJAXResponse(StringBuilder response, List<Strategy> strategies) {
        super(response, strategies);
    }

    public void render() {

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
            String price = (marketSnapshot != null) ? df6.format(marketSnapshot.getPrice()) : "n/a";

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
                String strategyName = strategy.getName();
                
                // Check if the StrategyReport exists
                boolean fileExists = true;
                try {
                	String reportFile = JBookTrader.getAppPath() + WebHandler.REPORT_DIR + strategy.getName() + ".htm";
                	File file = new File(reportFile);
                	
                	if (!file.exists()) fileExists = false;
                }
                catch (Exception e) {
                	fileExists = false;
                }
                
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
                    
                    symbolBlock.append("[STRATEGY]").append(",");
                    symbolBlock.append(strategyName).append(",");
                    symbolBlock.append(symbol).append(",");
                    symbolBlock.append(symbols.get(symbol)).append(",");
                    symbolBlock.append(positionManager.getPosition()).append(",");
                    symbolBlock.append(performanceManager.getTrades()).append(",");
                    symbolBlock.append(df0.format(performanceManager.getMaxDrawdown())).append(",");
                    symbolBlock.append(df0.format(performanceManager.getNetProfit())).append(",");
                    symbolBlock.append(fileExists ? "link" : "nolink").append("\n");
                    
                    strategyRowCount++;
                }
            }

            response.append("[SYMBOL]").append(",");
            response.append(symbol).append(",");
            response.append(symbols.get(symbol)).append(",");
            response.append(symbolPosition).append(",");
            response.append(df0.format(symbolNetProfit)).append("\n");
            response.append(symbolBlock);
            
            response.append("[SUMMARY]").append(",");
            response.append(totalTrades).append(",");
            response.append(totalNetProfit).append("\n");
        }
    }

}
