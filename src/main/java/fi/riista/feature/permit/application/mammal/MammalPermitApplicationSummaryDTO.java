package fi.riista.feature.permit.application.mammal;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.feature.permit.application.DeliveryAddressDTO;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.PermitHolderDTO;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.derogation.area.DerogationPermitApplicationAreaDTO;
import fi.riista.feature.permit.application.derogation.attachments.DerogationPermitApplicationAttachmentDTO;
import fi.riista.feature.permit.application.derogation.damage.DerogationPermitApplicationDamageDTO;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethodsDTO;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethodsSpeciesDTO;
import fi.riista.feature.permit.application.derogation.population.DerogationPermitApplicationSpeciesPopulationDTO;
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationReasonsDTO;
import fi.riista.feature.permit.application.mammal.amount.MammalPermitApplicationSpeciesAmountDTO;
import fi.riista.feature.permit.application.mammal.period.MammalPermitApplicationSpeciesPeriodDTO;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDateTime;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class MammalPermitApplicationSummaryDTO {

    public MammalPermitApplicationSummaryDTO(final Builder builder) {
        this.id = builder.id;
        this.status = builder.status;
        this.applicationNumber = builder.applicationNumber;
        this.applicationName = builder.applicationName;
        this.huntingYear = builder.huntingYear;
        this.harvestPermitCategory = builder.harvestPermitCategory;
        this.submitDate = builder.submitDate;
        this.contactPerson = builder.contactPerson;
        this.permitHolder = builder.permitHolder;
        this.speciesAmounts = builder.speciesAmounts;
        this.speciesPeriods = builder.speciesPeriods;
        this.damage = builder.damage;
        this.population = builder.population;
        this.areaAttachments = builder.areaAttachments;
        this.otherAttachments = builder.otherAttachments;
        this.reasons = builder.reasons;
        this.forbiddenMethods = builder.forbiddenMethods;
        this.derogationPermitApplicationAreaDTO = builder.derogationPermitApplicationAreaDTO;
        this.email1 = builder.email1;
        this.email2 = builder.email2;
        this.deliveryByMail = builder.deliveryByMail;
        this.deliveryAddress = builder.deliveryAddress;
        this.decisionLanguage = builder.decisionLanguage;
        this.validityYears = builder.validityYears;
        this.extendedPeriodGrounds = builder.extendedPeriodGrounds;
        this.protectedAreaName = builder.protectedAreaName;
        this.extendedPeriodDescription = builder.extendedPeriodDescription;
    }

    public static MammalPermitApplicationSummaryDTO create(final @Nonnull HarvestPermitApplication entity,
                                                           final @Nonnull MammalPermitApplication mammalPermitApplication,
                                                           final DerogationPermitApplicationReasonsDTO reasonsDTO) {
        requireNonNull(entity);
        requireNonNull(mammalPermitApplication);

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
                .withSpeciesAmounts(entity.getSpeciesAmounts().stream()
                        .map(MammalPermitApplicationSpeciesAmountDTO::new)
                        .collect(Collectors.toList()))
                .withSpeciesPeriods(entity.getSpeciesAmounts().stream()
                        .map(MammalPermitApplicationSpeciesPeriodDTO::new)
                        .collect(Collectors.toList()))
                .withDamage(entity.getSpeciesAmounts().stream()
                        .map(DerogationPermitApplicationDamageDTO::new)
                        .collect(Collectors.toList()))
                .withPopulation(entity.getSpeciesAmounts().stream()
                        .map(DerogationPermitApplicationSpeciesPopulationDTO::new)
                        .collect(Collectors.toList()))
                .withAreaAttachments(entity.getAttachments().stream()
                        .filter(a -> a.getAttachmentType() == HarvestPermitApplicationAttachment.Type.PROTECTED_AREA)
                        .map(DerogationPermitApplicationAttachmentDTO::new)
                        .collect(Collectors.toList()))
                .withOtherAttachments(entity.getAttachments().stream()
                        .filter(a -> a.getAttachmentType() == HarvestPermitApplicationAttachment.Type.OTHER)
                        .map(DerogationPermitApplicationAttachmentDTO::new)
                        .collect(Collectors.toList()))
                .withReasons(Optional.ofNullable(reasonsDTO)
                        .orElse(null))
                .withDeviationJustification(Optional.ofNullable(mammalPermitApplication.getForbiddenMethods())
                        .map(deviation -> DerogationPermitApplicationForbiddenMethodsDTO.createFrom(deviation,
                                entity.getSpeciesAmounts().stream()
                                        .map(DerogationPermitApplicationForbiddenMethodsSpeciesDTO::new)
                                        .collect(Collectors.toList())))
                        .orElse(null))
                .withArea(DerogationPermitApplicationAreaDTO.createFrom(mammalPermitApplication))
                .withEmail1(Optional.ofNullable(entity.getEmail1()).orElse(null))
                .withEmail2(Optional.ofNullable(entity.getEmail2()).orElse(null))
                .withDeliveryByMail(Optional.ofNullable(entity.getDeliveryByMail()).orElse(null))
                .withDeliveryAddress(DeliveryAddressDTO.fromNullable(entity.getDeliveryAddress()))
                .withDecisionLanguage(entity.getDecisionLocale().getLanguage())
                .withValidityYears(entity.getValidityYears())
                .withExtendedPeriodGrounds(mammalPermitApplication.getExtendedPeriodGrounds())
                .withProtectedAreaName(mammalPermitApplication.getProtectedAreaName())
                .withExtendedPeriodDescription(mammalPermitApplication.getExtendedPeriodGroundsDescription())
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
    private final List<MammalPermitApplicationSpeciesAmountDTO> speciesAmounts;
    private final List<MammalPermitApplicationSpeciesPeriodDTO> speciesPeriods;
    private final List<DerogationPermitApplicationDamageDTO> damage;
    private final List<DerogationPermitApplicationSpeciesPopulationDTO> population;
    private final List<DerogationPermitApplicationAttachmentDTO> areaAttachments;
    private final List<DerogationPermitApplicationAttachmentDTO> otherAttachments;
    private final DerogationPermitApplicationReasonsDTO reasons;
    private final DerogationPermitApplicationForbiddenMethodsDTO forbiddenMethods;
    private final DerogationPermitApplicationAreaDTO derogationPermitApplicationAreaDTO;
    private final String email1;
    private final String email2;
    private final Boolean deliveryByMail;
    private final DeliveryAddressDTO deliveryAddress;
    private final String decisionLanguage;
    private final Integer validityYears;
    private final MammalPermitApplication.ExtendedPeriodGrounds extendedPeriodGrounds;
    private final String protectedAreaName;
    private final String extendedPeriodDescription;

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

    public List<MammalPermitApplicationSpeciesAmountDTO> getSpeciesAmounts() {
        return speciesAmounts;
    }

    public List<MammalPermitApplicationSpeciesPeriodDTO> getSpeciesPeriods() {
        return speciesPeriods;
    }

    public List<DerogationPermitApplicationDamageDTO> getDamage() {
        return damage;
    }

    public List<DerogationPermitApplicationSpeciesPopulationDTO> getPopulation() {
        return population;
    }

    public List<DerogationPermitApplicationAttachmentDTO> getAreaAttachments() {
        return areaAttachments;
    }

    public List<DerogationPermitApplicationAttachmentDTO> getOtherAttachments() {
        return otherAttachments;
    }

    public DerogationPermitApplicationReasonsDTO getReasons() {
        return reasons;
    }


    public DerogationPermitApplicationForbiddenMethodsDTO getForbiddenMethods() {
        return forbiddenMethods;
    }

    public MammalPermitApplication.ExtendedPeriodGrounds getExtendedPeriodGrounds() {
        return extendedPeriodGrounds;
    }

    public String getProtectedAreaName() {
        return protectedAreaName;
    }

    public String getExtendedPeriodDescription() {
        return extendedPeriodDescription;
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

    public Integer getValidityYears() {
        return validityYears;
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
        private List<MammalPermitApplicationSpeciesAmountDTO> speciesAmounts;
        private List<MammalPermitApplicationSpeciesPeriodDTO> speciesPeriods;
        private List<DerogationPermitApplicationDamageDTO> damage;
        private List<DerogationPermitApplicationSpeciesPopulationDTO> population;
        private List<DerogationPermitApplicationAttachmentDTO> areaAttachments;
        private List<DerogationPermitApplicationAttachmentDTO> otherAttachments;
        private DerogationPermitApplicationReasonsDTO reasons;
        private DerogationPermitApplicationAreaDTO derogationPermitApplicationAreaDTO;
        private DerogationPermitApplicationForbiddenMethodsDTO forbiddenMethods;
        private String email1;
        private String email2;
        private Boolean deliveryByMail;
        private DeliveryAddressDTO deliveryAddress;
        private String decisionLanguage;
        private Integer validityYears;
        private MammalPermitApplication.ExtendedPeriodGrounds extendedPeriodGrounds;
        private String protectedAreaName;
        private String extendedPeriodDescription;

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

        public Builder withSpeciesAmounts(List<MammalPermitApplicationSpeciesAmountDTO> speciesAmounts) {
            this.speciesAmounts = speciesAmounts;
            return this;
        }

        public Builder withSpeciesPeriods(List<MammalPermitApplicationSpeciesPeriodDTO> speciesPeriods) {
            this.speciesPeriods = speciesPeriods;
            return this;
        }

        public Builder withDamage(List<DerogationPermitApplicationDamageDTO> damage) {
            this.damage = damage;
            return this;
        }

        public Builder withPopulation(List<DerogationPermitApplicationSpeciesPopulationDTO> population) {
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

        public Builder withReasons(DerogationPermitApplicationReasonsDTO reasons) {
            this.reasons = reasons;
            return this;
        }

        public Builder withArea(DerogationPermitApplicationAreaDTO derogationPermitApplicationAreaDTO) {
            this.derogationPermitApplicationAreaDTO = derogationPermitApplicationAreaDTO;
            return this;
        }

        public Builder withDeviationJustification(DerogationPermitApplicationForbiddenMethodsDTO forbiddenMethods) {
            this.forbiddenMethods = forbiddenMethods;
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

        public Builder withValidityYears(Integer validityYears) {
            this.validityYears = validityYears;
            return this;
        }

        public Builder withExtendedPeriodGrounds(MammalPermitApplication.ExtendedPeriodGrounds grounds) {
            this.extendedPeriodGrounds = grounds;
            return this;
        }

        public Builder withProtectedAreaName(String protectedAreaName) {
            this.protectedAreaName = protectedAreaName;
            return this;
        }

        public Builder withExtendedPeriodDescription(String extendedPeriodDescription) {
            this.extendedPeriodDescription = extendedPeriodDescription;
            return this;
        }

        public MammalPermitApplicationSummaryDTO build() {
            return new MammalPermitApplicationSummaryDTO(this);
        }
    }
}
