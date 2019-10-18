package fi.riista.feature.permit.application.search.excel;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.PermitHolderDTO;
import fi.riista.feature.permit.application.bird.ProtectedAreaType;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType;
import fi.riista.feature.permit.decision.methods.ForbiddenMethodType;
import fi.riista.util.LocalisedString;
import org.joda.time.LocalDateTime;

import java.util.Set;

public class HarvestPermitApplicationExcelResultDTO {

    public static Builder builder() {
        return new Builder();
    }

    private final int applicationYear;
    private final HarvestPermitCategory harvestPermitCategory;
    private final Integer applicationNumber;
    private final LocalisedString rkaName;
    private final LocalisedString rhyName;
    private final LocalDateTime submitDate;
    private final String contactPerson;
    private final PermitHolderDTO permitHolder;
    private final String handler;
    private final HarvestPermitApplication.Status status;
    private final Set<LocalisedString> gameSpeciesNames;
    private final PermitDecision.Status decisionStatus;
    private final PermitDecision.DecisionType decisionType;
    private final PermitDecision.GrantStatus grantStatus;
    private final PermitDecision.AppealStatus appealStatus;
    private final Set<ProtectedAreaType> protectedAreaTypes;
    private final Set<ForbiddenMethodType> forbiddenMethodTypes;
    private final Set<PermitDecisionDerogationReasonType> decisionDerogationReasonTypes;
    private final OrganisationNameDTO rhy;

    public int getApplicationYear() {
        return applicationYear;
    }

