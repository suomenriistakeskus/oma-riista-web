package fi.riista.integration.metsahallitus.permit;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.util.LocalisedString;
import fi.riista.validation.FinnishHunterNumber;
import fi.riista.validation.FinnishSocialSecurityNumber;
import org.apache.commons.lang.StringUtils;
import javax.validation.constraints.NotBlank;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Size;

@Entity
@Table(name = "mh_permit")
@Access(value = AccessType.FIELD)
public class MetsahallitusPermit extends LifecycleEntity<Long> {

    private Long id;

    @FinnishSocialSecurityNumber
    @Column(length = 11)
    private String ssn;

    @FinnishHunterNumber
    @Column
    private String hunterNumber;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String status;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, unique = true)
    private String permitIdentifier;

    @Size(max = 255)
    @Column
    private String permitType;

    @Size(max = 255)
    @Column
    private String permitTypeSwedish;

    @Size(max = 255)
    @Column
    private String permitTypeEnglish;

    @Size(max = 255)
    @Column
    private String permitName;

    @Size(max = 255)
    @Column
    private String permitNameSwedish;

    @Size(max = 255)
    @Column
    private String permitNameEnglish;

    @Size(max = 255)
    @Column
    private String areaNumber;

    @Size(max = 255)
    @Column
    private String areaName;

    @Size(max = 255)
    @Column
    private String areaNameSwedish;

    @Size(max = 255)
    @Column
    private String areaNameEnglish;

    @Column
    private LocalDate beginDate;

    @Column
    private LocalDate endDate;

    @Size(max = 255)
    @Column
    private String url;

    // saalispalauteAnnettu
    @Column
    private Boolean harvestReportSubmitted;

    @AssertTrue
    public boolean isSsnOrHunterNumberGiven() {
        return StringUtils.isNotBlank(ssn) || StringUtils.isNotBlank(hunterNumber);
    }

    @Nonnull
    public LocalisedString getPermitNameLocalisation() {
        return LocalisedString.of(permitName, permitNameSwedish, permitNameEnglish);
    }

    @Nonnull
    public LocalisedString getPermitTypeLocalisation() {
        return LocalisedString.of(permitType, permitTypeSwedish, permitTypeEnglish);
    }

    @Nonnull
    public LocalisedString getAreaNameLocalisation() {
        return LocalisedString.of(areaName, areaNameSwedish, areaNameEnglish);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mh_permit_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(final String ssn) {
        this.ssn = ssn;
    }

    public String getHunterNumber() {
        return hunterNumber;
    }

    public void setHunterNumber(final String hunterNumber) {
        this.hunterNumber = hunterNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getPermitIdentifier() {
        return permitIdentifier;
    }

    public void setPermitIdentifier(String permitIdentifier) {
        this.permitIdentifier = permitIdentifier;
    }

    public String getPermitType() {
        return permitType;
    }

    public void setPermitType(String permitType) {
        this.permitType = permitType;
    }

    public String getPermitTypeSwedish() {
        return permitTypeSwedish;
    }

    public void setPermitTypeSwedish(String permitTypeSwedish) {
        this.permitTypeSwedish = permitTypeSwedish;
    }

    public String getPermitTypeEnglish() {
        return permitTypeEnglish;
    }

    public void setPermitTypeEnglish(String permitTypeEnglish) {
        this.permitTypeEnglish = permitTypeEnglish;
    }

    public String getPermitName() {
        return permitName;
    }

    public void setPermitName(String permitName) {
        this.permitName = permitName;
    }

    public String getPermitNameSwedish() {
        return permitNameSwedish;
    }

    public void setPermitNameSwedish(String permitNameSwedish) {
        this.permitNameSwedish = permitNameSwedish;
    }

    public String getPermitNameEnglish() {
        return permitNameEnglish;
    }

    public void setPermitNameEnglish(String permitNameEnglish) {
        this.permitNameEnglish = permitNameEnglish;
    }

    public String getAreaNumber() {
        return areaNumber;
    }

    public void setAreaNumber(String areaNumber) {
        this.areaNumber = areaNumber;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getAreaNameSwedish() {
        return areaNameSwedish;
    }

    public void setAreaNameSwedish(String areaNameSwedish) {
        this.areaNameSwedish = areaNameSwedish;
    }

    public String getAreaNameEnglish() {
        return areaNameEnglish;
    }

    public void setAreaNameEnglish(String areaNameEnglish) {
        this.areaNameEnglish = areaNameEnglish;
    }

    public LocalDate getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getHarvestReportSubmitted() {
        return harvestReportSubmitted;
    }

    public void setHarvestReportSubmitted(final Boolean harvestReportSubmitted) {
        this.harvestReportSubmitted = harvestReportSubmitted;
    }
}
