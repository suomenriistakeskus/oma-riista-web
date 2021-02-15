package fi.riista.feature.common.decision.nomination;


import fi.riista.feature.common.decision.AppealStatus;
import fi.riista.feature.organization.occupation.OccupationType;
import org.springframework.data.domain.PageRequest;

import javax.annotation.Nullable;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;
import java.util.Set;

public class NominationDecisionSearchDTO {

    public enum StatusSearch {
        // Päätös käsittelyssä
        DRAFT,
        // Lukittu
        LOCKED,
        // Julkaistu
        PUBLISHED
    }

    @Min(0)
    private int page;

    @Min(0)
    private int size;

    @Pattern(regexp = "^\\d{0,3}$")
    private String rhyOfficialCode;

    @Pattern(regexp = "^\\d{0,3}$")
    private String rkaOfficialCode;

    @Min(2020)
    private Integer year;

    private Long handlerId;

    private Set<NominationDecisionSearchDTO.StatusSearch> statuses;

    private Set<NominationDecision.NominationDecisionType> decisionTypes;

    private Set<OccupationType> occupationTypes;

    private Set<AppealStatus> appealStatuses;

    private Integer decisionNumber;

    @AssertTrue
    public boolean isJhtOccupationsOnly() {
        return this.occupationTypes == null ||
                this.occupationTypes.stream().allMatch(OccupationType::isJHTOccupation);
    }

    public PageRequest asPageRequest() {
        return PageRequest.of(page, size);
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

    public Integer getYear() {
        return year;
    }

    public void setYear(final Integer year) {
        this.year = year;
    }

    public Long getHandlerId() {
        return handlerId;
    }

    public void setHandlerId(final Long handlerId) {
        this.handlerId = handlerId;
    }

    public Set<NominationDecisionSearchDTO.StatusSearch> getStatuses() {
        return statuses;
    }

    public void setStatuses(final Set<NominationDecisionSearchDTO.StatusSearch> statuses) {
        this.statuses = statuses;
    }

    public Set<NominationDecision.NominationDecisionType> getDecisionTypes() {
        return decisionTypes;
    }

    public void setDecisionTypes(final Set<NominationDecision.NominationDecisionType> decisionTypes) {
        this.decisionTypes = decisionTypes;
    }

    public Set<OccupationType> getOccupationTypes() {
        return occupationTypes;
    }

    public void setOccupationTypes(final Set<OccupationType> occupationTypes) {
        this.occupationTypes = occupationTypes;
    }

    public Set<AppealStatus> getAppealStatuses() {
        return appealStatuses;
    }

    public void setAppealStatuses(final Set<AppealStatus> appealStatuses) {
        this.appealStatuses = appealStatuses;
    }

    public Integer getDecisionNumber() {
        return decisionNumber;
    }

    public void setDecisionNumber(final Integer decisionNumber) {
        this.decisionNumber = decisionNumber;
    }

}
