package com.jbooktrader.platform.c2;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.report.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;


public class C2Sender extends Thread {
    private static final EventReport report = Dispatcher.getEventReport();
    private static boolean shutdown;
    private final Queue<URL> urls;
    private static C2Sender instance;

    private C2Sender() {
        urls = new ConcurrentLinkedQueue<URL>();
        start();
    }

    public synchronized static C2Sender getInstance() {
        if (instance == null) {
            instance = new C2Sender();
        }
        return instance;
    }

    public synchronized static void shutdown() {
        shutdown = true;
    }

    private void send(URL url) throws IOException {
        report.report("submitting signal to Collective2");
        URLConnection connection = url.openConnection();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        StringBuilder response = new StringBuilder();
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } finally {
            reader.close();
        }

        report.report("Collective2: " + response.toString());
    }


    public void run() {
        while (!shutdown) {
            synchronized (urls) {
                while (urls.isEmpty()) {
                    try {
                        urls.wait();
                    } catch (InterruptedException ie) {
                        report.report(ie);
                    }
                }
            }
            while (!urls.isEmpty()) {
                URL url = urls.peek();
                try {
                    send(url);
                    urls.poll();
                } catch (IOException e) {
                    try {
                        // the job is still in the queue, retry again in one second
                        report.report(e);
                        report.report("Failed sending trade to Collective2, will retry in 1 second.");
                        sleep(1000);
                    } catch (InterruptedException ie) {
                        // ignore
                    }
                }
            }
        }
    }

    public void submit(URL url) {
        urls.offer(url);
        synchronized (urls) {
            urls.notifyAll();
        }
    }
}

