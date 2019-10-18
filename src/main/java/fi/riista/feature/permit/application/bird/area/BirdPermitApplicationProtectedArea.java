package fi.riista.feature.permit.application.bird.area;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.permit.application.bird.ProtectedAreaType;
import org.hibernate.validator.constraints.SafeHtml;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

@Embeddable
@Access(AccessType.FIELD)
public class BirdPermitApplicationProtectedArea {

    @Enumerated(EnumType.STRING)
    private ProtectedAreaType protectedAreaType;

    @Column
    @Min(0)
    private Integer protectedAreaSize;

    @Column(name = "protected_area_name")
    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String name;

    @Column(name = "protected_area_street_address")
    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String streetAddress;

    @Column(name = "protected_area_postal_code")
    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String postalCode;

    @Column(name = "protected_area_city")
    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String city;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT", name = "protected_area_description")
    private String descriptionOfRights;

    @Valid
    @Embedded
    private GeoLocation geoLocation;

    public ProtectedAreaType getProtectedAreaType() {
        return protectedAreaType;
    }

    public void setProtectedAreaType(ProtectedAreaType protectedAreaType) {
        this.protectedAreaType = protectedAreaType;
    }

    public Integer getProtectedAreaSize() {
        return protectedAreaSize;
    }

    public void setAreaSize(Integer protectedAreaSize) {
        this.protectedAreaSize = protectedAreaSize;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String protectedAreaAddressLine1) {
        this.streetAddress = protectedAreaAddressLine1;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String getProtectedAreaAddressLine2) {
        this.postalCode = getProtectedAreaAddressLine2;
    }

    public void setProtectedAreaSize(Integer protectedAreaSize) {
        this.protectedAreaSize = protectedAreaSize;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDescriptionOfRights() {
        return descriptionOfRights;
    }

    public void setDescriptionOfRights(String descriptionOfRights) {
        this.descriptionOfRights = descriptionOfRights;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(final GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }
}
