package fi.riista.feature.permit.application.importing;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HasParentApplication;
import fi.riista.feature.permit.application.derogation.area.DerogationPermitApplicationAreaInfo;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Entity
@Access(AccessType.FIELD)
public class ImportingPermitApplication extends HasParentApplication implements DerogationPermitApplicationAreaInfo {

    public static final String ID_COLUMN_NAME = "importing_permit_application_id";

    public static ImportingPermitApplication create(@Nonnull final HarvestPermitApplication application) {
        requireNonNull(application);
        checkArgument(application.getHarvestPermitCategory() == HarvestPermitCategory.IMPORTING,
                "Application category must be importing");

        return new ImportingPermitApplication(application);
    }

    private Long id;

    @Column
    @Min(0)
    private Integer areaSize;

    @Valid
    @Embedded
    private GeoLocation geoLocation;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String areaDescription;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String countryOfOrigin;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String details;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String purpose;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String release;

    /*package*/ ImportingPermitApplication() { }

    private ImportingPermitApplication(final HarvestPermitApplication application) {
        setHarvestPermitApplication(application);
    }

    // ACCESSORS

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

    @Override
    public Integer getAreaSize() {
        return areaSize;
    }

    @Override
    public void setAreaSize(final Integer areaSize) {
        this.areaSize = areaSize;
    }

    @Override
    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    @Override
    public void setGeoLocation(final GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    @Override
    public String getAreaDescription() {
        return areaDescription;
    }

    @Override
    public void setAreaDescription(final String areaDescription) {
        this.areaDescription = areaDescription;
    }

    public String getCountryOfOrigin() {
        return countryOfOrigin;
    }

    public void setCountryOfOrigin(final String countryOfOrigin) {
        this.countryOfOrigin = countryOfOrigin;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(final String details) {
        this.details = details;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(final String purpose) {
        this.purpose = purpose;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(final String release) {
        this.release = release;
    }
}
