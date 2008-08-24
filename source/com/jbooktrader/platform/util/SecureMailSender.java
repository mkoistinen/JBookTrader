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
    private final Properties props = new Properties();
    private String login, password, from, subject, recipient;
    private final boolean isEnabled;
    private static SecureMailSender instance;

    // inner class
    private class Mailer extends Thread {
        private final String content;

        Mailer(String content) {
            this.content = content;
        }

        public void run() {
            try {
            	send();
                Dispatcher.getReporter().report("Email notification sent");
            } catch (Throwable t) {
                Dispatcher.getReporter().report("Email notification failed");
                Dispatcher.getReporter().report(t);
            }
        }
        
        public void send() throws MessagingException, SendFailedException {
            Session mailSession = Session.getDefaultInstance(props);
            //mailSession.setDebug(true); // sends debugging info to System.out

            MimeMessage message = new MimeMessage(mailSession);
            message.setFrom(new InternetAddress(from));
            message.setSubject(subject);
            message.setContent(content, "text/plain");
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));

            Transport transport = mailSession.getTransport();
            transport.connect(login, password);

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
    private SecureMailSender() {
        PreferencesHolder prefs = PreferencesHolder.getInstance();
        isEnabled = prefs.get(EmailMonitoring).equalsIgnoreCase("enabled");        
        _init(prefs.get(SMTPSHost), prefs.get(EmailLogin), prefs.get(EmailPassword), prefs.get(From), prefs.get(To), prefs.get(EmailSubject));
    }
    
    // private constructor for noninstantiability
    private SecureMailSender(String SMTPSHost, String login, String password, String from, String to, String subject) {
    	isEnabled = false;
    	_init(SMTPSHost, login, password, from, to , subject);
    }
    
    private void _init(String SMTPSHost, String login, String password, String from, String to, String subject) {
        props.put("mail.transport.protocol", "smtps");
        props.put("mail.smtps.host", SMTPSHost);
        props.put("mail.smtps.auth", "true");

        this.from = from;
        if(login.length()==0) {
        	this.login = from;
        }
        else {
            this.login = login;
        }
        this.recipient = to;
        this.password = password;
        this.subject = subject;    	
    }

    public void send(String content) {
        if (isEnabled) {
            new Mailer(content).start();
        }
    }

    static public void sendTest(String SMTPSHost, String login, String password, String from, String to, String subject) throws MessagingException, SendFailedException {
    	new SecureMailSender(SMTPSHost, login, password, from, to, subject)._sendTest();
    	
    }
    
    private void _sendTest() throws MessagingException, SendFailedException {
    	new Mailer("JBT remote notification email test.").send();
    }
}
