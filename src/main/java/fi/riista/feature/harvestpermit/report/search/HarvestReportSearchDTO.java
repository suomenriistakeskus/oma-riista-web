package fi.riista.feature.harvestpermit.report.search;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fi.riista.feature.common.entity.HasBeginAndEndDate;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.validation.XssSafe;
import javax.validation.constraints.NotEmpty;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import java.util.List;

public class HarvestReportSearchDTO implements HasBeginAndEndDate {
    public enum SearchType {
        MODERATOR,
        COORDINATOR
    }

    @JsonIgnore
    private SearchType searchType;
    private LocalDate beginDate;
    private LocalDate endDate;
    private Long personId;
    private Long seasonId;
    private Integer gameSpeciesCode;
    private Long harvestAreaId;
    private Long areaId;
    private Long rhyId;

    @NotEmpty
    private List<HarvestReportState> states;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String permitNumber;

    @XssSafe
    private String text;

    public HarvestReportSearchDTO() {
    }

    public HarvestReportSearchDTO(final SearchType searchType, final List<HarvestReportState> states) {
        this.searchType = searchType;
        this.states = states;
    }

    @JsonIgnore
    public boolean isCoordinatorSearch() {
        return searchType == SearchType.COORDINATOR;
    }

    @JsonIgnore
    public boolean isModeratorSearch() {
        return searchType == SearchType.MODERATOR;
    }

    public SearchType getSearchType() {
        return searchType;
    }

    public void setSearchType(final SearchType searchType) {
        this.searchType = searchType;
    }

    @Override
    public LocalDate getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(final Long personId) {
        this.personId = personId;
    }

    public Integer getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public void setGameSpeciesCode(final Integer gameSpeciesCode) {
        this.gameSpeciesCode = gameSpeciesCode;
    }

    public Long getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(Long seasonId) {
        this.seasonId = seasonId;
    }

    public Long getHarvestAreaId() {
        return harvestAreaId;
    }

    public void setHarvestAreaId(Long harvestAreaId) {
        this.harvestAreaId = harvestAreaId;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPermitNumber() {
        return permitNumber;
    }

    public void setPermitNumber(String permitNumber) {
        this.permitNumber = permitNumber;
    }

    public List<HarvestReportState> getStates() {
        return states;
    }

    public void setStates(final List<HarvestReportState> states) {
        this.states = states;
    }
}
