package com.jbooktrader.platform.util.ntp;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.report.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.util.format.*;
import org.apache.commons.net.ntp.*;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * NTPClock uses Apache's NTPUPDClient which implements the Network Time Protocol (RFC-1305) specification:
 * http://www.faqs.org/ftp/rfc/rfc1305.pdf
 * <p/>
 * NTPClock does not synchronize the computer's clock, but merely uses NTP time to timestamp incoming market data.
 *
 * @author Eugene Kononov
 */
public class NTPClock {
    private static final String ERROR_MSG = "Problem while requesting time from server ";
    private final NTPUDPClient ntpClient;
    private final InetAddress host;
    private final AtomicLong offset;
    private final EventReport eventReport;

    private class NTPClockPoller implements Runnable {
        public void run() {
            try {
                TimeInfo timeInfo = ntpClient.getTime(host);
                timeInfo.computeDetails();
                long offsetNow = timeInfo.getOffset();
                if (offsetNow != 0) {
                    offset.set(offsetNow);
                }
            } catch (Exception e) {
                eventReport.report(JBookTrader.APP_NAME, ERROR_MSG + host.getHostName() + ": " + e.getMessage());
            }
        }
    }

    public NTPClock() throws JBookTraderException {
        eventReport = Dispatcher.getInstance().getEventReport();
        ntpClient = new NTPUDPClient();
        ntpClient.setDefaultTimeout(5000);
        offset = new AtomicLong();
        String hostName = PreferencesHolder.getInstance().get(JBTPreferences.NTPTimeServer);

        TimeInfo timeInfo;
        try {
            ntpClient.open();
            host = InetAddress.getByName(hostName);
            timeInfo = ntpClient.getTime(host);
        } catch (IOException e) {
            throw new JBookTraderException(e);
        }

        timeInfo.computeDetails();
        long offsetNow = timeInfo.getOffset();
        if (offsetNow != 0) {
            offset.set(offsetNow);
        }

        DecimalFormat df4 = NumberFormatterFactory.getNumberFormatter(4);
        NtpV3Packet ntpMsg = timeInfo.getMessage();
        StringBuilder msg = new StringBuilder();
        msg.append("Time host: ").append(host.getHostName());
        msg.append(", name: ").append(ntpMsg.getReferenceIdString());
        msg.append(", stratum: ").append(ntpMsg.getStratum());
        msg.append(", type: ").append(ntpMsg.getType());
        String precision = df4.format(Math.pow(2, ntpMsg.getPrecision()) * Math.pow(10, 6));
        msg.append(", precision: ").append(precision).append(" microseconds");
        eventReport.report(JBookTrader.APP_NAME, msg.toString());
        eventReport.report(JBookTrader.APP_NAME, "NTP clock updated. Offset: " + offset + " ms");

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(new NTPClockPoller(), 0, 15, TimeUnit.MINUTES);
    }

    public long getTime() {
        return System.currentTimeMillis() + offset.get();
    }
}
