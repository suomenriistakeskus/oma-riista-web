package fi.riista.feature.permit.application.disability;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Entity
@Access(AccessType.FIELD)
public class DisabilityPermitApplication extends LifecycleEntity<Long> {

    public static final String ID_COLUMN_NAME = "disability_permit_application_id";

    public static DisabilityPermitApplication create(@Nonnull final HarvestPermitApplication application) {
        requireNonNull(application);
        checkArgument(application.getHarvestPermitCategory() == HarvestPermitCategory.DISABILITY,
                "Application category must be disability");

        final DisabilityPermitApplication disabilityPermitApplication = new DisabilityPermitApplication();
        disabilityPermitApplication.setHarvestPermitApplication(application);
        return disabilityPermitApplication;
    }

    private Long id;

    @NotNull
    @JoinColumn(unique = true, nullable = false)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private HarvestPermitApplication harvestPermitApplication;

    private boolean useMotorVehicle;

    private boolean useVehicleForWeaponTransport;

    private LocalDate beginDate;
    private LocalDate endDate;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "TEXT")
    private String justification;

    DisabilityPermitApplication() {}

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

    public boolean getUseMotorVehicle() {
        return useMotorVehicle;
    }

    public void setUseMotorVehicle(final boolean useMotorVehicle) {
        this.useMotorVehicle = useMotorVehicle;
    }

    public boolean getUseVehicleForWeaponTransport() {
        return useVehicleForWeaponTransport;
    }

    public void setUseVehicleForWeaponTransport(final boolean useVehicleForWeaponTransport) {
        this.useVehicleForWeaponTransport = useVehicleForWeaponTransport;
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

    public String getJustification() {
        return justification;
    }

    public void setJustification(final String justification) {
        this.justification = justification;
    }
}