    public HarvestPermitCategory getHarvestPermitCategory() {
        return harvestPermitCategory;
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

    public Set<LocalisedString> getGameSpeciesNames() {
        return gameSpeciesNames;
    }

    public PermitDecision.Status getDecisionStatus() {
        return decisionStatus;
    }

    public PermitDecision.DecisionType getDecisionType() {
        return decisionType;
    }

    public PermitDecision.GrantStatus getGrantStatus() {
        return grantStatus;
    }

    public PermitDecision.AppealStatus getAppealStatus() {
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

    public OrganisationNameDTO getRhy() {
        return rhy;
    }

    private HarvestPermitApplicationExcelResultDTO(final int applicationYear,
                                                   final HarvestPermitCategory harvestPermitCategory,
                                                   final Integer applicationNumber, final LocalisedString rkaName,
                                                   final LocalisedString rhyName, final LocalDateTime submitDate,
                                                   final String contactPerson, final PermitHolderDTO permitHolder,
                                                   final String handler, final HarvestPermitApplication.Status status,
                                                   final Set<LocalisedString> gameSpeciesNames,
                                                   final PermitDecision.Status decisionStatus,
                                                   final PermitDecision.DecisionType decisionType,
                                                   final PermitDecision.GrantStatus grantStatus,
                                                   final PermitDecision.AppealStatus appealStatus,
                                                   final Set<ProtectedAreaType> protectedAreaTypes,
                                                   final Set<ForbiddenMethodType> forbiddenMethodTypes,
                                                   final Set<PermitDecisionDerogationReasonType> decisionDerogationReasonTypes,
                                                   final OrganisationNameDTO rhy) {
        this.applicationYear = applicationYear;
        this.harvestPermitCategory = harvestPermitCategory;
        this.applicationNumber = applicationNumber;
        this.rkaName = rkaName;
        this.rhyName = rhyName;
        this.submitDate = submitDate;
        this.contactPerson = contactPerson;
        this.permitHolder = permitHolder;
        this.handler = handler;
        this.status = status;
        this.gameSpeciesNames = gameSpeciesNames;
        this.decisionStatus = decisionStatus;
        this.decisionType = decisionType;
        this.grantStatus = grantStatus;
        this.appealStatus = appealStatus;
        this.protectedAreaTypes = protectedAreaTypes;
        this.forbiddenMethodTypes = forbiddenMethodTypes;
        this.decisionDerogationReasonTypes = decisionDerogationReasonTypes;
        this.rhy = rhy;
    }

    public static final class Builder {
        private int applicationYear;
        private HarvestPermitCategory harvestPermitCategory;
        private Integer applicationNumber;
        private LocalisedString rkaName;
        private LocalisedString rhyName;
        private LocalDateTime submitDate;
        private String contactPerson;
        private PermitHolderDTO permitHolder;
        private String handler;
        private HarvestPermitApplication.Status status;
        private Set<LocalisedString> gameSpeciesNames;
        private PermitDecision.Status decisionStatus;
        private PermitDecision.DecisionType decisionType;
        private PermitDecision.GrantStatus grantStatus;
        private PermitDecision.AppealStatus appealStatus;
        private Set<ProtectedAreaType> protectedAreaTypes;
        private Set<ForbiddenMethodType> forbiddenMethodTypes;
        private Set<PermitDecisionDerogationReasonType> decisionDerogationReasonTypes;
        private OrganisationNameDTO rhy;

        private Builder() {
        }

        public Builder withApplicationYear(int applicationYear) {
            this.applicationYear = applicationYear;
            return this;
        }

        public Builder withHarvestPermitCategory(HarvestPermitCategory harvestPermitCategory) {
            this.harvestPermitCategory = harvestPermitCategory;
            return this;
        }

        public Builder withApplicationNumber(Integer applicationNumber) {
            this.applicationNumber = applicationNumber;
            return this;
        }

        public Builder withRkaName(LocalisedString rkaName) {
            this.rkaName = rkaName;
            return this;
        }

        public Builder withRhyName(LocalisedString rhyName) {
            this.rhyName = rhyName;
            return this;
        }

        public Builder withSubmitDate(LocalDateTime submitDate) {
            this.submitDate = submitDate;
            return this;
        }

        public Builder withContactPerson(String contactPerson) {
            this.contactPerson = contactPerson;
            return this;
        }

        public Builder withPermitHolder(PermitHolderDTO permitHolder) {
            this.permitHolder = permitHolder;
            return this;
        }

        public Builder withHandler(String handler) {
            this.handler = handler;
            return this;
        }

        public Builder withStatus(HarvestPermitApplication.Status status) {
            this.status = status;
            return this;
        }

        public Builder withGameSpeciesNames(Set<LocalisedString> gameSpeciesNames) {
            this.gameSpeciesNames = gameSpeciesNames;
            return this;
        }

        public Builder withDecisionStatus(PermitDecision.Status decisionStatus) {
            this.decisionStatus = decisionStatus;
            return this;
        }

        public Builder withDecisionType(PermitDecision.DecisionType decisionType) {
            this.decisionType = decisionType;
            return this;
        }

        public Builder withGrantStatus(PermitDecision.GrantStatus grantStatus) {
            this.grantStatus = grantStatus;
            return this;
        }

        public Builder withAppealStatus(PermitDecision.AppealStatus appealStatus) {
            this.appealStatus = appealStatus;
            return this;
        }

        public Builder withProtectedAreaTypes(Set<ProtectedAreaType> protectedAreaTypes) {
            this.protectedAreaTypes = protectedAreaTypes;
            return this;
        }

        public Builder withForbiddenMethodTypes(Set<ForbiddenMethodType> forbiddenMethodTypes) {
            this.forbiddenMethodTypes = forbiddenMethodTypes;
            return this;
        }

        public Builder withDecisionDerogationReasonTypes(Set<PermitDecisionDerogationReasonType> decisionDerogationReasonTypes) {
            this.decisionDerogationReasonTypes = decisionDerogationReasonTypes;
            return this;
        }

        public Builder withRhy(OrganisationNameDTO rhy) {
            this.rhy = rhy;
            return this;
        }

        public HarvestPermitApplicationExcelResultDTO build() {
            return new HarvestPermitApplicationExcelResultDTO(applicationYear, harvestPermitCategory,
                    applicationNumber, rkaName, rhyName, submitDate, contactPerson, permitHolder, handler, status,
                    gameSpeciesNames, decisionStatus, decisionType, grantStatus, appealStatus, protectedAreaTypes,
                    forbiddenMethodTypes, decisionDerogationReasonTypes, rhy);
        }
    }
}
