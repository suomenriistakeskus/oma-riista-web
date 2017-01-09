package fi.riista.feature.harvestpermit.season;

import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFieldsDTO;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import java.util.List;

public class HarvestSeasonDTO extends BaseEntityDTO<Long> implements Has2BeginEndDates {

    @Nonnull
    public static HarvestSeasonDTO create(final @Nonnull HarvestSeason season) {
        final HarvestSeasonDTO dto = new HarvestSeasonDTO();
        DtoUtil.copyBaseFields(season, dto);

        dto.setNameFI(season.getNameFinnish());
        dto.setNameSV(season.getNameSwedish());
        dto.setFields(HarvestReportFieldsDTO.create(season.getFields()));
        dto.copyDatesFrom(season);
        dto.setEndOfReportingDate(season.getEndOfReportingDate());
        dto.setEndOfReportingDate2(season.getEndOfReportingDate2());
        dto.setQuotas(F.mapNonNullsToList(season.getQuotas(), HarvestQuotaDTO::create));

        return dto;
    }

    private Long id;
    private Integer rev;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String nameFI;
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String nameSV;

    @Valid
    private HarvestReportFieldsDTO fields;

    private LocalDate beginDate;
    private LocalDate endDate;
    private LocalDate endOfReportingDate;

    private LocalDate beginDate2;
    private LocalDate endDate2;
    private LocalDate endOfReportingDate2;

    private List<HarvestQuotaDTO> quotas;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(Integer rev) {
        this.rev = rev;
    }

    public void setNameFI(String nameFI) {
        this.nameFI = nameFI;
    }

    public String getNameFI() {
        return nameFI;
    }

    public void setNameSV(String nameSV) {
        this.nameSV = nameSV;
    }

    public String getNameSV() {
        return nameSV;
    }

    public void setFields(HarvestReportFieldsDTO fields) {
        this.fields = fields;
    }

    public HarvestReportFieldsDTO getFields() {
        return fields;
    }

    @Override
    public LocalDate getBeginDate() {
        return beginDate;
    }

    @Override
    public void setBeginDate(LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    @Override
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDate getEndOfReportingDate() {
        return endOfReportingDate;
    }

    public void setEndOfReportingDate(LocalDate endOfReportingDate) {
        this.endOfReportingDate = endOfReportingDate;
    }

    @Override
    public LocalDate getBeginDate2() {
        return beginDate2;
    }

    @Override
    public void setBeginDate2(LocalDate beginDate2) {
        this.beginDate2 = beginDate2;
    }

    @Override
    public LocalDate getEndDate2() {
        return endDate2;
    }

    @Override
    public void setEndDate2(LocalDate endDate2) {
        this.endDate2 = endDate2;
    }

    public LocalDate getEndOfReportingDate2() {
        return endOfReportingDate2;
    }

    public void setEndOfReportingDate2(LocalDate endOfReportingDate2) {
        this.endOfReportingDate2 = endOfReportingDate2;
    }

    public List<HarvestQuotaDTO> getQuotas() {
        return quotas;
    }

    public void setQuotas(List<HarvestQuotaDTO> quotas) {
        this.quotas = quotas;
    }
}
