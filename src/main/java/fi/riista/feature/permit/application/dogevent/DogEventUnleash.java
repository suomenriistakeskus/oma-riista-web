package fi.riista.feature.permit.application.dogevent;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.HasBeginAndEndDate;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Objects;

@Entity
@Access(AccessType.FIELD)
public class DogEventUnleash extends LifecycleEntity<Long> implements HasBeginAndEndDate {

    public static final String ID_COLUMN_NAME = "dog_event_unleash_id";

    private Long id;

    @NotNull
    @JoinColumn(unique = true, nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private HarvestPermitApplication harvestPermitApplication;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DogEventType eventType;

    @NotNull
    @Column(nullable = false)
    private LocalDate beginDate;

    @Column
    private LocalDate endDate;

    @Min(1)
    @Max(9999)
    @Column(nullable = false)
    private int dogsAmount;

    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column
    private String naturaArea;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT", nullable = false)
    private String eventDescription;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT", nullable = false)
    private String locationDescription;

    @NotBlank
    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(nullable = false)
    private String contactName;

    @Email
    @Size(max = 255)
    @Column
    private String contactMail;

    @NotBlank
    @Size(max = 255)
    @Pattern(regexp = "[+]?[ 0-9]+")
    @Column(nullable = false)
    private String contactPhone;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String additionalInfo;

    @Valid
    @NotNull
    @Embedded
    private GeoLocation geoLocation;

    // Comparator

    public boolean isEqualTo(final DogEventUnleash other) {
        return Objects.equals(id, other.getId())
                && Objects.equals(eventType, other.getEventType())
                && Objects.equals(beginDate, other.getBeginDate())
                && Objects.equals(endDate, other.getEndDate())
                && Objects.equals(dogsAmount, other.getDogsAmount())
                && Objects.equals(naturaArea, other.getNaturaArea())
                && Objects.equals(eventDescription, other.getEventDescription())
                && Objects.equals(locationDescription, other.getLocationDescription())
                && Objects.equals(contactName, other.getContactName())
                && Objects.equals(contactMail, other.getContactMail())
                && Objects.equals(contactPhone, other.getContactPhone())
                && Objects.equals(additionalInfo, other.getAdditionalInfo())
                && Objects.equals(geoLocation, other.getGeoLocation());
    }

    // Accessors

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = ID_COLUMN_NAME, nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public HarvestPermitApplication getHarvestPermitApplication() {
        return harvestPermitApplication;
    }

    public void setHarvestPermitApplication(final HarvestPermitApplication harvestPermitApplication) {
        this.harvestPermitApplication = harvestPermitApplication;
    }

    public DogEventType getEventType() {
        return eventType;
    }

    public void setEventType(final DogEventType eventType) {
        this.eventType = eventType;
    }

    @Override
    public LocalDate getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(final LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    @Override
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getDogsAmount() {
        return dogsAmount;
    }

    public void setDogsAmount(final int dogsAmount) {
        this.dogsAmount = dogsAmount;
    }

    public String getNaturaArea() {
        return naturaArea;
    }

    public void setNaturaArea(final String naturaArea) {
        this.naturaArea = naturaArea;
    }

    public String getEventDescription() {
        return eventDescription;
    }

    public void setEventDescription(final String eventDescription) {
        this.eventDescription = eventDescription;
    }

    public String getLocationDescription() {
        return locationDescription;
    }

    public void setLocationDescription(final String locationDescription) {
        this.locationDescription = locationDescription;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(final String contactName) {
        this.contactName = contactName;
    }

    public String getContactMail() {
        return contactMail;
    }

    public void setContactMail(final String contactMail) {
        this.contactMail = contactMail;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(final String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(final String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(final GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

}
