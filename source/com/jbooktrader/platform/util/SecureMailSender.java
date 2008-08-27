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
    private static final Properties props = new Properties();
    private final String host, login, password, subject, sender, recipient;
    private final boolean isEnabled;
    private static SecureMailSender instance;
    private static PreferencesHolder prefs = PreferencesHolder.getInstance();

    // inner class
    private class Mailer extends Thread {
        private final String content;

        Mailer(String content) {
            this.content = content;
        }

        public void run() {
            try {
                send();
                Dispatcher.getReporter().report("Email notification sent.");
            } catch (Throwable t) {
                Dispatcher.getReporter().report("Email notification failed.");
                Dispatcher.getReporter().report(t);
            }
        }

        public void send() throws MessagingException, SendFailedException {
            Session mailSession = Session.getDefaultInstance(props);
            //mailSession.setDebug(true); // sends debugging info to System.out

            MimeMessage message = new MimeMessage(mailSession);
            message.setSubject(subject);
            message.setContent(content, "text/plain");
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            message.setFrom(new InternetAddress(sender));

            Transport transport = mailSession.getTransport();
            transport.connect(host, login, password);

            transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
            transport.close();
        }
    }

    public static synchronized SecureMailSender getInstance() {
        if (instance == null) {
            instance = new SecureMailSender();
        }
        return instance;
    }

    // private constructor for noninstantiability
    private SecureMailSender(String smtpsHost, String login, String password, String from, String to, String subject) {
        isEnabled = prefs.get(EmailMonitoring).equalsIgnoreCase("enabled");
        props.setProperty("mail.transport.protocol", "smtps");
        props.setProperty("mail.smtps.auth", "true");

        this.host = smtpsHost;
        this.login = login;
        this.sender = from.isEmpty() ? login : from;
        this.recipient = to;
        this.password = password;
        this.subject = subject;
    }

    // private constructor for noninstantiability
    private SecureMailSender() {
        this(prefs.get(SMTPSHost), prefs.get(EmailLogin), prefs.get(EmailPassword), prefs.get(From), prefs.get(To), prefs.get(EmailSubject));
    }


    public void send(String content) {
        if (isEnabled) {
            new Mailer(content).start();
        }
    }

    static public void test(String SMTPSHost, String login, String password, String from, String to, String subject) throws MessagingException {
        new SecureMailSender(SMTPSHost, login, password, from, to, subject).new Mailer("JBT remote notification email test.").send();
    }

}
