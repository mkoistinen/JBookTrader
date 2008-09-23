package com.jbooktrader.platform.web;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.position.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.strategy.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class JBTServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);

        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<center><h3>");
        sb.append(JBookTrader.APP_NAME).append(", version ").append(JBookTrader.VERSION);
        sb.append(", ").append(Dispatcher.getMode()).append(" mode");
        sb.append("</h3></center>");

        sb.append("<table border=1 width=100%>");
        sb.append("<tr><th>Strategy<th>Position<th>Trades<th>MaxDD<th>Net Profit</tr>");

        for (Strategy strategy : Dispatcher.getTrader().getAssistant().getAllStrategies()) {
            PositionManager positionManager = strategy.getPositionManager();
            PerformanceManager performanceManager = strategy.getPerformanceManager();
            sb.append("<tr>");
            sb.append("<td>").append(strategy.getName()).append("</td>");
            sb.append("<td>").append(positionManager.getPosition()).append("</td>");
            sb.append("<td>").append(performanceManager.getTrades()).append("</td>");
            sb.append("<td>").append(performanceManager.getMaxDrawdown()).append("</td>");
            sb.append("<td>").append(performanceManager.getNetProfit()).append("</td>");
            sb.append("</tr>");
        }

        sb.append("</table>");
        sb.append("</html>");
        response.getWriter().println(sb);
    }
}
