package com.jbooktrader.platform.util;

import com.jbooktrader.platform.model.*;
import static com.jbooktrader.platform.preferences.JBTPreferences.*;
import com.jbooktrader.platform.preferences.*;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;

/**
 * Sends SSL Mail
 */
public class SecureMailSender {

    private static SecureMailSender instance;
    private final Properties props;
    private final String user, password, subject, recipient;
    private final boolean isEnabled;

    // inner class
    private class Mailer extends Thread {
        private final String content;

        Mailer(String content) {
            this.content = content;
        }

        public void run() {
            try {
                Session mailSession = Session.getDefaultInstance(props);
                //mailSession.setDebug(true); // sends debugging info to System.out

                MimeMessage message = new MimeMessage(mailSession);
                message.setSubject(subject);
                message.setContent(content, "text/plain");
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));

                Transport transport = mailSession.getTransport();
                transport.connect(user, password);
                transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
                transport.close();

                Dispatcher.getReporter().report("Email notification sent");
            } catch (Throwable t) {
                Dispatcher.getReporter().report("Email notification failed");
                Dispatcher.getReporter().report(t);
            }

        }
    }

    public static synchronized SecureMailSender getInstance() {
        if (instance == null) {
            instance = new SecureMailSender();
        }
        return instance;
    }

    // private constructor for noninstantiability
    private SecureMailSender() {
        PreferencesHolder prefs = PreferencesHolder.getInstance();
        isEnabled = prefs.get(EmailMonitoring).equalsIgnoreCase("enabled");

        props = new Properties();
        props.put("mail.transport.protocol", "smtps");
        props.put("mail.smtps.host", "smtp.gmail.com");
        props.put("mail.smtps.auth", "true");

        user = prefs.get(From);
        recipient = prefs.get(To);
        password = prefs.get(EmailPassword);
        subject = prefs.get(EmailSubject);
    }

    public void send(String content) {
        if (isEnabled) {
            new Mailer(content).start();
        }
    }

}
