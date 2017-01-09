package fi.riista.feature.sms.delivery;

import com.google.i18n.phonenumbers.Phonenumber;
import fi.riista.feature.sms.storage.SMSPersistentMessage;

import java.util.List;

public interface SMSGatewayProvider {
    List<SMSPersistentMessage> pollForIncomingMessages();

    SMSPersistentMessage sendMessage(Phonenumber.PhoneNumber phoneNumber, String message);
}
