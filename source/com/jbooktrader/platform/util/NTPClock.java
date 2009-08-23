package com.jbooktrader.platform.util;

import org.apache.commons.net.ntp.*;

import java.net.*;

/*
-------- Server Pools--------
Worldwide: 	 pool.ntp.org
Asia: 	asia.pool.ntp.org
Europe: 	europe.pool.ntp.org
North America: 	north-america.pool.ntp.org
Oceania: 	oceania.pool.ntp.org
South America: 	south-america.pool.ntp.org
*/
public class NTPClock {
    private final NTPUDPClient ntp;
    private final InetAddress host;

    public NTPClock() {
        ntp = new NTPUDPClient();
        //ntp.setDefaultTimeout(500);
        try {
            host = InetAddress.getByName("north-america.pool.ntp.org");
        } catch (UnknownHostException uhe) {
            throw new RuntimeException(uhe);
        }
    }

    public long getTime() {
        TimeInfo timeInfo = null;

        while (timeInfo == null) {
            try {
                timeInfo = ntp.getTime(host);
            } catch (Exception e) {
                // ignore and retry
            }
        }

        timeInfo.computeDetails();
        return System.currentTimeMillis() + timeInfo.getOffset();
    }
}
