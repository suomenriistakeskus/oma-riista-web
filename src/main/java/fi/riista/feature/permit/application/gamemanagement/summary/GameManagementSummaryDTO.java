package fi.riista.feature.permit.application.gamemanagement.summary;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.person.PersonContactInfoDTO;
import fi.riista.feature.permit.application.DeliveryAddressDTO;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.PermitHolderDTO;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.derogation.area.DerogationPermitApplicationAreaDTO;
import fi.riista.feature.permit.application.derogation.attachments.DerogationPermitApplicationAttachmentDTO;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethodsDTO;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethodsSpeciesDTO;
import fi.riista.feature.permit.application.derogation.reasons.DerogationPermitApplicationReasonsDTO;
import fi.riista.feature.permit.application.gamemanagement.GameManagementPermitApplication;
import fi.riista.feature.permit.application.gamemanagement.amount.GameManagementSpeciesAmountDTO;
import fi.riista.feature.permit.application.gamemanagement.period.GameManagementSpeciesPeriodDTO;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.LocalDateTime;

import javax.annotation.Nonnull;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class GameManagementSummaryDTO {

    private GameManagementSummaryDTO(final @Nonnull Builder builder) {
        requireNonNull(builder);

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
        this.areaAttachments = builder.areaAttachments;
        this.otherAttachments = builder.otherAttachments;
        this.forbiddenMethods = builder.forbiddenMethods;
        this.derogationPermitApplicationAreaDTO = builder.derogationPermitApplicationAreaDTO;
        this.email1 = builder.email1;
        this.email2 = builder.email2;
        this.deliveryByMail = builder.deliveryByMail;
        this.deliveryAddress = builder.deliveryAddress;
        this.decisionLanguage = builder.decisionLanguage;
        this.justification = builder.justification;
    }

    public static GameManagementSummaryDTO create(final @Nonnull HarvestPermitApplication entity,
                                                  final @Nonnull GameManagementPermitApplication gameManagementPermitApplication,
                                                  final @Nonnull List<HarvestPermitApplicationSpeciesAmount> speciesAmounts) {
        requireNonNull(entity);
        requireNonNull(gameManagementPermitApplication);
        requireNonNull(speciesAmounts);

        return new Builder()
                .withId(entity.getId())
                .withStatus(entity.getStatus())
                .withApplicationNumber(entity.getApplicationNumber())
                .withApplicationName(entity.getHarvestPermitCategory().getApplicationName().getTranslation(entity.getLocale()))
                .withHuntingYear(entity.getApplicationYear())
                .withCategory(entity.getHarvestPermitCategory())
                .withSubmitDate(DateUtil.toLocalDateTimeNullSafe(entity.getSubmitDate()))
                .withContactPerson(F.mapNullable(entity.getContactPerson(), PersonContactInfoDTO::create))
                .withPermitHolder(PermitHolderDTO.createFrom(entity.getPermitHolder()))
                .withSpeciesAmounts(F.mapNonNullsToList(
                        speciesAmounts,
                        spa -> {
                            final GameManagementSpeciesAmountDTO gameManagementSpeciesAmountDTO = new GameManagementSpeciesAmountDTO();
                            gameManagementSpeciesAmountDTO.setGameSpeciesCode(spa.getGameSpecies().getOfficialCode());
                            gameManagementSpeciesAmountDTO.setSubSpeciesName(spa.getSubSpeciesName());
                            gameManagementSpeciesAmountDTO.setSpecimenAmount(F.mapNullable(spa.getSpecimenAmount(), Float::intValue));
                            gameManagementSpeciesAmountDTO.setEggAmount(spa.getEggAmount());

                            return gameManagementSpeciesAmountDTO;
                        }))
                .withSpeciesPeriods(F.mapNonNullsToList(speciesAmounts, GameManagementSpeciesPeriodDTO::new))
                .withAreaAttachments(entity.getAttachments().stream()
                        .filter(a -> a.getAttachmentType() == HarvestPermitApplicationAttachment.Type.PROTECTED_AREA)
                        .map(DerogationPermitApplicationAttachmentDTO::new)
                        .collect(Collectors.toList()))
                .withOtherAttachments(entity.getAttachments().stream()
                        .filter(a -> a.getAttachmentType() == HarvestPermitApplicationAttachment.Type.OTHER)
                        .map(DerogationPermitApplicationAttachmentDTO::new)
                        .collect(Collectors.toList()))
                .withForbiddenMethods(F.mapNullable(
                        gameManagementPermitApplication.getForbiddenMethods(),
                        forbiddenMethod -> DerogationPermitApplicationForbiddenMethodsDTO.createFrom(
                                forbiddenMethod,
                                F.mapNonNullsToList(speciesAmounts, DerogationPermitApplicationForbiddenMethodsSpeciesDTO::new))))
                .withArea(DerogationPermitApplicationAreaDTO.createFrom(gameManagementPermitApplication))
                .withEmail1(entity.getEmail1())
                .withEmail2(entity.getEmail2())
                .withDeliveryByMail(entity.getDeliveryByMail())
                .withDeliveryAddress(DeliveryAddressDTO.fromNullable(entity.getDeliveryAddress()))
                .withDecisionLanguage(entity.getDecisionLocale().getLanguage())
                .withJustification(gameManagementPermitApplication.getJustification())
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
    private final List<GameManagementSpeciesAmountDTO> speciesAmounts;
    private final List<GameManagementSpeciesPeriodDTO> speciesPeriods;
    private final List<DerogationPermitApplicationAttachmentDTO> areaAttachments;
    private final List<DerogationPermitApplicationAttachmentDTO> otherAttachments;
    private final DerogationPermitApplicationForbiddenMethodsDTO forbiddenMethods;
    private final DerogationPermitApplicationAreaDTO derogationPermitApplicationAreaDTO;
    private final String email1;
    private final String email2;
    private final Boolean deliveryByMail;
    private final DeliveryAddressDTO deliveryAddress;
    private final String decisionLanguage;
    private final String justification;

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

    public List<GameManagementSpeciesAmountDTO> getSpeciesAmounts() {
        return speciesAmounts;
    }

    public List<GameManagementSpeciesPeriodDTO> getSpeciesPeriods() {
        return speciesPeriods;
    }

    public List<DerogationPermitApplicationAttachmentDTO> getAreaAttachments() {
        return areaAttachments;
    }

    public List<DerogationPermitApplicationAttachmentDTO> getOtherAttachments() {
        return otherAttachments;
    }

    public DerogationPermitApplicationForbiddenMethodsDTO getForbiddenMethods() {
        return forbiddenMethods;
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

    public String getJustification() {
        return justification;
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
        private List<GameManagementSpeciesAmountDTO> speciesAmounts;
        private List<GameManagementSpeciesPeriodDTO> speciesPeriods;
        private List<DerogationPermitApplicationAttachmentDTO> areaAttachments;
        private List<DerogationPermitApplicationAttachmentDTO> otherAttachments;
        private DerogationPermitApplicationAreaDTO derogationPermitApplicationAreaDTO;
        private DerogationPermitApplicationForbiddenMethodsDTO forbiddenMethods;
        private String email1;
        private String email2;
        private Boolean deliveryByMail;
        private DeliveryAddressDTO deliveryAddress;
        private String decisionLanguage;
        private String justification;

        private Builder() {
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

        public Builder withHuntingYear(final int huntingYear) {
            this.huntingYear = huntingYear;
            return this;
        }

        public Builder withCategory(final HarvestPermitCategory harvestPermitCategory) {
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

        public Builder withSpeciesAmounts(final List<GameManagementSpeciesAmountDTO> speciesAmounts) {
            this.speciesAmounts = speciesAmounts;
            return this;
        }

        public Builder withSpeciesPeriods(final List<GameManagementSpeciesPeriodDTO> speciesPeriods) {
            this.speciesPeriods = speciesPeriods;
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

        public Builder withArea(final DerogationPermitApplicationAreaDTO derogationPermitApplicationAreaDTO) {
            this.derogationPermitApplicationAreaDTO = derogationPermitApplicationAreaDTO;
            return this;
        }

        public Builder withForbiddenMethods(final DerogationPermitApplicationForbiddenMethodsDTO forbiddenMethods) {
            this.forbiddenMethods = forbiddenMethods;
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

        public Builder withDecisionLanguage(final String language) {
            this.decisionLanguage = language;
            return this;
        }

        public Builder withJustification(final String justification) {
            this.justification = justification;
            return this;
        }

        public GameManagementSummaryDTO build() {
            return new GameManagementSummaryDTO(this);
        }
    }

}
