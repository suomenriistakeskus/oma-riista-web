package fi.riista.integration.srva.callring;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class SrvaCallRingConfiguration {
    private String rhyOfficialCode;
    private List<Phonenumber.PhoneNumber> phoneNumbers;

    private List<String> notificationEmails;

    public SrvaCallRingConfiguration(final String rhyOfficialCode,
                                     final List<Phonenumber.PhoneNumber> phoneNumbers,
                                     final List<String> notificationEmails) {
        this.rhyOfficialCode = Objects.requireNonNull(rhyOfficialCode);
        this.phoneNumbers = Objects.requireNonNull(phoneNumbers);
        this.notificationEmails = Objects.requireNonNull(notificationEmails);
    }

    public String getRhyOfficialCode() {
        return rhyOfficialCode;
    }

    public List<String> getFormattedPhoneNumbers() {
        return phoneNumbers.stream()
                .map(p -> PhoneNumberUtil.getInstance().format(p, PhoneNumberUtil.PhoneNumberFormat.E164))
                .map(p -> p.startsWith("+") ? p.substring(1) : p)
                .collect(toList());
    }

    public List<String> getNotificationEmails() {
        return notificationEmails;
    }
}
