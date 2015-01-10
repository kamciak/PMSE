package com.publicationmetasearchengine.services.mailingservice;

import com.publicationmetasearchengine.dao.properties.PropertiesManager;
import com.publicationmetasearchengine.services.mailingservice.emailparts.EmailSubject;
import com.publicationmetasearchengine.services.mailingservice.exception.EmailNotSentException;
import com.publicationmetasearchengine.utils.CryptoUtils;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class MailingServiceImpl implements MailingService {

    private static final Logger LOGGER = Logger.getLogger(MailingServiceImpl.class);
    private static final String smptPropertiesPrefix = "mailingservice.smtp.";
    private static final String accountPropertiesPrefix = "mailingservice.account.";
    private static final String applicationPropertiesPrefix = "mailingservice.application.";

    private InternetAddress fromAddress;
    private Properties properties = new Properties();
    private Session session = null;


    public MailingServiceImpl() {
        final PropertiesManager propertiesManager = PropertiesManager.getInstance();
        properties.put("mail.smtp.host", propertiesManager.getProperty(smptPropertiesPrefix+"host"));
        properties.put("mail.smtp.socketFactory.port", propertiesManager.getProperty(smptPropertiesPrefix+"port"));
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.port", propertiesManager.getProperty(smptPropertiesPrefix+"port"));

        session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(propertiesManager.getProperty(accountPropertiesPrefix+"login"),
                        CryptoUtils.decrypt(propertiesManager.getProperty(accountPropertiesPrefix+"password")));
            }
        });
        try {
            fromAddress = new InternetAddress(String.format("%s<%s>", propertiesManager.getProperty(applicationPropertiesPrefix+"name"),
                    propertiesManager.getProperty(applicationPropertiesPrefix+"email")));
        } catch (AddressException ex) {
            LOGGER.fatal("Address is incorrect", ex);
        }
        LOGGER.info("Initialized");
    }

    @Override
    public Email createEmail(EmailSubject subject) {
        return new Email(this, subject);
    }

    @Override
    public void sendEmail(Email email) throws EmailNotSentException{
        try {
            Message message = new MimeMessage(session);
            message.setFrom(fromAddress);
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(email.getToAddress()));
            message.setSubject(email.getSubject().getSubject());
            message.setText(email.getBody().getBody());

            LOGGER.debug(String.format("Sending email %s", email.toString()));
            Transport.send(message);
            LOGGER.info("Email sent");
        } catch (MessagingException ex) {
            LOGGER.error("Email not sent", ex);
            throw new EmailNotSentException("Email not sent.\nPlease try again later");
        }
    }
}
