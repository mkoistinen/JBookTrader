package com.jbooktrader.platform.util;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.report.*;
import com.jbooktrader.platform.startup.*;
import org.apache.commons.net.ntp.*;

import java.net.*;
import java.text.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * NTPClock uses Apache's NTPUPDClient which implements the Network Time Protocol (RFC-1305) specification:
 * http://www.faqs.org/ftp/rfc/rfc1305.pdf
 * <p/>
 * NTPClock does not synchronize the computer's clock, but merely uses NTP time to timestamp incoming market data.
 */
public class NTPClock {
    private static final String ERROR_MSG = "Problem while requesting time from server ";
    private final NTPUDPClient ntpClient;
    private final InetAddress host;
    private final AtomicLong offset;
    private final EventReport eventReport;

    private class NTPClockPoller implements Runnable {
        public void run() {
            getOffset();
        }
    }

    public void reportAttributes() {
        try {
            TimeInfo timeInfo = ntpClient.getTime(host);
            timeInfo.computeDetails();
            NtpV3Packet message = timeInfo.getMessage();

            String msg = "Time host: " + host.getHostName();
            msg += ", name: " + message.getReferenceIdString();
            msg += ", stratum: " + message.getStratum();
            msg += ", type: " + message.getType();
            DecimalFormat df4 = NumberFormatterFactory.getNumberFormatter(4);
            msg += ", precision (µs): " + df4.format(Math.pow(2, message.getPrecision()) * Math.pow(10, 6));
            msg += ", dispersion (ms): " + df4.format(message.getRootDispersionInMillisDouble());

            eventReport.report(JBookTrader.APP_NAME, msg);
        } catch (Exception e) {
            eventReport.report(JBookTrader.APP_NAME, ERROR_MSG + host.getHostName() + ": " + e.getMessage());
        }
    }


    private void getOffset() {
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

    public NTPClock() throws UnknownHostException, SocketException {
        eventReport = Dispatcher.getInstance().getEventReport();
        ntpClient = new NTPUDPClient();
        ntpClient.setDefaultTimeout(10000);
        ntpClient.open();
        offset = new AtomicLong();

        String hostName = PreferencesHolder.getInstance().get(JBTPreferences.NTPTimeServer);
        host = InetAddress.getByName(hostName);

        getOffset();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(new NTPClockPoller(), 0, 5, TimeUnit.MINUTES);
    }

    public long getTime() {
        return System.currentTimeMillis() + offset.get();
    }
}
