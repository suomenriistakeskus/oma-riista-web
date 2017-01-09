package fi.riista.feature.harvestpermit.season;

import fi.riista.feature.common.entity.Has2BeginEndDates;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.harvestpermit.report.fields.HarvestReportFields;
import fi.riista.util.DateUtil;
import fi.riista.util.LocalisedString;
import org.hibernate.validator.constraints.NotBlank;
import org.joda.time.LocalDate;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Access(AccessType.FIELD)
public class HarvestSeason extends LifecycleEntity<Long> implements Has2BeginEndDates {

    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String nameFinnish;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String nameSwedish;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "harvest_report_fields_id", nullable = false)
    private HarvestReportFields fields;

    @NotNull
    @Column(nullable = false)
    private LocalDate beginDate;
    @NotNull
    @Column(nullable = false)
    private LocalDate endDate;
    @NotNull
    @Column(nullable = false)
    private LocalDate endOfReportingDate;

    @Column
    private LocalDate beginDate2;
    @Column
    private LocalDate endDate2;
    @Column
    private LocalDate endOfReportingDate2;

    @OneToMany(mappedBy = "harvestSeason")
    private Set<HarvestQuota> quotas = new HashSet<>();

    protected HarvestSeason() {
    }

    public HarvestSeason(String nameFinnish,
            String nameSwedish,
            HarvestReportFields fields,
            LocalDate beginDate,
            LocalDate endDate,
            LocalDate endOfReportingDate) {
        this.nameFinnish = nameFinnish;
        this.nameSwedish = nameSwedish;
        this.fields = fields;
        this.beginDate = beginDate;
        this.endDate = endDate;
        this.endOfReportingDate = endOfReportingDate;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "harvest_season_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getNameFinnish() {
        return nameFinnish;
    }

    public void setNameFinnish(String nameFinnish) {
        this.nameFinnish = nameFinnish;
    }

    public String getNameSwedish() {
        return nameSwedish;
    }

    public void setNameSwedish(String nameSwedish) {
        this.nameSwedish = nameSwedish;
    }

    public LocalisedString getNameLocalisation() {
        return LocalisedString.of(nameFinnish, nameSwedish);
    }

    public HarvestReportFields getFields() {
        return fields;
    }

    public void setFields(HarvestReportFields fields) {
        this.fields = fields;
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

    public Set<HarvestQuota> getQuotas() {
        return quotas;
    }

    public void setQuotas(Set<HarvestQuota> quotas) {
        this.quotas = quotas;
    }

    public boolean hasQuotas() {
        return !quotas.isEmpty();
    }

    public boolean isHarvestReportRequired(LocalDate date, int gameSpeciesCode) {
        return fields.getSpecies().getOfficialCode() == gameSpeciesCode
                && DateUtil.overlapsInclusive(beginDate, endDate, date);
    }

}
