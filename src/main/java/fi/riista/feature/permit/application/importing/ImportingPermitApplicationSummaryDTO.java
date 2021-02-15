package fi.riista.feature.permit.application.importing;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.feature.permit.application.DeliveryAddressDTO;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.PermitHolderDTO;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.derogation.attachments.DerogationPermitApplicationAttachmentDTO;
import fi.riista.feature.permit.application.importing.amount.ImportingPermitApplicationSpeciesAmountDTO;
import fi.riista.feature.permit.application.importing.justification.ImportingPermitApplicationJustificationDTO;
import fi.riista.feature.permit.application.importing.period.ImportingPermitApplicationSpeciesPeriodDTO;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.LocalDateTime;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class ImportingPermitApplicationSummaryDTO {

    public static ImportingPermitApplicationSummaryDTO from(final @Nonnull HarvestPermitApplication application,
                                                            final @Nonnull ImportingPermitApplication importingPermitApplication,
                                                            final @Nonnull List<HarvestPermitApplicationSpeciesAmount> amounts,
                                                            final @Nonnull List<HarvestPermitApplicationAttachment> areaAttachments,
                                                            final @Nonnull List<HarvestPermitApplicationAttachment> otherAttachments) {
        requireNonNull(application);
        requireNonNull(importingPermitApplication);
        requireNonNull(amounts);
        requireNonNull(areaAttachments);
        requireNonNull(otherAttachments);
        
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
                .withAreaDescription(importingPermitApplication.getAreaDescription())
                .withValidityYears(application.getValidityYears())
                .withSpeciesAmounts(F.mapNonNullsToList(amounts, ImportingPermitApplicationSpeciesAmountDTO::new))
                .withPeriods(F.mapNonNullsToList(amounts, ImportingPermitApplicationSpeciesPeriodDTO::new))
                .withJustification(new ImportingPermitApplicationJustificationDTO(importingPermitApplication))
                .withAreaAttachments(F.mapNonNullsToList(areaAttachments, DerogationPermitApplicationAttachmentDTO::new))
                .withOtherAttachments(F.mapNonNullsToList(otherAttachments, DerogationPermitApplicationAttachmentDTO::new))
                .withEmail1(Optional.ofNullable(application.getEmail1()).orElse(null))
                .withEmail2(Optional.ofNullable(application.getEmail2()).orElse(null))
                .withDeliveryByMail(Optional.ofNullable(application.getDeliveryByMail()).orElse(false))
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
    private final String areaDescription;
    private final Integer validityYears;
    private final List<ImportingPermitApplicationSpeciesAmountDTO> speciesAmounts;
    private final List<ImportingPermitApplicationSpeciesPeriodDTO> periods;
    private final ImportingPermitApplicationJustificationDTO justification;
    private final List<DerogationPermitApplicationAttachmentDTO> areaAttachments;
    private final List<DerogationPermitApplicationAttachmentDTO> otherAttachments;
    private final String email1;
    private final String email2;
    private final Boolean deliveryByMail;
    private final DeliveryAddressDTO deliveryAddress;
    private final String decisionLanguage;

    private ImportingPermitApplicationSummaryDTO(final long id,
                                                 final @Nonnull HarvestPermitApplication.Status status,
                                                 final Integer applicationNumber,
                                                 final @Nonnull String applicationName,
                                                 final int applicationYear,
                                                 final @Nonnull HarvestPermitCategory harvestPermitCategory,
                                                 final LocalDateTime submitDate,
                                                 final @Nonnull PersonContactInfoDTO contactPerson,
                                                 final PermitHolderDTO permitHolder,
                                                 final String areaDescription,
                                                 final Integer validityYears, final @Nonnull List<ImportingPermitApplicationSpeciesAmountDTO> speciesAmounts,
                                                 final @Nonnull List<ImportingPermitApplicationSpeciesPeriodDTO> periods,
                                                 final @Nonnull ImportingPermitApplicationJustificationDTO justification,
                                                 final @Nonnull List<DerogationPermitApplicationAttachmentDTO> areaAttachments,
                                                 final @Nonnull List<DerogationPermitApplicationAttachmentDTO> otherAttachments,
                                                 final String email1, final String email2,
                                                 final boolean deliveryByMail, final DeliveryAddressDTO deliveryAddress,
                                                 final @Nonnull String decisionLanguage) {
        this.validityYears = validityYears;

        requireNonNull(status);
        requireNonNull(applicationName);
        requireNonNull(harvestPermitCategory);
        requireNonNull(contactPerson);
        requireNonNull(speciesAmounts);
        requireNonNull(periods);
        requireNonNull(justification);
        requireNonNull(areaAttachments);
        requireNonNull(otherAttachments);
        requireNonNull(decisionLanguage);

        this.id = id;
        this.status = status;
        this.applicationNumber = applicationNumber;
        this.applicationName = requireNonNull(applicationName);
        this.applicationYear = applicationYear;
        this.harvestPermitCategory = harvestPermitCategory;
        this.submitDate = submitDate;
        this.contactPerson = contactPerson;
        this.permitHolder = permitHolder;
        this.areaDescription = areaDescription;
        this.speciesAmounts = speciesAmounts;
        this.periods = periods;
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

    public String getAreaDescription() {
        return areaDescription;
    }

    public Integer getValidityYears() {
        return validityYears;
    }

    public List<ImportingPermitApplicationSpeciesAmountDTO> getSpeciesAmounts() {
        return speciesAmounts;
    }

    public List<ImportingPermitApplicationSpeciesPeriodDTO> getPeriods() {
        return periods;
    }

    public ImportingPermitApplicationJustificationDTO getJustification() {
        return justification;
    }

    public List<DerogationPermitApplicationAttachmentDTO> getAreaAttachments() {
        return areaAttachments;
    }

    public List<DerogationPermitApplicationAttachmentDTO> getOtherAttachments() {
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
        private List<ImportingPermitApplicationSpeciesAmountDTO> speciesAmounts;
        private List<ImportingPermitApplicationSpeciesPeriodDTO> periods;
        private ImportingPermitApplicationJustificationDTO justification;
        private List<DerogationPermitApplicationAttachmentDTO> areaAttachments;
        private List<DerogationPermitApplicationAttachmentDTO> otherAttachments;
        private String email1;
        private String email2;
        private Boolean deliveryByMail;
        private DeliveryAddressDTO deliveryAddress;
        private String decisionLanguage;
        private String areaDescription;
        private Integer validityYears;

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

        public Builder withAreaDescription(final String areaDescription) {
            this.areaDescription = areaDescription;
            return this;
        }

        public Builder withValidityYears(Integer validityYears) {
            this.validityYears = validityYears;
            return this;
        }

        public Builder withSpeciesAmounts(final List<ImportingPermitApplicationSpeciesAmountDTO> speciesAmounts) {
            this.speciesAmounts = speciesAmounts;
            return this;
        }

        public Builder withPeriods(final List<ImportingPermitApplicationSpeciesPeriodDTO> periods) {
            this.periods = periods;
            return this;
        }

        public Builder withAreaAttachments(final List<DerogationPermitApplicationAttachmentDTO> areaAttachments) {
            this.areaAttachments = areaAttachments;
            return this;
        }

        public Builder withOtherAttachments(final List<DerogationPermitApplicationAttachmentDTO> otherAttachments) {
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

        public Builder withJustification(final ImportingPermitApplicationJustificationDTO justification) {
            this.justification = justification;
            return this;
        }

        public ImportingPermitApplicationSummaryDTO build() {
            return new ImportingPermitApplicationSummaryDTO(id, status, applicationNumber, applicationName,
                    applicationYear, harvestPermitCategory, submitDate, contactPerson, permitHolder,
                    areaDescription, validityYears, speciesAmounts, periods, justification, areaAttachments, otherAttachments,
                    email1, email2, deliveryByMail, deliveryAddress, decisionLanguage);
        }
    }
}
