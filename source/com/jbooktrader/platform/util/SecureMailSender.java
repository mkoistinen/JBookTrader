package com.jbooktrader.platform.util;

import com.jbooktrader.platform.model.*;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

/**
 * Sends SSL Mail
 */
public class SecureMailSender {

    private static SecureMailSender instance;
    private final Properties props = new Properties();
    private final String host, user, password, subject, recipient;

    // inner class
    private class Mailer extends Thread {
        final String content;

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
                transport.connect(host, user, password);
                transport.sendMessage(message, message.getRecipients(Message.RecipientType.TO));
                transport.close();

            } catch (Exception e) {
                Dispatcher.getReporter().report(e);
            }

        }
    }

    public static synchronized SecureMailSender getInstance() throws JBookTraderException {
        if (instance == null) {
            instance = new SecureMailSender();
        }
        return instance;
    }

    // private constructor for noninstantiability
    private SecureMailSender() throws JBookTraderException {
        props.put("mail.transport.protocol", "smtps");
        props.put("mail.smtps.auth", "true");
        props.put("mail.smtps.quitwait", "false");

        PropertiesHolder properties = PropertiesHolder.getInstance();
        host = properties.getProperty("mail.host");
        user = properties.getProperty("mail.user");
        password = properties.getProperty("mail.password");
        subject = properties.getProperty("mail.subject");
        recipient = properties.getProperty("mail.recipient");
    }

    public void send(String content) {
        new Mailer(content).start();
    }

}
