package fi.riista.feature.permit.application.dogevent;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.permit.application.HarvestPermitApplication;
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
public class DogEventApplication extends LifecycleEntity<Long> implements DerogationPermitApplicationAreaInfo {

    public static final String ID_COLUMN_NAME = "dog_event_application_id";

    public static DogEventApplication create(@Nonnull final HarvestPermitApplication application) {

        requireNonNull(application);
        checkArgument(application.getHarvestPermitCategory() == HarvestPermitCategory.DOG_UNLEASH ||
                              application.getHarvestPermitCategory() == HarvestPermitCategory.DOG_DISTURBANCE,
                      "Application category must be dog event");

        final DogEventApplication dogEventApplication = new DogEventApplication();
        dogEventApplication.setHarvestPermitApplication(application);
        dogEventApplication.setAreaSize(0);
        return dogEventApplication;
    }

    /* package */ DogEventApplication() {
    }

    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(unique = true, nullable = false)
    private HarvestPermitApplication harvestPermitApplication;

    @Min(0)
    @Column
    private Integer areaSize;

    @Valid
    @Embedded
    private GeoLocation geoLocation;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String areaDescription;

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

    @Override
    public Integer getAreaSize() {
        return areaSize;
    }

    @Override
    public void setAreaSize(final Integer areaSize) {
        this.areaSize = areaSize;
    }

    @Override
    public void setGeoLocation(final GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    @Override
    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    @Override
    public String getAreaDescription() {
        return areaDescription;
    }

    @Override
    public void setAreaDescription(final String areaDescription) {
        this.areaDescription = areaDescription;
    }
}
