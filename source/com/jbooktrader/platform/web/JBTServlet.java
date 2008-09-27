package com.jbooktrader.platform.web;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.position.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.text.*;

public class JBTServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
        sb.append("<html>");
        sb.append("<head>");
        sb.append("<title>JBookTrader Web Console</title>");
        sb.append("<style type=\"text/css\">h3 { text-align: center; }</style>");
        sb.append("</head>");
        sb.append("<body>");
        sb.append("<h3>");
        sb.append(JBookTrader.APP_NAME).append(", version ").append(JBookTrader.VERSION);
        sb.append(", ").append(Dispatcher.getMode()).append(" mode");
        sb.append("</h3>");

        sb.append("<table bgcolor=\"#FFFFEE\" cellspacing=\"0\" border=\"1\" width=\"100%\">");
        sb.append("<tr bgcolor=\"#FFCC33\"><th>Strategy<th>Position<th>Trades<th>Max DD<th>Net Profit</tr>");
        DecimalFormat df = NumberFormatterFactory.getNumberFormatter(0);

        for (Strategy strategy : Dispatcher.getTrader().getAssistant().getAllStrategies()) {
            PositionManager positionManager = strategy.getPositionManager();
            PerformanceManager performanceManager = strategy.getPerformanceManager();
            sb.append("<tr>");
            sb.append("<td>").append(strategy.getName()).append("</td>");
            sb.append("<td align=\"right\">").append(positionManager.getPosition()).append("</td>");
            sb.append("<td align=\"right\">").append(performanceManager.getTrades()).append("</td>");
            sb.append("<td align=\"right\">").append(df.format(performanceManager.getMaxDrawdown())).append("</td>");
            sb.append("<td align=\"right\">").append(df.format(performanceManager.getNetProfit())).append("</td>");
            sb.append("</tr>");
        }

        sb.append("</table>");
        sb.append("</body>");
        sb.append("</html>");
        response.getWriter().println(sb);
    }
}
