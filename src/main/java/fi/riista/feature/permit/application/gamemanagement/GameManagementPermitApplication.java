package fi.riista.feature.permit.application.gamemanagement;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HasParentApplication;
import fi.riista.feature.permit.application.derogation.area.DerogationPermitApplicationAreaInfo;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethods;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Entity
@Access(AccessType.FIELD)
public class GameManagementPermitApplication extends HasParentApplication implements DerogationPermitApplicationAreaInfo {

    public static final String ID_COLUMN_NAME = "game_management_permit_application_id";

    private Long id;

    @Column
    @Min(1)
    private Integer areaSize;

    @Valid
    @Embedded
    private GeoLocation geoLocation;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String areaDescription;

    @Embedded
    @Valid
    private DerogationPermitApplicationForbiddenMethods forbiddenMethods;

    @Column(columnDefinition = "text")
    @Size(min = 5)
    private String justification;

    public GameManagementPermitApplication(@Nonnull final HarvestPermitApplication application) {
        requireNonNull(application);
        checkArgument(application.getHarvestPermitCategory() == HarvestPermitCategory.GAME_MANAGEMENT,
                "Application category must be game management");
        setHarvestPermitApplication(application);
    }

    public GameManagementPermitApplication() {}

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

    public DerogationPermitApplicationForbiddenMethods getForbiddenMethods() {
        return forbiddenMethods;
    }

    public void setForbiddenMethods(final DerogationPermitApplicationForbiddenMethods forbiddenMethods) {
        this.forbiddenMethods = forbiddenMethods;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(final String justification) {
        this.justification = justification;
    }
}
