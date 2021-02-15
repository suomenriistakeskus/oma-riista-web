package fi.riista.feature.harvestpermit.search;

import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.feature.common.decision.GrantStatus;
import org.hibernate.validator.constraints.SafeHtml;
import org.springframework.data.domain.Sort;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

public class HarvestPermitSearchDTO {
    enum Validity {
        ACTIVE, PASSED, FUTURE
    }

    enum SortType {
        NORMAL,
        SPECIAL
    }

    // @FinnishHuntingPermitNumber
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String permitNumber;

    private Long areaId;
    private Long rhyId;

    private Integer speciesCode;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String year;

    private HarvestReportState state;

    private boolean reportNotDone;

    private Validity validity;

    @Valid
    private HarvestPermitTypeDTO permitType;

    @Valid
    private List<GrantStatus> decisionStatuses;

    private SortType sortingType = SortType.NORMAL;
    private Sort.Direction permitNumberSort = Sort.Direction.ASC;
    private Sort.Direction yearSort = Sort.Direction.DESC;
    private Sort.Direction ordinalSort = Sort.Direction.DESC;


    public void clearAndCheckRhyParams() {
        this.areaId = null;
        Objects.requireNonNull(this.rhyId);
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public void setPermitNumber(String permitNumber) {
        this.permitNumber = permitNumber;
    }

    public Long getAreaId() {
        return areaId;
    }

    public void setAreaId(Long areaId) {
        this.areaId = areaId;
    }

    public Long getRhyId() {
        return rhyId;
    }

    public void setRhyId(Long rhyId) {
        this.rhyId = rhyId;
    }

    public Integer getSpeciesCode() {
        return speciesCode;
    }

    public void setSpeciesCode(Integer speciesCode) {
        this.speciesCode = speciesCode;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public HarvestReportState getState() {
        return state;
    }

    public void setState(HarvestReportState state) {
        this.state = state;
    }

    public boolean isReportNotDone() {
        return reportNotDone;
    }

    public void setReportNotDone(boolean reportNotDone) {
        this.reportNotDone = reportNotDone;
    }

    public Validity getValidity() {
        return validity;
    }

    public void setValidity(Validity validity) {
        this.validity = validity;
    }

    public HarvestPermitTypeDTO getPermitType() {
        return permitType;
    }

    public void setPermitType(HarvestPermitTypeDTO permitType) {
        this.permitType = permitType;
    }

    public SortType getSortingType() {
        return sortingType;
    }

    public void setSortingType(final SortType sortingType) {
        this.sortingType = sortingType;
    }

    public Sort.Direction getPermitNumberSort() {
        return permitNumberSort;
    }

    public void setPermitNumberSort(final Sort.Direction permitNumberSort) {
        this.permitNumberSort = permitNumberSort;
    }

    public Sort.Direction getYearSort() {
        return yearSort;
    }

    public void setYearSort(final Sort.Direction yearSort) {
        this.yearSort = yearSort;
    }

    public Sort.Direction getOrdinalSort() {
        return ordinalSort;
    }

    public void setOrdinalSort(final Sort.Direction ordinalSort) {
        this.ordinalSort = ordinalSort;
    }

    public List<GrantStatus> getDecisionStatuses() {
        return decisionStatuses;
    }

    public void setDecisionStatuses(final List<GrantStatus> decisionStatuses) {
        this.decisionStatuses = decisionStatuses;
    }
}
