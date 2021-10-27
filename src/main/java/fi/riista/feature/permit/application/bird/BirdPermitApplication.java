package fi.riista.feature.permit.application.bird;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.gamediary.GameCategory;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HasParentApplication;
import fi.riista.feature.permit.application.bird.area.BirdPermitApplicationProtectedArea;
import fi.riista.feature.permit.application.bird.cause.BirdPermitApplicationCause;
import fi.riista.feature.permit.application.derogation.area.DerogationPermitApplicationAreaInfo;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethods;
import org.hibernate.validator.constraints.SafeHtml;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.validation.Valid;

@Entity
@Access(AccessType.FIELD)
public class BirdPermitApplication extends HasParentApplication implements DerogationPermitApplicationAreaInfo {

    public static final String ID_COLUMN_NAME = "bird_permit_application_id";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = ID_COLUMN_NAME, nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Embedded
    @Valid
    private BirdPermitApplicationProtectedArea protectedArea;

    @Embedded
    @Valid
    private BirdPermitApplicationCause cause;

    @Embedded
    @Valid
    private DerogationPermitApplicationForbiddenMethods forbiddenMethods;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String areaDescription;

    public static BirdPermitApplication create(HarvestPermitApplication application) {
        BirdPermitApplication birdApplication = new BirdPermitApplication();
        birdApplication.setHarvestPermitApplication(application);
        birdApplication.setForbiddenMethods(new DerogationPermitApplicationForbiddenMethods());
        birdApplication.setCause(new BirdPermitApplicationCause());
        birdApplication.setProtectedArea(new BirdPermitApplicationProtectedArea());
        return birdApplication;
    }

    @Transient
    public boolean isLimitlessPermitAllowed() {
        return protectedArea != null && !protectedArea.getProtectedAreaType().equals(ProtectedAreaType.OTHER) &&
                cause != null && cause.hasOnlyCausesAllowingLimitlessPermit() &&
                hasOnlyUnprotectedSpecies();
    }

    @Transient
    public boolean hasOnlyUnprotectedSpecies() {
        return getHarvestPermitApplication().getSpeciesAmounts()
                .stream().allMatch(spa -> GameCategory.UNPROTECTED.equals(spa.getGameSpecies().getCategory()));
    }

    // ACCESSORS

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    private Long id;

    public BirdPermitApplicationProtectedArea getProtectedArea() {
        return protectedArea;
    }

    public void setProtectedArea(BirdPermitApplicationProtectedArea protectedArea) {
        this.protectedArea = protectedArea;
    }

    public BirdPermitApplicationCause getCause() {
        return cause;
    }

    public void setCause(BirdPermitApplicationCause cause) {
        this.cause = cause;
    }

    public DerogationPermitApplicationForbiddenMethods getForbiddenMethods() {
        return forbiddenMethods;
    }

    public void setForbiddenMethods(DerogationPermitApplicationForbiddenMethods forbiddenMethods) {
        this.forbiddenMethods = forbiddenMethods;
    }

    @Override
    public Integer getAreaSize() {
        return this.protectedArea.getProtectedAreaSize();
    }

    @Override
    public void setAreaSize(final Integer size) {
        this.protectedArea.setAreaSize(size);
    }

    @Override
    public GeoLocation getGeoLocation() {
        return this.protectedArea.getGeoLocation();
    }

    @Override
    public void setGeoLocation(final GeoLocation geoLocation) {
        this.protectedArea.setGeoLocation(geoLocation);
    }

    @Override
    public String getAreaDescription() {
        return this.areaDescription;
    }

    @Override
    public void setAreaDescription(final String areaDescription) {
        this.areaDescription = areaDescription;
    }
}
