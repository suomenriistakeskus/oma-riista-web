package fi.riista.feature.sms.delivery;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import fi.riista.config.profile.EmbeddedDatabase;
import fi.riista.feature.sms.storage.SMSMessageStatus;
import fi.riista.feature.sms.storage.SMSPersistentMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@EmbeddedDatabase
public class MockSMSGatewayProvider implements SMSGatewayProvider {
    private static final Logger LOG = LoggerFactory.getLogger(MockSMSGatewayProvider.class);

    @Override
    public List<SMSPersistentMessage> pollForIncomingMessages() {
        return Collections.emptyList();
    }

    @Override
    public SMSPersistentMessage sendMessage(final Phonenumber.PhoneNumber phoneNumber, final String message) {
        LOG.info("Sending SMS using mock implementation phoneNumber={} message={}", phoneNumber, message);

        final SMSPersistentMessage sms = new SMSPersistentMessage();
        sms.setStatus(SMSMessageStatus.PENDING, null);
        sms.setDirection(SMSPersistentMessage.Direction.OUT);
        sms.setNumberTo(PhoneNumberUtil.getInstance().format(
                phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164));
        sms.setMessage(message);

        return sms;
    }
}
