package com.jbooktrader.platform.util;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.preferences.*;
import org.apache.commons.net.ntp.*;

import java.net.*;
import java.util.concurrent.*;

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
    private long offset;

    private class NTPClockPoller implements Runnable {
        public void run() {
            getOffset();
        }
    }

    private void getAttributes() {
        try {
            TimeInfo timeInfo = ntpClient.getTime(host);
            timeInfo.computeDetails();
            NtpV3Packet message = timeInfo.getMessage();

            String msg = "NTP clock: [name:" + message.getReferenceIdString();
            msg += ", stratum:" + message.getStratum();
            msg += ", mode:" + message.getModeName();
            msg += ", precision:" + message.getPrecision();
            msg += ", type:" + message.getType();
            msg += ", dispersion (ms):" + message.getRootDispersionInMillisDouble();
            msg += "]";

            Dispatcher.getReporter().report(msg);


        } catch (Exception e) {
            Dispatcher.getReporter().report(ERROR_MSG + host.getHostName() + ": " + e.getMessage());
        }
    }


    private void getOffset() {
        try {
            TimeInfo timeInfo = ntpClient.getTime(host);
            timeInfo.computeDetails();
            offset = timeInfo.getOffset();
        } catch (Exception e) {
            Dispatcher.getReporter().report(ERROR_MSG + host.getHostName() + ": " + e.getMessage());
        }
    }

    public NTPClock() {
        ntpClient = new NTPUDPClient();
        ntpClient.setDefaultTimeout(5000);
        try {
            String hostName = PreferencesHolder.getInstance().get(JBTPreferences.NTPTimeServer);
            host = InetAddress.getByName(hostName);
        } catch (UnknownHostException uhe) {
            Dispatcher.getReporter().report(uhe);
            throw new RuntimeException(uhe);
        }

        getAttributes();
        getOffset();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(new NTPClockPoller(), 0, 1, TimeUnit.MINUTES);
    }

    public long getTime() {
        return System.currentTimeMillis() + offset;
    }
}
