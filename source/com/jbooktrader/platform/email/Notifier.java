package com.jbooktrader.platform.email;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.preferences.*;
import org.apache.commons.mail.*;

import static com.jbooktrader.platform.preferences.JBTPreferences.*;

/**
 */
public class Notifier {
    private static Notifier instance;
    private final String[] recipients;
    private final String smtpHost, user, password, subject;
    private final int smtpPort;

    private Notifier() {
        PreferencesHolder prefs = PreferencesHolder.getInstance();

        smtpHost = prefs.get(SmtpHost);
        smtpPort = Integer.parseInt(prefs.get(SmtpPort));
        user = prefs.get(SmtpUser);
        password = prefs.get(SmtpPassword);
        subject = prefs.get(Subject);


        String recipientsPref = prefs.get(Recipients);
        recipients = recipientsPref.split(",");

    }

    public static synchronized Notifier getInstance() {
        if (instance == null) {
            instance = new Notifier();
        }
        return instance;
    }

    public void send(String msg) {
        try {
            send(msg, false);
        } catch (EmailException ee) {
            Dispatcher.getInstance().getEventReport().report(ee);
        }
    }

    synchronized public void send(String msg, boolean debug) throws EmailException {
        Mode mode = Dispatcher.getInstance().getMode();
        if (debug || (mode == Mode.Trade) || (mode == Mode.ForwardTest) || (mode == Mode.ForceClose)) {
            Email email = new SimpleEmail();
            email.setDebug(debug);
            email.setHostName(smtpHost);
            email.setSmtpPort(smtpPort);
            email.setAuthenticator(new DefaultAuthenticator(user, password));
            email.setSSLOnConnect(true);
            email.setFrom(user);
            email.addTo(recipients);
            email.setSubject(subject);
            email.setMsg(msg);

            email.send();
        }
    }
}
