package fi.riista.feature.permit.application.dogevent.unleash;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.HasBeginAndEndDate;
import fi.riista.feature.permit.application.dogevent.DogEventType;
import fi.riista.feature.permit.application.dogevent.DogEventUnleash;
import fi.riista.validation.PhoneNumber;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

import static java.util.Objects.requireNonNull;

public class DogEventUnleashDTO implements Serializable, HasBeginAndEndDate {

    public static DogEventUnleashDTO createFrom(@Nonnull final DogEventUnleash entity) {
        requireNonNull(entity);
        final DogEventUnleashDTO dto = new DogEventUnleashDTO();
        dto.setId(entity.getId());
        dto.setEventType(entity.getEventType());
        dto.setBeginDate(entity.getBeginDate());
        dto.setEndDate(entity.getEndDate());
        dto.setDogsAmount(entity.getDogsAmount());
        dto.setNaturaArea(entity.getNaturaArea());
        dto.setEventDescription(entity.getEventDescription());
        dto.setLocationDescription(entity.getLocationDescription());
        dto.setContactName(entity.getContactName());
        dto.setContactMail(entity.getContactMail());
        dto.setContactPhone(entity.getContactPhone());
        dto.setAdditionalInfo(entity.getAdditionalInfo());
        dto.setGeoLocation(entity.getGeoLocation());
        return dto;
    }

    private Long id;

    @NotNull
    private DogEventType eventType;

    @NotNull
    private LocalDate beginDate;

    private LocalDate endDate;

    @Min(1)
    @Max(9999)
    private int dogsAmount;

    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String naturaArea;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String eventDescription;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String locationDescription;

    @NotBlank
    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String contactName;

    @Email
    @Size(max = 255)
    private String contactMail;

    @NotBlank
    @Size(max = 255)
    @PhoneNumber
    private String contactPhone;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String additionalInfo;

    @NotNull
    @Valid
    private GeoLocation geoLocation;

    // Accessors

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
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
