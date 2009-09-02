package com.jbooktrader.platform.report;

import com.jbooktrader.platform.model.*;
import static com.jbooktrader.platform.model.Dispatcher.*;
import com.jbooktrader.platform.util.*;

import java.io.*;
import java.text.*;
import java.util.*;


public final class EventReport extends Report {
    private static boolean isDisabled;
    protected final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
    protected final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS z");


    public EventReport(String fileName) throws JBookTraderException {
        super(fileName);
    }

    public static void disable() {
        isDisabled = true;
    }

    public static void enable() {
        isDisabled = false;
    }

    private void report(StringBuilder message) {
        Date date = getDate();
        StringBuilder s = new StringBuilder();
        s.append(ROW_START);
        s.append(FIELD_START).append(dateFormat.format(date)).append(FIELD_END);
        s.append(FIELD_START).append(timeFormat.format(date)).append(FIELD_END);
        s.append(FIELD_START).append(message).append(FIELD_END);
        s.append(ROW_END);
        write(s);
    }


    public void report(String message) {
        if (!isDisabled) {
            report(new StringBuilder(message));
        }
    }


    public void report(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.close();
        boolean saved = isDisabled;
        isDisabled = false;//always report exceptions
        report(sw.toString());
        isDisabled = saved;
    }

    private Date getDate() {
        Mode mode = Dispatcher.getMode();
        if (mode == Mode.ForwardTest || mode == Mode.Trade) {
            return new Date(NTPClock.getInstance().getTime());
        } else {
            return new Date();
        }
    }


}
