package com.jbooktrader.platform.web;

import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;
import com.sun.net.httpserver.*;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

public class WebHandler implements HttpHandler {
    private static final DecimalFormat df0 = NumberFormatterFactory.getNumberFormatter(0);
    private static final DecimalFormat df6 = NumberFormatterFactory.getNumberFormatter(6);
    private static final String RESOURCE_DIR = JBookTrader.getAppPath() + "/resources";
    private static final String REPORT_DIR = JBookTrader.getAppPath() + "/reports";

    private void addRow(StringBuilder response, List<Object> cells, int rowCount) {
        response.append((rowCount % 2 == 0) ? "<tr>" : "<tr class=oddRow>");
        for (Object cell : cells) {
            response.append("<td>").append(cell).append("</td>");
        }
        response.append("</tr>");
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        byte[] out;
        String resource = httpExchange.getRequestURI().getPath();

        if (resource.equals("") || resource.equals("/")) {
            Dispatcher dispatcher = Dispatcher.getInstance();
            List<Strategy> strategies = new ArrayList<Strategy>(dispatcher.getTrader().getAssistant().getAllStrategies());
            Collections.sort(strategies);

            StringBuilder response = new StringBuilder();
            response.append("<html><head><title>");
            response.append(JBookTrader.APP_NAME + ", version " + JBookTrader.VERSION + "</title>");
            response.append("<link rel=stylesheet type=text/css href=JBookTrader.css />");
            response.append("<link rel=\"shortcut icon\" type=image/x-icon href=JBookTrader.ico />");
            response.append("</head><body><h1>" + JBookTrader.APP_NAME + " - ");
            response.append("<a href=EventReport.htm target=_new>" + dispatcher.getMode().getName() + "</a></h1><table>");
            response.append("<tr><th>Strategy</th><th>Symbol</th><th>Price</th><th>Position</th><th>Trades</th><th>Net Profit</th></tr>");

            int strategyRowCount = 0;
            for (Strategy strategy : strategies) {
                MarketSnapshot marketSnapshot = strategy.getMarketBook().getSnapshot();
                PerformanceManager pm = strategy.getPerformanceManager();

                List<Object> cells = new ArrayList<Object>();
                String path = REPORT_DIR + "/" + strategy.getName() + ".htm";
                if (new File(path).exists()) {
                    cells.add("<a href=" + strategy.getName() + ".htm target=_new>" + strategy.getName() + "</a>");
                } else {
                    cells.add(strategy.getName());
                }
                cells.add(strategy.getSymbol());
                cells.add((marketSnapshot != null) ? df6.format(marketSnapshot.getPrice()) : "n/a");
                cells.add(strategy.getPositionManager().getCurrentPosition());
                cells.add(pm.getTrades());
                cells.add(df0.format(pm.getNetProfit()));
                addRow(response, cells, strategyRowCount++);
            }

            response.append("</table></body></html>");
            out = response.toString().getBytes();
        } else {
            String path = (resource.endsWith("htm") ? REPORT_DIR : RESOURCE_DIR) + resource;
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(path));
            out = new byte[(int) new File(path).length()];
            bis.read(out);
            bis.close();
        }

        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, out.length);
        OutputStream responseBody = httpExchange.getResponseBody();
        responseBody.write(out);
        responseBody.close();
    }

}
