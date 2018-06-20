package fi.riista.feature.metsahallitus.permit;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.organization.person.Person;
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
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Access(value = AccessType.FIELD)
public class MhPermit extends LifecycleEntity<Long> {

    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Person person;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String permitIdentifier;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String permitType;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String permitTypeSwedish;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String permitTypeEnglish;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String permitName;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String permitNameSwedish;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String permitNameEnglish;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String areaNumber;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String areaName;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String areaNameSwedish;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false)
    private String areaNameEnglish;

    @NotNull
    @Column(nullable = false)
    private LocalDate beginDate;

    @NotNull
    @Column(nullable = false)
    private LocalDate endDate;

    @Size(max = 255)
    @Column
    private String url;

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

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
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
}
