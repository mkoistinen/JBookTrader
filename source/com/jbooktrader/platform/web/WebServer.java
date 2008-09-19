package com.jbooktrader.platform.web;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.Server;
import com.jbooktrader.platform.model.JBookTraderException;

public class WebServer {

    public static void start() throws JBookTraderException {

        try {
            Server server = new Server(1234);
            Context context = new Context(server,"/",Context.SESSIONS);
            context.addServlet(new ServletHolder(new JBTServlet()), "/*");
            server.start();
        }
        catch(Exception e) {
            throw new JBookTraderException(e);
        }
    }
    
    public static class JBTServlet extends HttpServlet {

        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("<h1>Hello SimpleServlet</h1>");
        }
    }
}
