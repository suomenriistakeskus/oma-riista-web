package fi.riista.feature.permit.application.email;

import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.PermitHolder;

import javax.annotation.Nonnull;
import java.net.URI;
import java.util.Locale;

import static java.util.Objects.requireNonNull;

public class HarvestPermitApplicationNotificationDTO {
    @Nonnull
    public static HarvestPermitApplicationNotificationDTO create(final HarvestPermitApplication application,
                                                                 final URI emailLink) {
        final Person contactPerson = application.getContactPerson();
        final PermitHolder permitHolder = application.getPermitHolder();
        final Locale locale = application.getLocale();

        final HarvestPermitApplicationNotificationDTO.Builder builder = HarvestPermitApplicationNotificationDTO.builder()
                .withLocale(locale)
                .withEmailLink(emailLink)
                .withApplicationNumber(application.getApplicationNumber())
                .withApplicationType(application.getHarvestPermitCategory().getApplicationName().getTranslation(locale))
                .withPermitHolderName(permitHolder.getName())
                .withPermitHolderCode(permitHolder.getCode())
                .withContactPersonFirstName(contactPerson.getFirstName())
                .withContactPersonLastName(contactPerson.getLastName())
                .withContactPersonEmail(contactPerson.getEmail())
                .withContactPersonPhoneNumber(contactPerson.getPhoneNumber());

        final Address contactPersonAddress = contactPerson.getAddress();

        if (contactPersonAddress != null) {
            builder.withContactPersonStreetAddress(contactPersonAddress.getStreetAddress())
                    .withContactPersonPostalCode(contactPersonAddress.getPostalCode())
                    .withContactPersonCity(contactPersonAddress.getCity())
                    .withContactPersonCountry(contactPersonAddress.getCountry());
        }

        return builder.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    private final Locale locale;
    private final URI emailLink;
    private final String applicationType;
    private final int applicationNumber;
    private final String permitHolderCode;
    private final String permitHolderName;
    private final String contactPersonFirstName;
    private final String contactPersonLastName;
    private final String contactPersonEmail;
    private final String contactPersonPhoneNumber;
    private final String contactPersonStreetAddress;
    private final String contactPersonPostalCode;
    private final String contactPersonCity;
    private final String contactPersonCountry;

    private HarvestPermitApplicationNotificationDTO(final Builder builder) {
        this.locale = requireNonNull(builder.locale);
        this.emailLink = requireNonNull(builder.emailLink);
        this.applicationType = requireNonNull(builder.applicationType);
        this.applicationNumber = requireNonNull(builder.applicationNumber);
        this.permitHolderName = requireNonNull(builder.permitHolderName);
        this.permitHolderCode = builder.permitHolderCode;
        this.contactPersonFirstName = requireNonNull(builder.contactPersonFirstName);
        this.contactPersonLastName = requireNonNull(builder.contactPersonLastName);
        this.contactPersonEmail = builder.contactPersonEmail;
        this.contactPersonPhoneNumber = builder.contactPersonPhoneNumber;
        this.contactPersonStreetAddress = builder.contactPersonStreetAddress;
        this.contactPersonPostalCode = builder.contactPersonPostalCode;
        this.contactPersonCity = builder.contactPersonCity;
        this.contactPersonCountry = builder.contactPersonCountry;
    }

    public Locale getLocale() {
        return locale;
    }

    public URI getEmailLink() {
        return emailLink;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public int getApplicationNumber() {
        return applicationNumber;
    }

    public String getContactPersonFirstName() {
        return contactPersonFirstName;
    }

    public String getContactPersonLastName() {
        return contactPersonLastName;
    }

    public String getContactPersonStreetAddress() {
        return contactPersonStreetAddress;
    }

    public String getContactPersonPostalCode() {
        return contactPersonPostalCode;
    }

    public String getContactPersonCity() {
        return contactPersonCity;
    }

    public String getContactPersonCountry() {
        return contactPersonCountry;
    }

    public String getContactPersonPhoneNumber() {
        return contactPersonPhoneNumber;
    }

    public String getContactPersonEmail() {
        return contactPersonEmail;
    }

    public String getPermitHolderCode() {
        return permitHolderCode;
    }

    public String getPermitHolderName() {
        return permitHolderName;
    }

    public static final class Builder {
        private Locale locale;
        private URI emailLink;
        private String applicationType;
        private Integer applicationNumber;
        private String contactPersonFirstName;
        private String contactPersonLastName;
        private String contactPersonStreetAddress;
        private String contactPersonPostalCode;
        private String contactPersonCity;
        private String contactPersonCountry;
        private String contactPersonPhoneNumber;
        private String contactPersonEmail;
        private String permitHolderCode;
        private String permitHolderName;

        private Builder() {
        }

        public Builder withLocale(Locale locale) {
            this.locale = locale;
            return this;
        }

        public Builder withEmailLink(URI emailLink) {
            this.emailLink = emailLink;
            return this;
        }

        public Builder withApplicationType(String applicationType) {
            this.applicationType = applicationType;
            return this;
        }

        public Builder withApplicationNumber(Integer applicationNumber) {
            this.applicationNumber = applicationNumber;
            return this;
        }

        public Builder withContactPersonFirstName(String contactPersonFirstName) {
            this.contactPersonFirstName = contactPersonFirstName;
            return this;
        }

        public Builder withContactPersonLastName(String contactPersonLastName) {
            this.contactPersonLastName = contactPersonLastName;
            return this;
        }

        public Builder withContactPersonStreetAddress(String contactPersonStreetAddress) {
            this.contactPersonStreetAddress = contactPersonStreetAddress;
            return this;
        }

        public Builder withContactPersonPostalCode(String contactPersonPostalCode) {
            this.contactPersonPostalCode = contactPersonPostalCode;
            return this;
        }

        public Builder withContactPersonCity(String contactPersonCity) {
            this.contactPersonCity = contactPersonCity;
            return this;
        }

        public Builder withContactPersonCountry(String contactPersonCountry) {
            this.contactPersonCountry = contactPersonCountry;
            return this;
        }

        public Builder withContactPersonPhoneNumber(String contactPersonPhoneNumber) {
            this.contactPersonPhoneNumber = contactPersonPhoneNumber;
            return this;
        }

        public Builder withContactPersonEmail(String contactPersonEmail) {
            this.contactPersonEmail = contactPersonEmail;
            return this;
        }

        public Builder withPermitHolderCode(String permitHolderCode) {
            this.permitHolderCode = permitHolderCode;
            return this;
        }

        public Builder withPermitHolderName(String permitHolderName) {
            this.permitHolderName = permitHolderName;
            return this;
        }

        public HarvestPermitApplicationNotificationDTO build() {
            return new HarvestPermitApplicationNotificationDTO(this);
        }
    }
}
