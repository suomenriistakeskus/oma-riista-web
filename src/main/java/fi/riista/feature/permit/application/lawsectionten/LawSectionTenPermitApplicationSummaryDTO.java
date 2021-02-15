package fi.riista.feature.permit.application.lawsectionten;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.feature.permit.application.DeliveryAddressDTO;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.PermitHolderDTO;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.derogation.area.DerogationPermitApplicationAreaDTO;
import fi.riista.feature.permit.application.derogation.attachments.DerogationPermitApplicationAttachmentDTO;
import fi.riista.feature.permit.application.derogation.population.DerogationPermitApplicationSpeciesPopulationDTO;
import fi.riista.feature.permit.application.lawsectionten.amount.LawSectionTenPermitApplicationSpeciesAmountDTO;
import fi.riista.feature.permit.application.lawsectionten.period.LawSectionTenPermitApplicationSpeciesPeriodDTO;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDateTime;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class LawSectionTenPermitApplicationSummaryDTO {

    public LawSectionTenPermitApplicationSummaryDTO(final Builder builder) {
        this.id = builder.id;
        this.status = builder.status;
        this.applicationNumber = builder.applicationNumber;
        this.applicationName = builder.applicationName;
        this.huntingYear = builder.huntingYear;
        this.harvestPermitCategory = builder.harvestPermitCategory;
        this.submitDate = builder.submitDate;
        this.contactPerson = builder.contactPerson;
        this.permitHolder = builder.permitHolder;
        this.speciesAmount = builder.speciesAmount;
        this.speciesPeriod = builder.speciesPeriod;
        this.population = builder.population;
        this.areaAttachments = builder.areaAttachments;
        this.otherAttachments = builder.otherAttachments;
        this.derogationPermitApplicationAreaDTO = builder.derogationPermitApplicationAreaDTO;
        this.email1 = builder.email1;
        this.email2 = builder.email2;
        this.deliveryByMail = builder.deliveryByMail;
        this.deliveryAddress = builder.deliveryAddress;
        this.decisionLanguage = builder.decisionLanguage;
    }

    public static LawSectionTenPermitApplicationSummaryDTO create(final @Nonnull HarvestPermitApplication entity,
                                                                  final @Nonnull LawSectionTenPermitApplication lawSectionTenPermitApplication) {
        requireNonNull(entity);
        requireNonNull(lawSectionTenPermitApplication);

        return new Builder()
                .withId(entity.getId())
                .withStatus(entity.getStatus())
                .withApplicationNumber(entity.getApplicationNumber())
                .withApplicationName(entity.getHarvestPermitCategory().getApplicationName().getTranslation(entity.getLocale()))
                .withHuntingYear(entity.getApplicationYear())
                .withCategory(entity.getHarvestPermitCategory())
                .withSubmitDate(DateUtil.toLocalDateTimeNullSafe(entity.getSubmitDate()))
                .withContactPerson(Optional.ofNullable(entity.getContactPerson())
                        .map(PersonContactInfoDTO::create)
                        .orElse(null))
                .withPermitHolder(PermitHolderDTO.createFrom(entity.getPermitHolder()))
                .withSpeciesAmount(entity.getSpeciesAmounts().stream()
                        .map(LawSectionTenPermitApplicationSpeciesAmountDTO::new)
                        .findFirst()
                        .orElse(null))
                .withSpeciesPeriod(entity.getSpeciesAmounts().stream()
                        .map(LawSectionTenPermitApplicationSpeciesPeriodDTO::new)
                        .findFirst()
                        .orElse(null))
                .withPopulation(entity.getSpeciesAmounts().stream()
                        .map(DerogationPermitApplicationSpeciesPopulationDTO::new)
                        .findFirst()
                        .orElse(null))
                .withAreaAttachments(entity.getAttachments().stream()
                        .filter(a -> a.getAttachmentType() == HarvestPermitApplicationAttachment.Type.PROTECTED_AREA)
                        .map(DerogationPermitApplicationAttachmentDTO::new)
                        .collect(Collectors.toList()))
                .withOtherAttachments(entity.getAttachments().stream()
                        .filter(a -> a.getAttachmentType() == HarvestPermitApplicationAttachment.Type.OTHER)
                        .map(DerogationPermitApplicationAttachmentDTO::new)
                        .collect(Collectors.toList()))
                .withArea(DerogationPermitApplicationAreaDTO.createFrom(lawSectionTenPermitApplication))
                .withEmail1(Optional.ofNullable(entity.getEmail1()).orElse(null))
                .withEmail2(Optional.ofNullable(entity.getEmail2()).orElse(null))
                .withDeliveryByMail(Optional.ofNullable(entity.getDeliveryByMail()).orElse(null))
                .withDeliveryAddress(DeliveryAddressDTO.fromNullable(entity.getDeliveryAddress()))
                .withDecisionLanguage(entity.getDecisionLocale().getLanguage())
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
    private final LawSectionTenPermitApplicationSpeciesAmountDTO speciesAmount;
    private final LawSectionTenPermitApplicationSpeciesPeriodDTO speciesPeriod;
    private final DerogationPermitApplicationSpeciesPopulationDTO population;
    private final List<DerogationPermitApplicationAttachmentDTO> areaAttachments;
    private final List<DerogationPermitApplicationAttachmentDTO> otherAttachments;
    private final DerogationPermitApplicationAreaDTO derogationPermitApplicationAreaDTO;
    private final String email1;
    private final String email2;
    private final Boolean deliveryByMail;
    private final DeliveryAddressDTO deliveryAddress;
    private final String decisionLanguage;

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

    public LawSectionTenPermitApplicationSpeciesAmountDTO getSpeciesAmount() {
        return speciesAmount;
    }

    public LawSectionTenPermitApplicationSpeciesPeriodDTO getSpeciesPeriod() {
        return speciesPeriod;
    }

    public DerogationPermitApplicationSpeciesPopulationDTO getPopulation() {
        return population;
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
        private int huntingYear;
        private HarvestPermitCategory harvestPermitCategory;
        private LocalDateTime submitDate;
        private PersonContactInfoDTO contactPerson;
        private PermitHolderDTO permitHolder;
        private LawSectionTenPermitApplicationSpeciesAmountDTO speciesAmount;
        private LawSectionTenPermitApplicationSpeciesPeriodDTO speciesPeriod;
        private DerogationPermitApplicationSpeciesPopulationDTO population;
        private List<DerogationPermitApplicationAttachmentDTO> areaAttachments;
        private List<DerogationPermitApplicationAttachmentDTO> otherAttachments;
        private DerogationPermitApplicationAreaDTO derogationPermitApplicationAreaDTO;
        private String email1;
        private String email2;
        private Boolean deliveryByMail;
        private DeliveryAddressDTO deliveryAddress;
        private String decisionLanguage;

        private Builder() {
        }

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withStatus(HarvestPermitApplication.Status status) {
            this.status = status;
            return this;
        }

        public Builder withApplicationNumber(Integer applicationNumber) {
            this.applicationNumber = applicationNumber;
            return this;
        }

        public Builder withApplicationName(String applicationName) {
            this.applicationName = applicationName;
            return this;
        }

        public Builder withHuntingYear(int huntingYear) {
            this.huntingYear = huntingYear;
            return this;
        }

        public Builder withCategory(HarvestPermitCategory harvestPermitCategory) {
            this.harvestPermitCategory = harvestPermitCategory;
            return this;
        }

        public Builder withSubmitDate(LocalDateTime submitDate) {
            this.submitDate = submitDate;
            return this;
        }

        public Builder withContactPerson(PersonContactInfoDTO contactPerson) {
            this.contactPerson = contactPerson;
            return this;
        }

        public Builder withPermitHolder(PermitHolderDTO permitHolder) {
            this.permitHolder = permitHolder;
            return this;
        }

        public Builder withSpeciesAmount(LawSectionTenPermitApplicationSpeciesAmountDTO speciesAmount) {
            this.speciesAmount = speciesAmount;
            return this;
        }

        public Builder withSpeciesPeriod(LawSectionTenPermitApplicationSpeciesPeriodDTO speciesPeriod) {
            this.speciesPeriod = speciesPeriod;
            return this;
        }

        public Builder withPopulation(DerogationPermitApplicationSpeciesPopulationDTO population) {
            this.population = population;
            return this;
        }

        public Builder withAreaAttachments(List<DerogationPermitApplicationAttachmentDTO> areaAttachments) {
            this.areaAttachments = areaAttachments;
            return this;
        }

        public Builder withOtherAttachments(List<DerogationPermitApplicationAttachmentDTO> otherAttachments) {
            this.otherAttachments = otherAttachments;
            return this;
        }

        public Builder withArea(DerogationPermitApplicationAreaDTO derogationPermitApplicationAreaDTO) {
            this.derogationPermitApplicationAreaDTO = derogationPermitApplicationAreaDTO;
            return this;
        }

        public Builder withEmail1(String email1) {
            this.email1 = email1;
            return this;
        }

        public Builder withEmail2(String email2) {
            this.email2 = email2;
            return this;
        }

        public Builder withDeliveryByMail(Boolean deliveryByMail) {
            this.deliveryByMail = deliveryByMail;
            return this;
        }

        public Builder withDeliveryAddress(DeliveryAddressDTO deliveryAddress) {
            this.deliveryAddress = deliveryAddress;
            return this;
        }

        public Builder withDecisionLanguage(String language) {
            this.decisionLanguage = language;
            return this;
        }

        public LawSectionTenPermitApplicationSummaryDTO build() {
            return new LawSectionTenPermitApplicationSummaryDTO(this);
        }
    }
}
