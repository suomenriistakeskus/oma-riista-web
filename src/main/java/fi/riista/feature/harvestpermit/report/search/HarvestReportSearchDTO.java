package fi.riista.feature.harvestpermit.report.search;

import fi.riista.feature.common.dto.XssSafe;
import fi.riista.feature.common.entity.HasBeginAndEndDate;

import fi.riista.feature.harvestpermit.report.HarvestReport;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

public class HarvestReportSearchDTO implements Cloneable, HasBeginAndEndDate {

    private LocalDate beginDate;
    private LocalDate endDate;
    private Long seasonId;
    private Long fieldsId;
    private Long harvestAreaId;
    private Long areaId;
    private Long rhyId;
    private List<HarvestReport.State> states;

    //@FinnishHuntingPermitNumber
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String permitNumber;

    @XssSafe
    private String text;

    public static HarvestReportSearchDTO cloneWithRhyRelevantFields(final HarvestReportSearchDTO that) {
        try {
            final HarvestReportSearchDTO dto = HarvestReportSearchDTO.class.cast(that.clone());

            // Explicitly null irrelevant fields
            dto.setSeasonId(null);
            dto.setHarvestAreaId(null);
            dto.setAreaId(null);
            dto.setText(null);
            return dto;
        } catch (final CloneNotSupportedException e) {
            // Should never end up here
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        final HarvestReportSearchDTO clone = (HarvestReportSearchDTO) super.clone();

        // Defensive copy of mutable fields
        clone.setStates(new ArrayList<>(states));

        return clone;
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

    public Long getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(Long seasonId) {
        this.seasonId = seasonId;
    }

    public Long getFieldsId() {
        return fieldsId;
    }

    public void setFieldsId(Long fieldsId) {
        this.fieldsId = fieldsId;
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

    public List<HarvestReport.State> getStates() {
        return states;
    }

    public void setStates(List<HarvestReport.State> states) {
        this.states = states;
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

}
