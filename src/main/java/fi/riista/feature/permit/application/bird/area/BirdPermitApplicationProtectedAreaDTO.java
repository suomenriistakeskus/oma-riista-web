package fi.riista.feature.permit.application.bird.area;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.permit.application.bird.BirdPermitApplication;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.persistence.Column;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Optional;

import fi.riista.feature.permit.application.bird.ProtectedAreaType;
import static java.util.Objects.requireNonNull;

public class BirdPermitApplicationProtectedAreaDTO {

    @NotNull
    private ProtectedAreaType protectedAreaType;

    @NotBlank
    @Length(min = 5, max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String name;

    @NotBlank
    @Length(min = 5, max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String streetAddress;

    @NotBlank
    @Length(max = 10)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String postalCode;

    @NotBlank
    @Length(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String city;

    @NotBlank
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String descriptionOfRights;

    @NotNull
    @Min(1)
    private Integer protectedAreSize;

    @Valid
    @NotNull
    private GeoLocation geoLocation;

    public BirdPermitApplicationProtectedAreaDTO() {
    }

    public BirdPermitApplicationProtectedAreaDTO(
            final ProtectedAreaType protectedAreaType,
            final String name,
            final Integer protectedAreSize,
            final String streetAddress,
            final String postalCode,
            final String city,
            final String descriptionOfRights,
            final GeoLocation geoLocation) {
        this.protectedAreaType = protectedAreaType;
        this.name = name;
        this.protectedAreSize = protectedAreSize;
        this.streetAddress = streetAddress;
        this.postalCode = postalCode;
        this.city = city;
        this.descriptionOfRights = descriptionOfRights;
        this.geoLocation = geoLocation;
    }

    public static BirdPermitApplicationProtectedAreaDTO createFrom(final BirdPermitApplication birdApplication) {
        return Optional.ofNullable(birdApplication)
                .map(BirdPermitApplication::getProtectedArea)
                .map(BirdPermitApplicationProtectedAreaDTO::createFrom)
                .orElseGet(BirdPermitApplicationProtectedAreaDTO::new);
    }

    public static BirdPermitApplicationProtectedAreaDTO createFrom(final @Nonnull BirdPermitApplicationProtectedArea area) {
        requireNonNull(area);

        return new BirdPermitApplicationProtectedAreaDTO(
                area.getProtectedAreaType(),
                area.getName(),
                area.getProtectedAreaSize(),
                area.getStreetAddress(),
                area.getPostalCode(),
                area.getCity(),
                area.getDescriptionOfRights(),
                area.getGeoLocation());
    }

    public BirdPermitApplicationProtectedArea toEntity() {
        final BirdPermitApplicationProtectedArea entity = new BirdPermitApplicationProtectedArea();
        entity.setName(name);
        entity.setProtectedAreaType(protectedAreaType);
        entity.setAreaSize(protectedAreSize);
        entity.setStreetAddress(streetAddress);
        entity.setPostalCode(postalCode);
        entity.setCity(city);
        entity.setDescriptionOfRights(descriptionOfRights);
        entity.setGeoLocation(geoLocation);

        return entity;
    }

    public ProtectedAreaType getProtectedAreaType() {
        return protectedAreaType;
    }

    public Integer getProtectedAreSize() {
        return protectedAreSize;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
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
