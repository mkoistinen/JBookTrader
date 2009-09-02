package com.jbooktrader.platform.util;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.preferences.*;
import org.apache.commons.net.ntp.*;

import java.net.*;
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
    private AtomicLong offset;
    private static NTPClock instance;

    synchronized public static NTPClock getInstance() {
        if (instance == null) {
            instance = new NTPClock();
        }
        return instance;
    }

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

            String msg = "NTP clock: [name:" + message.getReferenceIdString();
            msg += ", stratum:" + message.getStratum();
            msg += ", mode:" + message.getModeName();
            msg += ", precision:" + message.getPrecision();
            msg += ", type:" + message.getType();
            msg += ", dispersion (ms):" + message.getRootDispersionInMillisDouble();
            msg += "]";

            Dispatcher.getEventReport().report(msg);


        } catch (Exception e) {
            Dispatcher.getEventReport().report(ERROR_MSG + host.getHostName() + ": " + e.getMessage());
        }
    }


    private void getOffset() {
        try {
            TimeInfo timeInfo = ntpClient.getTime(host);
            timeInfo.computeDetails();
            offset.set(timeInfo.getOffset());
        } catch (Exception e) {
            Dispatcher.getEventReport().report(ERROR_MSG + host.getHostName() + ": " + e.getMessage());
        }
    }

    // private constructor for non-instantiability
    private NTPClock() {
        ntpClient = new NTPUDPClient();
        ntpClient.setDefaultTimeout(5000);
        offset = new AtomicLong();
        try {
            String hostName = PreferencesHolder.getInstance().get(JBTPreferences.NTPTimeServer);
            host = InetAddress.getByName(hostName);
        } catch (UnknownHostException uhe) {
            Dispatcher.getEventReport().report(uhe);
            throw new RuntimeException(uhe);
        }

        getOffset();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(new NTPClockPoller(), 0, 1, TimeUnit.MINUTES);
    }

    public long getTime() {
        return System.currentTimeMillis() + offset.get();
    }
}
