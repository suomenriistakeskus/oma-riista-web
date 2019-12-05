package fi.riista.feature.permit.application.mammal;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.derogation.area.DerogationPermitApplicationAreaInfo;
import fi.riista.feature.permit.application.derogation.forbidden.DerogationPermitApplicationForbiddenMethods;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
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
import javax.persistence.OneToOne;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Entity
@Access(AccessType.FIELD)
public class MammalPermitApplication extends LifecycleEntity<Long> implements DerogationPermitApplicationAreaInfo {

    public enum ExtendedPeriodGrounds {

        PERMANENT_ESTABLISHMENT, // Pysyvästi perustettu kohde
        PROTECTION_OF_FAUNA, // Eläimistön suojeleminen
        RESEARCH, // Tutkimusperuste
        NATURE_TREATMENT_PROJECT; // Luonnonhoitohanke
    }

    public static final String ID_COLUMN_NAME = "mammal_permit_application_id";

    public static MammalPermitApplication create(@Nonnull final HarvestPermitApplication application) {
        requireNonNull(application);
        checkArgument(application.getHarvestPermitCategory() == HarvestPermitCategory.MAMMAL,
                "Application category must be mammal");

        final MammalPermitApplication mammalPermitApplication = new MammalPermitApplication();
        mammalPermitApplication.setHarvestPermitApplication(application);
        mammalPermitApplication.setAreaSize(0);
        return mammalPermitApplication;
    }

    private Long id;

    @NotNull
    @JoinColumn(unique = true, nullable = false)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private HarvestPermitApplication harvestPermitApplication;

    @Column
    @Min(0)
    private Integer areaSize;

    @Valid
    @Embedded
    private GeoLocation geoLocation;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String areaDescription;

    @Enumerated(EnumType.STRING)
    private ExtendedPeriodGrounds extendedPeriodGrounds;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String extendedPeriodGroundsDescription;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String protectedAreaName;

    @Embedded
    @Valid
    private DerogationPermitApplicationForbiddenMethods forbiddenMethods;


    @AssertTrue
    public boolean isExtendedPeriodFieldsSetForMultiYearApplication() {
        return extendedPeriodGrounds == null && protectedAreaName == null ||
                extendedPeriodGrounds != null && protectedAreaName != null;
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

    public HarvestPermitApplication getHarvestPermitApplication() {
        return harvestPermitApplication;
    }

    public void setHarvestPermitApplication(final HarvestPermitApplication harvestPermitApplication) {
        this.harvestPermitApplication = harvestPermitApplication;
    }

    public Integer getAreaSize() {
        return areaSize;
    }

    public void setAreaSize(final Integer areaSize) {
        this.areaSize = areaSize;
    }

    public GeoLocation getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(final GeoLocation geoLocation) {
        this.geoLocation = geoLocation;
    }

    public String getAreaDescription() {
        return areaDescription;
    }

    public void setAreaDescription(final String areaDescription) {
        this.areaDescription = areaDescription;
    }

    public ExtendedPeriodGrounds getExtendedPeriodGrounds() {
        return extendedPeriodGrounds;
    }

    public void setExtendedPeriodGrounds(final ExtendedPeriodGrounds extendedPeriodGrounds) {
        this.extendedPeriodGrounds = extendedPeriodGrounds;
    }

    public String getExtendedPeriodGroundsDescription() {
        return extendedPeriodGroundsDescription;
    }

    public void setExtendedPeriodGroundsDescription(final String extendedPeriodGroundsDescription) {
        this.extendedPeriodGroundsDescription = extendedPeriodGroundsDescription;
    }

    public String getProtectedAreaName() {
        return protectedAreaName;
    }

    public void setProtectedAreaName(final String protectedAreaName) {
        this.protectedAreaName = protectedAreaName;
    }

    public DerogationPermitApplicationForbiddenMethods getForbiddenMethods() {
        return forbiddenMethods;
    }

    public void setForbiddenMethods(final DerogationPermitApplicationForbiddenMethods forbiddenMethods) {
        this.forbiddenMethods = forbiddenMethods;
    }
}
