package com.jbooktrader.platform.web;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.position.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;
import com.sun.net.httpserver.*;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

public class WebHandler implements HttpHandler {

    private static final String WEBROOT = JBookTrader.getAppPath() + "/resources/web";

    public void handle(HttpExchange httpExchange) throws IOException {
        URI uri = httpExchange.getRequestURI();
        String resource = uri.getPath();

        FileHandler fileHandler = new FileHandler();
        String fileName = fileHandler.getFileName(uri);
        ContentType fileType = ContentType.getContentType(fileName);

        boolean isIPhone = httpExchange.getRequestHeaders().getFirst("User-Agent").contains("iPhone");

        StringBuilder response = new StringBuilder();

        // We support a VIRTUAL directory '/reports/' which we manually map onto the reports folder in the class path
        // This must explicitly be the beginning of the requested resource.  Otherwise, we fold any requests over to
        // the WEBROOT
        String absoluteResource;
        if (resource.startsWith("/reports/")) {
            absoluteResource = JBookTrader.getAppPath() + resource;
        } else {
            absoluteResource = WEBROOT + resource;
        }

        // First, redirect for default page
        if (resource.equals("") || resource.equals("/")) {
            httpExchange.getResponseHeaders().add("Location", "/index.html");
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_MOVED_PERM, response.length());
        }

        // The index.html page...
        // This is VIRTUAL, it is not on the filesystem.
        else if (resource.equals("/index.html")) {

            // We'll respond to any unknown request with the main page
            response.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\n");
            response.append("<html>\n");
            response.append("<head>\n");
            response.append("<title>JBookTrader Web Console</title>\n");

            if (isIPhone) {
                response.append("<link rel=\"apple-touch-icon\" href=\"apple-touch-icon.png\" />\n");
                response.append("<meta name=\"viewport\" content=\"width=device-width; initial-scale=1.0; maximum-scale=1.0; user-scalable=0;\" />\n");
                response.append("<link media=\"screen\" rel=\"stylesheet\" type=\"text/css\" href=\"iphone.css\" />\n");
                response.append("<script type=\"application/x-javascript\" src=\"iphone.js\"></script> \n");
            } else {
                response.append("<link media=\"screen\" rel=\"stylesheet\" type=\"text/css\" href=\"stylesheet.css\" />\n");
            }

            response.append("</head>\n");
            response.append("<body>\n");
            response.append("<h1>\n");
            response.append(JBookTrader.APP_NAME).append(": ").append(Dispatcher.getMode().getName());
            response.append("</h1>\n");

            response.append("<table>");
            response.append("<tr><th><p>Strategy</p></th><th><p>Position</p></th><th><p>Trades</p></th><th><p>Max DD</p></th><th><p>Net Profit</p></th></tr>");
            DecimalFormat df = NumberFormatterFactory.getNumberFormatter(0);

            double totalNetProfit = 0.0;
            int totalTrades = 0;

            // First, make a list of the securities in use.
            HashMap<String, Double> symbols = new HashMap<String, Double>();

            for (Strategy strategy : Dispatcher.getTrader().getAssistant().getAllStrategies()) {
                String symbol = strategy.getContract().m_symbol;

                if (strategy.getContract().m_secType.equals("CASH")) {
                    symbol += "." + strategy.getContract().m_currency;
                }
                double quote = Double.NaN;
                try {
                    quote = strategy.getMarketBook().getSnapshot().getPrice();
                }
                catch (Exception e) { /* we don't care */ }
                if (!symbols.containsKey(symbol)) symbols.put(symbol, quote);
            }

            // Sort the securities alphabetically...
            List<String> symbolKeys = new ArrayList<String>(symbols.keySet());
            Collections.sort(symbolKeys);

            // Sort the strategies too...
            Collection<Strategy> strategies = Dispatcher.getTrader().getAssistant().getAllStrategies();
            List<Strategy> strategyList = new ArrayList<Strategy>(strategies);
            Collections.sort(strategyList);

            for (String symbol : symbolKeys) {
                StringBuilder symbolBlock = new StringBuilder();
                int symbolPosition = 0;
                double symbolNetProfit = 0.0;

                for (Strategy strategy : strategyList) {
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

                        symbolBlock.append("<tr class=\"strategy\">\n");
                        symbolBlock.append("<td><a href=\"/reports/").append(strategy.getName()).append(".htm\" target=\"_new\">").append(strategy.getName()).append("</a></td>");
                        symbolBlock.append("<td>").append(positionManager.getPosition()).append("</td>");
                        symbolBlock.append("<td>").append(performanceManager.getTrades()).append("</td>");
                        symbolBlock.append("<td>").append(df.format(performanceManager.getMaxDrawdown())).append("</td>");
                        symbolBlock.append("<td>").append(df.format(performanceManager.getNetProfit())).append("</td>");
                        symbolBlock.append("</tr>\n");
                    }
                }

                response.append("<tr class=\"symbol\">");
                response.append("<td>").append(symbol).append(" (");
                if (symbols.get(symbol).isNaN()) {
                    response.append("n/a");
                } else {
                    response.append(symbols.get(symbol));
                }
                response.append(")</td>");
                response.append("<td>").append(symbolPosition).append("</td>");
                response.append("<td colspan=\"2\">&nbsp;</td>");
                response.append("<td>").append(df.format(symbolNetProfit)).append("</td></tr>\n");
                response.append("<tr class=\"hidden\"></tr>"); // This is to keep alternating rows working nicely.
                response.append(symbolBlock);

            }

            response.append("<tr class=\"summary\">");
            response.append("<td colspan=\"2\">All Strategies</td>");
            response.append("<td colspan=\"1\">").append(totalTrades).append("</td>");
            response.append("<td class=\"summary\">&nbsp;</td>");
            response.append("<td>").append(df.format(totalNetProfit)).append("</td></tr>\n");

            response.append("</table>\n");
            response.append("<p class=\"version\">JBookTrader version ").append(JBookTrader.VERSION).append("</p>\n");
            response.append("<p class=\"eventReport\"><a href=\"/reports/EventReport.htm\" target=\"_new\">Event Report</a></p>\n");
            response.append("</body>\n");
            response.append("</html>\n");
            httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.length());
        }

        // ALL dynamic pages must be in the if/then/else sequence above this point
        // Static resources from here down
        else {
            if (!fileHandler.handleFile(httpExchange, absoluteResource, fileType)) {
                response = new StringBuilder("<h1>404 Not Found</h1>No context found for request");
                httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, response.length());
            }
        }

        OutputStream os = httpExchange.getResponseBody();
        os.write(response.toString().getBytes());
        os.close();
    }
}
