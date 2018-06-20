package fi.riista.feature.organization.lupahallinta;

import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.util.LocalisedString;
import org.hibernate.validator.constraints.NotBlank;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

@Entity
@Table(name = "lh_org")
@Access(value = AccessType.FIELD)
public class LHOrganisation extends BaseEntity<Long> {

    private static final String ID_COLUMN_NAME = "lh_org_id";

    private Long id;

    @NotBlank @Size(min = 7, max = 7)
    @Column(nullable = false, length = 7)
    private String officialCode;

    @Size(max = 255)
    @Column
    private String nameFinnish;

    @Size(max = 255)
    @Column
    private String nameSwedish;

    @Size(min = 3, max = 3)
    @Column(length = 3)
    private String rhyOfficialCode;

    @Size(max = 255)
    @Column
    private String mooseAreaCode;

    @Column
    private Integer latitude;

    @Column
    private Integer longitude;

    @Column
    private Integer areaSize;

    @Size(max = 255)
    @Column
    private String contactPersonSsn;

    @Size(max = 255)
    @Column
    private String contactPersonRhy;

    @Size(max = 255)
    @Column
    private String contactPersonName;

    @Size(max = 255)
    @Column(name = "contact_person_address_1")
    private String contactPersonAddress1;

    @Size(max = 255)
    @Column(name = "contact_person_address_2")
    private String contactPersonAddress2;

    @Size(max = 255)
    @Column(name = "contact_person_phone_1")
    private String contactPersonPhone1;

    @Size(max = 255)
    @Column(name = "contact_person_phone_2")
    private String contactPersonPhone2;

    @Size(max = 255)
    @Column
    private String contactPersonEmail;

    @Size(max = 255)
    @Column
    private String contactPersonLang;

    @Nonnull
    public LocalisedString getNameLocalisation() {
        return LocalisedString.of(nameFinnish, nameSwedish);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = ID_COLUMN_NAME, nullable = false)
    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public String getOfficialCode() {
        return officialCode;
    }

    public void setOfficialCode(final String officialCode) {
        this.officialCode = officialCode;
    }

    public String getNameFinnish() {
        return nameFinnish;
    }

    public void setNameFinnish(final String nameFinnish) {
        this.nameFinnish = nameFinnish;
    }

    public String getNameSwedish() {
        return nameSwedish;
    }

    public void setNameSwedish(final String nameSwedish) {
        this.nameSwedish = nameSwedish;
    }

    public String getRhyOfficialCode() {
        return rhyOfficialCode;
    }

    public void setRhyOfficialCode(final String rhyOfficialCode) {
        this.rhyOfficialCode = rhyOfficialCode;
    }

    public String getMooseAreaCode() {
        return mooseAreaCode;
    }

    public void setMooseAreaCode(final String mooseAreaCode) {
        this.mooseAreaCode = mooseAreaCode;
    }

    public Integer getLatitude() {
        return latitude;
    }

    public void setLatitude(final Integer latitude) {
        this.latitude = latitude;
    }

    public Integer getLongitude() {
        return longitude;
    }

    public void setLongitude(final Integer longitude) {
        this.longitude = longitude;
    }

    public Integer getAreaSize() {
        return areaSize;
    }

    public void setAreaSize(final Integer areaSize) {
        this.areaSize = areaSize;
    }

    public String getContactPersonSsn() {
        return contactPersonSsn;
    }

    public void setContactPersonSsn(final String contactPersonSsn) {
        this.contactPersonSsn = contactPersonSsn;
    }

    public String getContactPersonRhy() {
        return contactPersonRhy;
    }

    public void setContactPersonRhy(final String contactPersonRhy) {
        this.contactPersonRhy = contactPersonRhy;
    }

    public String getContactPersonName() {
        return contactPersonName;
    }

    public void setContactPersonName(final String contactPersonName) {
        this.contactPersonName = contactPersonName;
    }

    public String getContactPersonAddress1() {
        return contactPersonAddress1;
    }

    public void setContactPersonAddress1(final String contactPersonAddress1) {
        this.contactPersonAddress1 = contactPersonAddress1;
    }

    public String getContactPersonAddress2() {
        return contactPersonAddress2;
    }

    public void setContactPersonAddress2(final String contactPersonAddress2) {
        this.contactPersonAddress2 = contactPersonAddress2;
    }

    public String getContactPersonPhone1() {
        return contactPersonPhone1;
    }

    public void setContactPersonPhone1(final String contactPersonPhone1) {
        this.contactPersonPhone1 = contactPersonPhone1;
    }

    public String getContactPersonPhone2() {
        return contactPersonPhone2;
    }

    public void setContactPersonPhone2(final String contactPersonPhone2) {
        this.contactPersonPhone2 = contactPersonPhone2;
    }

    public String getContactPersonEmail() {
        return contactPersonEmail;
    }

    public void setContactPersonEmail(final String contactPersonEmail) {
        this.contactPersonEmail = contactPersonEmail;
    }

    public String getContactPersonLang() {
        return contactPersonLang;
    }

    public void setContactPersonLang(final String contactPersonLang) {
        this.contactPersonLang = contactPersonLang;
    }
}
