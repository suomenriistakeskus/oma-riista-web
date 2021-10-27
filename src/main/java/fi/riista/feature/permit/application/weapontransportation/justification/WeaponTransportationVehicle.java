package fi.riista.feature.permit.application.weapontransportation.justification;

import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.permit.application.weapontransportation.WeaponTransportationPermitApplication;
import fi.riista.feature.permit.application.weapontransportation.WeaponTransportationPermitApplication_;
import fi.riista.util.jpa.CriteriaUtils;
import org.hibernate.validator.constraints.SafeHtml;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
@Access(AccessType.FIELD)
public class WeaponTransportationVehicle extends BaseEntity<Long> {

    public static final String ID_COLUMN_NAME = "weapon_transportation_vehicle_id";

    private Long id;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private WeaponTransportationPermitApplication weaponTransportationPermitApplication;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private WeaponTransportationVehicleType type;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "text")
    private String description;

    public WeaponTransportationVehicle() {}

    public WeaponTransportationVehicle(final WeaponTransportationPermitApplication weaponTransportationPermitApplication,
                                       final WeaponTransportationVehicleType type,
                                       final String description) {
        this.weaponTransportationPermitApplication = weaponTransportationPermitApplication;
        this.type = type;
        this.description = description;
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

    public WeaponTransportationPermitApplication getWeaponTransportationPermitApplication() {
        return weaponTransportationPermitApplication;
    }

    public void setWeaponTransportationPermitApplication(final WeaponTransportationPermitApplication weaponTransportationPermitApplication) {
        CriteriaUtils.updateInverseCollection(WeaponTransportationPermitApplication_.vehicles,
                this,
                this.weaponTransportationPermitApplication,
                weaponTransportationPermitApplication);
        this.weaponTransportationPermitApplication = weaponTransportationPermitApplication;
    }

    public WeaponTransportationVehicleType getType() {
        return type;
    }

    public void setType(final WeaponTransportationVehicleType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}
