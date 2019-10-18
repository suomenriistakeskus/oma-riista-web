package fi.riista.feature.permit.application.carnivore;

import com.google.common.base.Preconditions;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.feature.permit.application.DeliveryAddressDTO;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.PermitHolderDTO;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.carnivore.attachments.CarnivorePermitApplicationAttachmentDTO;
import fi.riista.feature.permit.application.carnivore.justification.CarnivorePermitApplicationJustificationDTO;
import fi.riista.feature.permit.application.carnivore.species.CarnivorePermitApplicationSpeciesAmountDTO;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDateTime;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CarnivorePermitApplicationSummaryDTO {

    public static CarnivorePermitApplicationSummaryDTO from(final @Nonnull HarvestPermitApplication application,
                                                            final @Nonnull CarnivorePermitApplication carnivorePermitApplication) {
        final List<HarvestPermitApplicationSpeciesAmount> speciesAmounts = application.getSpeciesAmounts();
        Preconditions.checkArgument(speciesAmounts.size() <= 1);
        final Optional<HarvestPermitApplicationSpeciesAmount> spaOption = speciesAmounts.stream().findFirst();
        return Builder.builder()
                .withId(application.getId())
                .withStatus(application.getStatus())
                .withApplicationNumber(application.getApplicationNumber())
                .withApplicationName(application.getApplicationName())
                .withApplicationYear(application.getApplicationYear())
                .withHarvestPermitCategory(application.getHarvestPermitCategory())
                .withSubmitDate(DateUtil.toLocalDateTimeNullSafe(application.getSubmitDate()))
                .withContactPerson(PersonContactInfoDTO.create(application.getContactPerson()))
                .withPermitHolder(PermitHolderDTO.createFrom(application.getPermitHolder()))
                .withAreaSize(carnivorePermitApplication.getAreaSize())
                .withAreaDescription(carnivorePermitApplication.getAreaDescription())
                .withSpeciesAmounts(
                        spaOption.map(spa ->
                                CarnivorePermitApplicationSpeciesAmountDTO.create(application, spa))
                                .orElse(null))
                .withJustification(spaOption.map(spa ->
                        new CarnivorePermitApplicationJustificationDTO(carnivorePermitApplication, spa))
                        .orElse(null))
                .withAreaAttachments(application.getAttachments().stream()
                        .filter(a -> a.getAttachmentType() == HarvestPermitApplicationAttachment.Type.PROTECTED_AREA)
                        .map(CarnivorePermitApplicationAttachmentDTO::new)
                        .collect(Collectors.toList()))
                .withOtherAttachments(application.getAttachments().stream()
                        .filter(a -> a.getAttachmentType() == HarvestPermitApplicationAttachment.Type.OTHER)
                        .map(CarnivorePermitApplicationAttachmentDTO::new)
                        .collect(Collectors.toList()))
                .withEmail1(Optional.ofNullable(application.getEmail1()).orElse(null))
                .withEmail2(Optional.ofNullable(application.getEmail2()).orElse(null))
                .withDeliveryByMail(Optional.ofNullable(application.getDeliveryByMail()).orElse(null))
                .withDeliveryAddress(DeliveryAddressDTO.fromNullable(application.getDeliveryAddress()))
                .withDecisionLanguage(application.getDecisionLocale().getLanguage())
                .build();
    }

    private final long id;
    private final HarvestPermitApplication.Status status;
    private final Integer applicationNumber;
    private final String applicationName;
    private final int applicationYear;
    private final HarvestPermitCategory harvestPermitCategory;
    private final LocalDateTime submitDate;
    private final PersonContactInfoDTO contactPerson;
    private final PermitHolderDTO permitHolder;
    private final int areaSize;
    private final String areaDescription;
    private final CarnivorePermitApplicationSpeciesAmountDTO speciesAmounts;
    private final CarnivorePermitApplicationJustificationDTO justification;
    private final List<CarnivorePermitApplicationAttachmentDTO> areaAttachments;
    private final List<CarnivorePermitApplicationAttachmentDTO> otherAttachments;
    private final String email1;
    private final String email2;
    private final Boolean deliveryByMail;
    private final DeliveryAddressDTO deliveryAddress;
    private final String decisionLanguage;

    private CarnivorePermitApplicationSummaryDTO(final long id, final HarvestPermitApplication.Status status,
                                                 final Integer applicationNumber, final String applicationName,
                                                 final int applicationYear,
                                                 final HarvestPermitCategory harvestPermitCategory,
                                                 final LocalDateTime submitDate,
                                                 final PersonContactInfoDTO contactPerson,
                                                 final PermitHolderDTO permitHolder,
                                                 final int areaSize, final String areaDescription,
                                                 final CarnivorePermitApplicationSpeciesAmountDTO speciesAmounts,
                                                 final CarnivorePermitApplicationJustificationDTO justification,
                                                 final List<CarnivorePermitApplicationAttachmentDTO> areaAttachments,
                                                 final List<CarnivorePermitApplicationAttachmentDTO> otherAttachments,
                                                 final String email1, final String email2,
                                                 final Boolean deliveryByMail, final DeliveryAddressDTO deliveryAddress,
                                                 final String decisionLanguage) {
        this.id = id;
        this.status = status;
        this.applicationNumber = applicationNumber;
        this.applicationName = applicationName;
        this.applicationYear = applicationYear;
        this.harvestPermitCategory = harvestPermitCategory;
        this.submitDate = submitDate;
        this.contactPerson = contactPerson;
        this.permitHolder = permitHolder;
        this.areaSize = areaSize;
        this.areaDescription = areaDescription;
        this.speciesAmounts = speciesAmounts;
        this.justification = justification;
        this.areaAttachments = areaAttachments;
        this.otherAttachments = otherAttachments;
        this.email1 = email1;
        this.email2 = email2;
        this.deliveryByMail = deliveryByMail;
        this.deliveryAddress = deliveryAddress;
        this.decisionLanguage = decisionLanguage;
    }

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

    public int getApplicationYear() {
        return applicationYear;
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

    public int getAreaSize() {
        return areaSize;
    }

    public String getAreaDescription() {
        return areaDescription;
    }

    public CarnivorePermitApplicationSpeciesAmountDTO getSpeciesAmounts() {
        return speciesAmounts;
    }

    public CarnivorePermitApplicationJustificationDTO getJustification() {
        return justification;
    }

    public List<CarnivorePermitApplicationAttachmentDTO> getAreaAttachments() {
        return areaAttachments;
    }

    public List<CarnivorePermitApplicationAttachmentDTO> getOtherAttachments() {
        return otherAttachments;
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

    public static final class Builder {
        private long id;
        private HarvestPermitApplication.Status status;
        private Integer applicationNumber;
        private String applicationName;
        private int applicationYear;
        private HarvestPermitCategory harvestPermitCategory;
        private LocalDateTime submitDate;
        private PersonContactInfoDTO contactPerson;
        private PermitHolderDTO permitHolder;
        private CarnivorePermitApplicationSpeciesAmountDTO speciesAmounts;
        private CarnivorePermitApplicationJustificationDTO justification;
        private List<CarnivorePermitApplicationAttachmentDTO> areaAttachments;
        private List<CarnivorePermitApplicationAttachmentDTO> otherAttachments;
        private String email1;
        private String email2;
        private Boolean deliveryByMail;
        private DeliveryAddressDTO deliveryAddress;
        private String decisionLanguage;
        private int areaSize;
        private String areaDescription;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder withId(final long id) {
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

        public Builder withApplicationName(final String applicationName) {
            this.applicationName = applicationName;
            return this;
        }

        public Builder withApplicationYear(final int applicationYear) {
            this.applicationYear = applicationYear;
            return this;
        }

        public Builder withHarvestPermitCategory(final HarvestPermitCategory harvestPermitCategory) {
            this.harvestPermitCategory = harvestPermitCategory;
            return this;
        }

        public Builder withSubmitDate(final LocalDateTime submitDate) {
            this.submitDate = submitDate;
            return this;
        }

        public Builder withContactPerson(final PersonContactInfoDTO contactPerson) {
            this.contactPerson = contactPerson;
            return this;
        }

        public Builder withPermitHolder(final PermitHolderDTO permitHolder) {
            this.permitHolder = permitHolder;
            return this;
        }

        public Builder withAreaSize(final int areaSize) {
            this.areaSize = areaSize;
            return this;
        }

        public Builder withAreaDescription(final String areaDescription) {
            this.areaDescription = areaDescription;
            return this;
        }

        public Builder withSpeciesAmounts(final CarnivorePermitApplicationSpeciesAmountDTO speciesAmounts) {
            this.speciesAmounts = speciesAmounts;
            return this;
        }

        public Builder withAreaAttachments(final List<CarnivorePermitApplicationAttachmentDTO> areaAttachments) {
            this.areaAttachments = areaAttachments;
            return this;
        }

        public Builder withOtherAttachments(final List<CarnivorePermitApplicationAttachmentDTO> otherAttachments) {
            this.otherAttachments = otherAttachments;
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

        public Builder withDeliveryAddress(final DeliveryAddressDTO deliveryAddress) {
            this.deliveryAddress = deliveryAddress;
            return this;
        }

        public Builder withDecisionLanguage(final String decisionLanguage) {
            this.decisionLanguage = decisionLanguage;
            return this;
        }

        public Builder withJustification(final CarnivorePermitApplicationJustificationDTO justification) {
            this.justification = justification;
            return this;
        }

        public CarnivorePermitApplicationSummaryDTO build() {
            return new CarnivorePermitApplicationSummaryDTO(id, status, applicationNumber, applicationName,
                    applicationYear, harvestPermitCategory, submitDate, contactPerson, permitHolder, areaSize,
                    areaDescription, speciesAmounts, justification, areaAttachments, otherAttachments, email1, email2
                    , deliveryByMail,
                    deliveryAddress, decisionLanguage);
        }
    }
}
