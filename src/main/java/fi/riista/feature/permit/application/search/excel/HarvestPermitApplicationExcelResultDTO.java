package fi.riista.feature.permit.application.search.excel;

import fi.riista.feature.common.decision.AppealStatus;
import fi.riista.feature.common.decision.DecisionStatus;
import fi.riista.feature.common.decision.GrantStatus;
import fi.riista.feature.gamediary.GameSpeciesDTO;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmountDTO;
import fi.riista.feature.permit.application.PermitHolderDTO;
import fi.riista.feature.permit.application.bird.ProtectedAreaType;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType;
import fi.riista.feature.permit.decision.methods.ForbiddenMethodType;
import fi.riista.util.LocalisedString;
import org.joda.time.LocalDateTime;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class HarvestPermitApplicationExcelResultDTO {

    public static Builder builder() {
        return new Builder();
    }

    private final int applicationYear;
    private final HarvestPermitCategory harvestPermitCategory;
    private final String permitTypeCode;
    private final Integer applicationNumber;
    private final LocalisedString rkaName;
    private final LocalisedString rhyName;
    private final LocalDateTime submitDate;
    private final String contactPerson;
    private final PermitHolderDTO permitHolder;
    private final String handler;
    private final HarvestPermitApplication.Status status;
    private final List<GameSpeciesDTO> gameSpecies;
    private final DecisionStatus decisionStatus;
    private final LocalDateTime decisionPublishDate;
    private final PermitDecision.DecisionType decisionType;
    private final GrantStatus grantStatus;
    private final AppealStatus appealStatus;
    private final Set<ProtectedAreaType> protectedAreaTypes;
    private final Set<ForbiddenMethodType> forbiddenMethodTypes;
    private final Set<PermitDecisionDerogationReasonType> decisionDerogationReasonTypes;
    private final OrganisationNameDTO rhy;
    private final Map<Integer, HarvestPermitApplicationSpeciesAmountDTO> appliedSpeciesAmountsBySpecies;
    private final Map<Integer, Map<Integer, ApplicationSearchDecisionSpeciesAmountDTO>> decisionSpeciesAmountsBySpecies;

    public int getApplicationYear() {
        return applicationYear;
    }

    public HarvestPermitCategory getHarvestPermitCategory() {
        return harvestPermitCategory;
    }

    public String getPermitTypeCode() {
        return permitTypeCode;
    }

    public Integer getApplicationNumber() {
        return applicationNumber;
    }

    public LocalisedString getRkaName() {
        return rkaName;
    }

    public LocalisedString getRhyName() {
        return rhyName;
    }

    public LocalDateTime getSubmitDate() {
        return submitDate;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public PermitHolderDTO getPermitHolder() {
        return permitHolder;
    }

    public String getHandler() {
        return handler;
    }

    public HarvestPermitApplication.Status getStatus() {
        return status;
    }

    public List<GameSpeciesDTO> getGameSpecies() {
        return gameSpecies;
    }

    public DecisionStatus getDecisionStatus() {
        return decisionStatus;
    }

    public LocalDateTime getDecisionPublishDate() {
        return decisionPublishDate;
    }

    public PermitDecision.DecisionType getDecisionType() {
        return decisionType;
    }

    public GrantStatus getGrantStatus() {
        return grantStatus;
    }

    public AppealStatus getAppealStatus() {
        return appealStatus;
    }

    public Set<ProtectedAreaType> getProtectedAreaTypes() {
        return protectedAreaTypes;
    }

    public Set<ForbiddenMethodType> getForbiddenMethodTypes() {
        return forbiddenMethodTypes;
    }

    public Set<PermitDecisionDerogationReasonType> getDecisionDerogationReasonTypes() {
        return decisionDerogationReasonTypes;
    }

    public Map<Integer, HarvestPermitApplicationSpeciesAmountDTO> getAppliedSpeciesAmountsBySpecies() {
        return appliedSpeciesAmountsBySpecies;
    }

    public Map<Integer, Map<Integer, ApplicationSearchDecisionSpeciesAmountDTO>> getDecisionSpeciesAmountsBySpecies() {
        return decisionSpeciesAmountsBySpecies;
    }

    public OrganisationNameDTO getRhy() {
        return rhy;
    }

    private HarvestPermitApplicationExcelResultDTO(final int applicationYear,
                                                   final HarvestPermitCategory harvestPermitCategory,
                                                   final String permitTypeCode,
                                                   final Integer applicationNumber, final LocalisedString rkaName,
                                                   final LocalisedString rhyName, final LocalDateTime submitDate,
                                                   final String contactPerson, final PermitHolderDTO permitHolder,
                                                   final String handler, final HarvestPermitApplication.Status status,
                                                   final List<GameSpeciesDTO> gameSpecies,
                                                   final DecisionStatus decisionStatus,
                                                   final LocalDateTime decisionPublishDate,
                                                   final PermitDecision.DecisionType decisionType,
                                                   final GrantStatus grantStatus,
                                                   final AppealStatus appealStatus,
                                                   final Set<ProtectedAreaType> protectedAreaTypes,
                                                   final Set<ForbiddenMethodType> forbiddenMethodTypes,
                                                   final Set<PermitDecisionDerogationReasonType> decisionDerogationReasonTypes,
                                                   final OrganisationNameDTO rhy,
                                                   final Map<Integer, HarvestPermitApplicationSpeciesAmountDTO> appliedSpeciesAmountsBySpecies,
                                                   final Map<Integer, Map<Integer, ApplicationSearchDecisionSpeciesAmountDTO>> decisionSpeciesAmountsBySpecies) {
        this.applicationYear = applicationYear;
        this.harvestPermitCategory = harvestPermitCategory;
        this.permitTypeCode = permitTypeCode;
        this.applicationNumber = applicationNumber;
        this.rkaName = rkaName;
        this.rhyName = rhyName;
        this.submitDate = submitDate;
        this.contactPerson = contactPerson;
        this.permitHolder = permitHolder;
        this.handler = handler;
        this.status = status;
        this.gameSpecies = gameSpecies;
        this.decisionStatus = decisionStatus;
        this.decisionPublishDate = decisionPublishDate;
        this.decisionType = decisionType;
        this.grantStatus = grantStatus;
        this.appealStatus = appealStatus;
        this.protectedAreaTypes = protectedAreaTypes;
        this.forbiddenMethodTypes = forbiddenMethodTypes;
        this.decisionDerogationReasonTypes = decisionDerogationReasonTypes;
        this.rhy = rhy;
        this.appliedSpeciesAmountsBySpecies = appliedSpeciesAmountsBySpecies;
        this.decisionSpeciesAmountsBySpecies = decisionSpeciesAmountsBySpecies;
    }

    public static final class Builder {
        private int applicationYear;
        private HarvestPermitCategory harvestPermitCategory;
        private String permitTypeCode;
        private Integer applicationNumber;
        private LocalisedString rkaName;
        private LocalisedString rhyName;
        private LocalDateTime submitDate;
        private String contactPerson;
        private PermitHolderDTO permitHolder;
        private String handler;
        private HarvestPermitApplication.Status status;
        private List<GameSpeciesDTO> gameSpecies;
        private DecisionStatus decisionStatus;
        private LocalDateTime decisionPublishDate;
        private PermitDecision.DecisionType decisionType;
        private GrantStatus grantStatus;
        private AppealStatus appealStatus;
        private Set<ProtectedAreaType> protectedAreaTypes;
        private Set<ForbiddenMethodType> forbiddenMethodTypes;
        private Set<PermitDecisionDerogationReasonType> decisionDerogationReasonTypes;
        private OrganisationNameDTO rhy;
        private Map<Integer, HarvestPermitApplicationSpeciesAmountDTO> appliedSpeciesAmountsBySpecies;
        private Map<Integer, Map<Integer, ApplicationSearchDecisionSpeciesAmountDTO>> decisionSpeciesAmountsBySpecies;

        private Builder() {
        }

        public Builder withApplicationYear(final int applicationYear) {
            this.applicationYear = applicationYear;
            return this;
        }

        public Builder withHarvestPermitCategory(final HarvestPermitCategory harvestPermitCategory) {
            this.harvestPermitCategory = harvestPermitCategory;
            return this;
        }

        public Builder withPermitTypeCode(final String permitTypeCode) {
            this.permitTypeCode = permitTypeCode;
            return this;
        }

        public Builder withApplicationNumber(final Integer applicationNumber) {
            this.applicationNumber = applicationNumber;
            return this;
        }

        public Builder withRkaName(LocalisedString rkaName) {
            this.rkaName = rkaName;
            return this;
        }

        public Builder withRhyName(final LocalisedString rhyName) {
            this.rhyName = rhyName;
            return this;
        }

        public Builder withSubmitDate(final LocalDateTime submitDate) {
            this.submitDate = submitDate;
            return this;
        }

        public Builder withContactPerson(final String contactPerson) {
            this.contactPerson = contactPerson;
            return this;
        }

        public Builder withPermitHolder(final PermitHolderDTO permitHolder) {
            this.permitHolder = permitHolder;
            return this;
        }

        public Builder withHandler(final String handler) {
            this.handler = handler;
            return this;
        }

        public Builder withStatus(final HarvestPermitApplication.Status status) {
            this.status = status;
            return this;
        }

        public Builder withGameSpecies(final List<GameSpeciesDTO> gameSpecies) {
            this.gameSpecies = gameSpecies;
            return this;
        }

        public Builder withDecisionStatus(final DecisionStatus decisionStatus) {
            this.decisionStatus = decisionStatus;
            return this;
        }

        public Builder withDecisionPublishDate(final LocalDateTime decisionPublishDate) {
            this.decisionPublishDate = decisionPublishDate;
            return this;
        }

        public Builder withDecisionType(final PermitDecision.DecisionType decisionType) {
            this.decisionType = decisionType;
            return this;
        }

        public Builder withGrantStatus(final GrantStatus grantStatus) {
            this.grantStatus = grantStatus;
            return this;
        }

        public Builder withAppealStatus(final AppealStatus appealStatus) {
            this.appealStatus = appealStatus;
            return this;
        }

        public Builder withProtectedAreaTypes(final Set<ProtectedAreaType> protectedAreaTypes) {
            this.protectedAreaTypes = protectedAreaTypes;
            return this;
        }

        public Builder withForbiddenMethodTypes(final Set<ForbiddenMethodType> forbiddenMethodTypes) {
            this.forbiddenMethodTypes = forbiddenMethodTypes;
            return this;
        }

        public Builder withDecisionDerogationReasonTypes(final Set<PermitDecisionDerogationReasonType> decisionDerogationReasonTypes) {
            this.decisionDerogationReasonTypes = decisionDerogationReasonTypes;
            return this;
        }

        public Builder withRhy(final OrganisationNameDTO rhy) {
            this.rhy = rhy;
            return this;
        }

        public Builder withAppliedSpeciesAmountsBySpecies(final Map<Integer, HarvestPermitApplicationSpeciesAmountDTO> appliedSpeciesAmountsBySpecies) {
            this.appliedSpeciesAmountsBySpecies = appliedSpeciesAmountsBySpecies;
            return this;
        }

        public Builder withPermitSpeciesAmountsBySpecies(final Map<Integer, Map<Integer, ApplicationSearchDecisionSpeciesAmountDTO>> decisionSpeciesAmountsBySpecies) {
            this.decisionSpeciesAmountsBySpecies = decisionSpeciesAmountsBySpecies;
            return this;
        }

        public HarvestPermitApplicationExcelResultDTO build() {
            return new HarvestPermitApplicationExcelResultDTO(applicationYear, harvestPermitCategory, permitTypeCode,
                    applicationNumber, rkaName, rhyName, submitDate, contactPerson, permitHolder, handler, status,
                    gameSpecies, decisionStatus, decisionPublishDate, decisionType, grantStatus, appealStatus,
                    protectedAreaTypes, forbiddenMethodTypes, decisionDerogationReasonTypes, rhy, appliedSpeciesAmountsBySpecies,
                    decisionSpeciesAmountsBySpecies);
        }
    }
}
