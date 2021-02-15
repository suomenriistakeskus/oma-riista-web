package fi.riista.feature.permit.application.weapontransportation;

import fi.riista.feature.common.entity.GeoLocation;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.derogation.area.DerogationPermitApplicationAreaInfo;
import fi.riista.feature.permit.application.weapontransportation.justification.TransportedWeapon;
import fi.riista.feature.permit.application.weapontransportation.justification.WeaponTransportationVehicle;
import fi.riista.feature.permit.application.weapontransportation.reason.WeaponTransportationReasonType;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Entity
@Access(AccessType.FIELD)
public class WeaponTransportationPermitApplication extends LifecycleEntity<Long> implements DerogationPermitApplicationAreaInfo {

    public static final String ID_COLUMN_NAME = "weapon_transportation_permit_application_id";

    public static WeaponTransportationPermitApplication create(@Nonnull final HarvestPermitApplication application) {
        requireNonNull(application);
        checkArgument(application.getHarvestPermitCategory() == HarvestPermitCategory.WEAPON_TRANSPORTATION,
                "Application category must be weapon transportation");

        final WeaponTransportationPermitApplication weaponTransportationPermitApplication = new WeaponTransportationPermitApplication();
        weaponTransportationPermitApplication.setHarvestPermitApplication(application);
        return weaponTransportationPermitApplication;
    }

    private Long id;

    @NotNull
    @JoinColumn(unique = true, nullable = false)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private HarvestPermitApplication harvestPermitApplication;

    @Column
    @Enumerated(EnumType.STRING)
    private WeaponTransportationReasonType reasonType;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "text")
    private String reasonDescription;

    @Column
    private LocalDate beginDate;

    @Column
    private LocalDate endDate;

    @Column
    @Min(0)
    private Integer areaSize;

    @Valid
    @Embedded
    private GeoLocation geoLocation;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "text")
    private String areaDescription;

    @OneToMany(mappedBy = "weaponTransportationPermitApplication")
    private Set<WeaponTransportationVehicle> vehicles = new HashSet<>();

    @OneToMany(mappedBy = "weaponTransportationPermitApplication")
    private Set<TransportedWeapon> transportedWeapons = new HashSet<>();

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "text")
    private String justification;

    WeaponTransportationPermitApplication() {}

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

    public WeaponTransportationReasonType getReasonType() {
        return reasonType;
    }

    public void setReasonType(final WeaponTransportationReasonType reasonType) {
        this.reasonType = reasonType;
    }

    public String getReasonDescription() {
        return reasonDescription;
    }

    public void setReasonDescription(final String reasonDescription) {
        this.reasonDescription = reasonDescription;
    }

    public LocalDate getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(final LocalDate beginDate) {
        this.beginDate = beginDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(final LocalDate endDate) {
        this.endDate = endDate;
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

    public String getJustification() {
        return justification;
    }

    public void setJustification(final String justification) {
        this.justification = justification;
    }

    Set<TransportedWeapon> getTransportedWeapons() {
        return transportedWeapons;
    }

    Set<WeaponTransportationVehicle> getVehicles() {
        return vehicles;
    }
}
