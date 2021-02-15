package fi.riista.feature.permit.application.dogevent.summary;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.feature.permit.application.DeliveryAddress;
import fi.riista.feature.permit.application.DeliveryAddressDTO;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.application.PermitHolderDTO;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.derogation.area.DerogationPermitApplicationAreaDTO;
import fi.riista.feature.permit.application.derogation.attachments.DerogationPermitApplicationAttachmentDTO;
import fi.riista.feature.permit.application.dogevent.DogEventApplication;
import fi.riista.feature.permit.application.dogevent.DogEventUnleash;
import fi.riista.feature.permit.application.dogevent.unleash.DogEventUnleashDTO;
import fi.riista.util.DateUtil;
import fi.riista.util.LocalisedString;
import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class DogEventUnleashSummaryDTO implements Serializable {

    private DogEventUnleashSummaryDTO(final Builder builder) {

        this.id = builder.id;
        this.status = builder.status;
        this.applicationNumber = builder.applicationNumber;
        this.applicationName = builder.applicationName;
        this.huntingYear = builder.huntingYear;
        this.harvestPermitCategory = builder.harvestPermitCategory;
        this.submitDate = builder.submitDate;
        this.contactPerson = builder.contactPerson;
        this.permitHolder = builder.permitHolder;
        this.email1 = builder.email1;
        this.email2 = builder.email2;
        this.deliveryByMail = builder.deliveryByMail;
        this.deliveryAddress = builder.deliveryAddress;
        this.decisionLanguage = builder.decisionLanguage;
        this.areaAttachments = builder.areaAttachments;
        this.otherAttachments = builder.otherAttachments;
        this.derogationPermitApplicationAreaDTO = builder.derogationPermitApplicationAreaDTO;
        this.events = builder.events;
    }

    public static DogEventUnleashSummaryDTO create(final @Nonnull HarvestPermitApplication application,
                                                   final @Nonnull DogEventApplication dogEventApplication,
                                                   final List<DogEventUnleash> dogEvents) {

        requireNonNull(application);
        requireNonNull(dogEventApplication);

        return new Builder()
                .withHarvestPermitApplication(application)
                .withDogEventApplication(dogEventApplication)
                .withDogEvents(dogEvents)
                .build();
    }

    private final long id;
    private final HarvestPermitApplication.Status status;
    private final Integer applicationNumber;
    private final String applicationName;
    private final int huntingYear;
    private final HarvestPermitCategory harvestPermitCategory;
    private final LocalDateTime submitDate;
    private final PersonContactInfoDTO contactPerson;
    private final PermitHolderDTO permitHolder;
    private final String email1;
    private final String email2;
    private final Boolean deliveryByMail;
    private final DeliveryAddressDTO deliveryAddress;
    private final String decisionLanguage;
    private final List<DerogationPermitApplicationAttachmentDTO> areaAttachments;
    private final List<DerogationPermitApplicationAttachmentDTO> otherAttachments;
    private final DerogationPermitApplicationAreaDTO derogationPermitApplicationAreaDTO;
    private final List<DogEventUnleashDTO> events;

    public long getId() {
        return id;
    }

    public HarvestPermitApplication.Status getStatus() {
        return status;
    }

    public Integer getApplicationNumber() {
        return applicationNumber;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public int getHuntingYear() {
        return huntingYear;
    }

    public HarvestPermitCategory getHarvestPermitCategory() {
        return harvestPermitCategory;
    }

    public LocalDateTime getSubmitDate() {
        return submitDate;
    }

    public PersonContactInfoDTO getContactPerson() {
        return contactPerson;
    }

    public PermitHolderDTO getPermitHolder() {
        return permitHolder;
    }

    public String getEmail1() {
        return email1;
    }

    public String getEmail2() {
        return email2;
    }

    public Boolean getDeliveryByMail() {
        return deliveryByMail;
    }

    public DeliveryAddressDTO getDeliveryAddress() {
        return deliveryAddress;
    }

    public String getDecisionLanguage() {
        return decisionLanguage;
    }

    public List<DerogationPermitApplicationAttachmentDTO> getAreaAttachments() {
        return areaAttachments;
    }

    public List<DerogationPermitApplicationAttachmentDTO> getOtherAttachments() {
        return otherAttachments;
    }

    public DerogationPermitApplicationAreaDTO getDerogationPermitApplicationAreaDTO() {
        return derogationPermitApplicationAreaDTO;
    }

    public List<DogEventUnleashDTO> getEvents() {
        return events;
    }

    /**
     *
     * Builder
     *
     */

    public static final class Builder {
        // From HarvestPermitApplication
        private long id;
        private HarvestPermitApplication.Status status;
        private Integer applicationNumber;
        private String applicationName;
        private int huntingYear;
        private HarvestPermitCategory harvestPermitCategory;
        private LocalDateTime submitDate;
        private PersonContactInfoDTO contactPerson;
        private PermitHolderDTO permitHolder;
        private String email1;
        private String email2;
        private Boolean deliveryByMail;
        private DeliveryAddressDTO deliveryAddress;
        private String decisionLanguage;
        private List<DerogationPermitApplicationAttachmentDTO> areaAttachments;
        private List<DerogationPermitApplicationAttachmentDTO> otherAttachments;
        // From DogEventApplication
        private DerogationPermitApplicationAreaDTO derogationPermitApplicationAreaDTO;
        // From DogEvent
        private List<DogEventUnleashDTO> events;

        public Builder() {}

        public Builder withHarvestPermitApplication(final HarvestPermitApplication application) {

            return withId(application.getId())
                    .withStatus(application.getStatus())
                    .withApplicationNumber(application.getApplicationNumber())
                    .withApplicationName(application.getHarvestPermitCategory().getApplicationName(), application.getLocale())
                    .withHuntingYear(application.getApplicationYear())
                    .withHarvestPermitCategory(application.getHarvestPermitCategory())
                    .withSubmitDate(application.getSubmitDate())
                    .withContactPerson(application.getContactPerson())
                    .withPermitHolder(application.getPermitHolder())
                    .withEmail1(application.getEmail1())
                    .withEmail2(application.getEmail2())
                    .withDeliveryByMail(application.getDeliveryByMail())
                    .withDeliveryAddress(application.getDeliveryAddress())
                    .withDecisionLanguage(application.getDecisionLocale().getLanguage())
                    .withAreaAttachments(application.getAttachments())
                    .withOtherAttachments(application.getAttachments());
        }

        public Builder withDogEventApplication(final DogEventApplication dogEventApplication) {

            return withArea(dogEventApplication);
        }

        public Builder withDogEvents(final List<DogEventUnleash> events) {

            this.events = events.stream()
                    .map(DogEventUnleashDTO::createFrom)
                    .collect(toList());
            return this;
        }

        public Builder withId(final Long id) {

            this.id = id;
            return this;
        }

        public Builder withStatus(final HarvestPermitApplication.Status status) {

            this.status = status;
            return this;
        }

        public Builder withApplicationNumber(final Integer applicationNumber) {

            this.applicationNumber = applicationNumber;
            return this;
        }

        public Builder withApplicationName(final LocalisedString applicationName, final Locale locale) {

            this.applicationName = applicationName.getTranslation(locale);
            return this;
        }

        public Builder withHuntingYear(final int huntingYear) {

            this.huntingYear = huntingYear;
            return this;
        }

        public Builder withHarvestPermitCategory(final HarvestPermitCategory harvestPermitCategory) {

            this.harvestPermitCategory = harvestPermitCategory;
            return this;
        }

        public Builder withSubmitDate(final DateTime submitDate) {

            this.submitDate = DateUtil.toLocalDateTimeNullSafe(submitDate);
            return this;
        }

        public Builder withContactPerson(final Person contactPerson) {

            this.contactPerson = Optional.ofNullable(contactPerson)
                    .map(PersonContactInfoDTO::create)
                    .orElse(null);
            return this;
        }

        public Builder withPermitHolder(final PermitHolder permitHolder) {

            this.permitHolder = PermitHolderDTO.createFrom(permitHolder);
            return this;
        }

        public Builder withEmail1(final String email1) {

            this.email1 = email1;
            return this;
        }

        public Builder withEmail2(final String email2) {
            this.email2 = email2;
            return this;
        }

        public Builder withDeliveryByMail(final Boolean deliveryByMail) {

            this.deliveryByMail = deliveryByMail;
            return this;
        }

        public Builder withDeliveryAddress(final DeliveryAddress deliveryAddress) {

            this.deliveryAddress = DeliveryAddressDTO.fromNullable(deliveryAddress);
            return this;
        }

        public Builder withDecisionLanguage(final String decisionLanguage) {

            this.decisionLanguage = decisionLanguage;
            return this;
        }

        public Builder withAreaAttachments(final List<HarvestPermitApplicationAttachment> attachments) {

            this.areaAttachments = attachments.stream()
                    .filter(a -> a.getAttachmentType() == HarvestPermitApplicationAttachment.Type.PROTECTED_AREA)
                    .map(DerogationPermitApplicationAttachmentDTO::new)
                    .collect(toList());
            return this;
        }

        public Builder withOtherAttachments(final List<HarvestPermitApplicationAttachment> attachments) {

            this.otherAttachments = attachments.stream()
                    .filter(a -> a.getAttachmentType() == HarvestPermitApplicationAttachment.Type.OTHER)
                    .map(DerogationPermitApplicationAttachmentDTO::new)
                    .collect(toList());
            return this;
        }

        public Builder withArea(final DogEventApplication dogEventApplication) {

            this.derogationPermitApplicationAreaDTO = DerogationPermitApplicationAreaDTO.createFrom(dogEventApplication);
            return this;
        }

        public DogEventUnleashSummaryDTO build() {

            return new DogEventUnleashSummaryDTO(this);
        }
    }
}