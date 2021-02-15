package fi.riista.feature.permit.application.carnivore;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.LifecycleEntity;
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
public class CarnivorePermitApplication extends LifecycleEntity<Long> implements DerogationPermitApplicationAreaInfo {

    public static final String ID_COLUMN_NAME = "carnivore_permit_application_id";

    public static CarnivorePermitApplication create(@Nonnull final HarvestPermitApplication application) {
        requireNonNull(application);
        checkArgument(application.getHarvestPermitCategory().isLargeCarnivore(),
                "Application category must relate to large carnivore species");

        final CarnivorePermitApplication carnivorePermitApplication = new CarnivorePermitApplication();
        carnivorePermitApplication.setHarvestPermitApplication(application);
        carnivorePermitApplication.setAreaSize(0);
        return carnivorePermitApplication;
    }

    private Long id;

    @NotNull
    @JoinColumn(unique = true, nullable = false)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private HarvestPermitApplication harvestPermitApplication;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String additionalJustificationInfo;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String alternativeMeasures;

    @Column
    @Min(0)
    private Integer areaSize;

    @Valid
    @Embedded
    private GeoLocation geoLocation;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String areaDescription;

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

    public HarvestPermitApplication getHarvestPermitApplication() {
        return harvestPermitApplication;
    }

    public void setHarvestPermitApplication(final HarvestPermitApplication harvestPermitApplication) {
        this.harvestPermitApplication = harvestPermitApplication;
    }

    public String getAdditionalJustificationInfo() {
        return additionalJustificationInfo;
    }

    public void setAdditionalJustificationInfo(final String additionalJustificationInfo) {
        this.additionalJustificationInfo = additionalJustificationInfo;
    }

    public String getAlternativeMeasures() {
        return alternativeMeasures;
    }

    public void setAlternativeMeasures(final String alternativeMeasures) {
        this.alternativeMeasures = alternativeMeasures;
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
}
