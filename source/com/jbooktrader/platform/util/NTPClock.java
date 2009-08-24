package com.jbooktrader.platform.util;

import com.jbooktrader.platform.model.*;
import org.apache.commons.net.ntp.*;

import java.net.*;
import java.util.concurrent.*;

/*
-------- Server Pools --------
Worldwide: 	 pool.ntp.org
Asia: 	asia.pool.ntp.org
Europe: 	europe.pool.ntp.org
North America: 	north-america.pool.ntp.org
Oceania: 	oceania.pool.ntp.org
South America: 	south-america.pool.ntp.org
*/
public class NTPClock {
    private static final String HOST_NAME = "ntp2.usno.navy.mil";
    private final NTPUDPClient ntp;
    private final InetAddress host;
    private long offset;

    private class NTPClockPoller implements Runnable {
        public void run() {
            getOffset();
        }
    }

    private void getAttributes() {
        try {
            TimeInfo timeInfo = ntp.getTime(host);
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
            Dispatcher.getReporter().report("Problem while requesting time from server " + HOST_NAME + ": " + e.getMessage());
        }
    }


    private void getOffset() {
        try {
            TimeInfo timeInfo = ntp.getTime(host);
            timeInfo.computeDetails();
            offset = timeInfo.getOffset();
        } catch (Exception e) {
            Dispatcher.getReporter().report("Problem while requesting time from server " + HOST_NAME + ": " + e.getMessage());
        }
    }

    public NTPClock() {
        ntp = new NTPUDPClient();
        ntp.setDefaultTimeout(5000);
        try {
            host = InetAddress.getByName(HOST_NAME);
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
