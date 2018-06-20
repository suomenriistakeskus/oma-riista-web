package fi.riista.feature.mail;

import org.springframework.mail.MailParseException;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Date;

public interface HasMailMessageFields {
    String getFrom();

    String getSubject();

    String getBody();

    default void prepareMimeMessage(final MimeMessage mimeMessage, final String toEmail) {
        try {
            final MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, "UTF-8");

            messageHelper.setTo(toEmail);
            messageHelper.setFrom(getFrom());
            messageHelper.setSubject(getSubject());
            messageHelper.setText(getBody(), true);
            messageHelper.setValidateAddresses(true);
            mimeMessage.setSentDate(new Date());
            mimeMessage.saveChanges();

        } catch (MessagingException ex) {
            throw new MailParseException(ex);
        } catch (Exception ex) {
            throw new MailPreparationException(ex);
        }
    }
}
