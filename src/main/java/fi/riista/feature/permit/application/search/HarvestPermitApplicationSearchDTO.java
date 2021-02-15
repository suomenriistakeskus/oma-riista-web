package fi.riista.feature.permit.application.search;

import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.permit.application.bird.ProtectedAreaType;
import fi.riista.feature.common.decision.AppealStatus;
import fi.riista.feature.common.decision.GrantStatus;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.derogation.PermitDecisionDerogationReasonType;
import fi.riista.feature.permit.decision.methods.ForbiddenMethodType;
import org.springframework.data.domain.PageRequest;

import javax.annotation.Nullable;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.util.Set;

public class HarvestPermitApplicationSearchDTO {
    public enum StatusSearch {
        // Hakemus jätetty
        ACTIVE,
        // Hakemus käsittelyssä
        DRAFT,
        // Hakemusta täydennetään
        AMENDING,
        // Lukittu
        LOCKED,
        // Julkaistu
        PUBLISHED
    }

    private Integer page;

    private Integer size;

    @Pattern(regexp = "^\\d{0,3}$")
    private String rhyOfficialCode;

    @Pattern(regexp = "^\\d{0,3}$")
    private String rkaOfficialCode;

    @Min(2017)
    private Integer huntingYear;

    // Zero for annually renewed permits
    @Min(0)
    @Max(5)
    private Integer validityYears;

    private HarvestPermitCategory harvestPermitCategory;

    private Integer gameSpeciesCode;

    private Long handlerId;

    private Set<StatusSearch> status;

    private Set<PermitDecision.DecisionType> decisionType;

    private Set<AppealStatus> appealStatus;

    private Set<GrantStatus> grantStatus;

    private Set<PermitDecisionDerogationReasonType> derogationReason;

    private Set<ProtectedAreaType> protectedArea;

    private Set<ForbiddenMethodType> forbiddenMethod;

    private Integer applicationNumber;

    @Nullable
    public PageRequest asPageRequest() {
        return page != null && size != null
                ? PageRequest.of(page, size)
                : null;
    }

    @AssertTrue
    public boolean isPageInfoValidWhenPresent() {
        return (page == null && size == null) || isPageInfoPresent();
    }

    public boolean isPageInfoPresent() {
        return page != null && size != null;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(final Integer page) {
        this.page = page;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(final Integer size) {
        this.size = size;
    }

    public String getRhyOfficialCode() {
        return rhyOfficialCode;
    }

    public void setRhyOfficialCode(final String rhyOfficialCode) {
        this.rhyOfficialCode = rhyOfficialCode;
    }

    public String getRkaOfficialCode() {
        return rkaOfficialCode;
    }

    public void setRkaOfficialCode(final String rkaOfficialCode) {
        this.rkaOfficialCode = rkaOfficialCode;
    }

    public Integer getHuntingYear() {
        return huntingYear;
    }

    public void setHuntingYear(final Integer huntingYear) {
        this.huntingYear = huntingYear;
    }

    public Integer getValidityYears() {
        return validityYears;
    }

    public void setValidityYears(final Integer validityYears) {
        this.validityYears = validityYears;
    }

    public HarvestPermitCategory getHarvestPermitCategory() {
        return harvestPermitCategory;
    }

    public void setHarvestPermitCategory(final HarvestPermitCategory harvestPermitCategory) {
        this.harvestPermitCategory = harvestPermitCategory;
    }

    public Integer getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(final Integer gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public Long getHandlerId() {
        return handlerId;
    }

    public void setHandlerId(final Long handlerId) {
        this.handlerId = handlerId;
    }

    public Set<StatusSearch> getStatus() {
        return status;
    }

    public void setStatus(final Set<StatusSearch> status) {
        this.status = status;
    }

    public Set<PermitDecision.DecisionType> getDecisionType() {
        return decisionType;
    }

    public void setDecisionType(final Set<PermitDecision.DecisionType> decisionType) {
        this.decisionType = decisionType;
    }

    public Set<AppealStatus> getAppealStatus() {
        return appealStatus;
    }

    public void setAppealStatus(final Set<AppealStatus> appealStatus) {
        this.appealStatus = appealStatus;
    }

    public Set<GrantStatus> getGrantStatus() {
        return grantStatus;
    }

    public void setGrantStatus(final Set<GrantStatus> grantStatus) {
        this.grantStatus = grantStatus;
    }

    public Set<PermitDecisionDerogationReasonType> getDerogationReason() {
        return derogationReason;
    }

    public void setDerogationReason(final Set<PermitDecisionDerogationReasonType> derogationReason) {
        this.derogationReason = derogationReason;
    }

    public Set<ProtectedAreaType> getProtectedArea() {
        return protectedArea;
    }

    public void setProtectedArea(final Set<ProtectedAreaType> protectedArea) {
        this.protectedArea = protectedArea;
    }

    public Set<ForbiddenMethodType> getForbiddenMethod() {
        return forbiddenMethod;
    }

    public void setForbiddenMethod(final Set<ForbiddenMethodType> forbiddenMethod) {
        this.forbiddenMethod = forbiddenMethod;
    }

    public Integer getApplicationNumber() {
        return applicationNumber;
    }

    public void setApplicationNumber(final Integer applicationNumber) {
        this.applicationNumber = applicationNumber;
    }
}
