package com.jbooktrader.platform.c2;

import static com.jbooktrader.platform.c2.C2Action.*;
import com.jbooktrader.platform.model.*;
import static com.jbooktrader.platform.preferences.JBTPreferences.*;
import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.report.*;

import java.io.*;
import java.net.*;

public class Collective2Gateway {
    private static final String COLLECTIVE2_URL = "http://www.collective2.com/cgi-perl/signal.mpl";
    private final String systemId;
    private final String password;
    private static final Report report = Dispatcher.getReporter();

    public Collective2Gateway(String systemId) {
        this.systemId = systemId;
        password = PreferencesHolder.getInstance().get(Collective2Password);
    }


    public void send(int currentPosition, int newPosition) {
        Collective2Sender c2Sender = Collective2Sender.getInstance();
        C2Action c2Action = null;
        int quantity;

        if (currentPosition * newPosition < 0) {
            // reverse
            quantity = Math.abs(newPosition);
        } else {
            quantity = Math.abs(newPosition - currentPosition);
            if (newPosition > currentPosition) {
                c2Action = (currentPosition < 0) ? BuyToClose : BuyToOpen;
            } else {
                c2Action = (currentPosition > 0) ? SellToClose : SellToOpen;
            }
        }

        try {
            URL url = createURL(c2Action, quantity);
            c2Sender.submit(url);
        } catch (Exception e) {
            report.report(e);
        }
    }

    private URL createURL(C2Action c2Action, int quantity) throws MalformedURLException, IllegalArgumentException, Collective2Exception {
        StringBuffer params;
        try {
            params = new StringBuffer(COLLECTIVE2_URL);
            if (c2Action == null) {
                params.append("?cmd=reverse");
            } else {
                params.append("?cmd=signal");
            }
            params.append("&systemid=").append(systemId);
            params.append("&pw=").append(URLEncoder.encode(password, "US-ASCII"));
            if (c2Action != null) {
                params.append("&action=").append(URLEncoder.encode(c2Action.getCode(), "US-ASCII"));
            }
            params.append("&quant=").append(Integer.toString(quantity));
            //todo: pass in instrument and symbol
            params.append("&instrument=future");
            params.append("&symbol=").append(URLEncoder.encode("@ESZ8", "US-ASCII"));
        } catch (UnsupportedEncodingException e) {
            throw new Collective2Exception(e);
        }

        return new URL(params.toString());
    }

}
