package fi.riista.feature.permit.application.disability.justification;

import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.permit.application.PermitApplicationVehicleType;
import fi.riista.feature.permit.application.disability.DisabilityPermitApplication;
import org.hibernate.validator.constraints.SafeHtml;

import javax.annotation.Nonnull;
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

import static java.util.Objects.requireNonNull;

@Entity
@Access(AccessType.FIELD)
public class DisabilityPermitVehicle extends BaseEntity<Long> {

    public static final String ID_COLUMN_NAME = "disability_permit_vehicle_id";

    private Long id;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private DisabilityPermitApplication disabilityPermitApplication;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PermitApplicationVehicleType type;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "text")
    private String description;

    @NotNull
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "text", nullable = false)
    private String justification;

    public DisabilityPermitVehicle() {}

    public DisabilityPermitVehicle(final @Nonnull DisabilityPermitApplication disabilityPermitApplication,
                                   final PermitApplicationVehicleType type,
                                   final String description,
                                   final String justification) {
        requireNonNull(disabilityPermitApplication);

        this.disabilityPermitApplication = disabilityPermitApplication;
        this.type = type;
        this.description = description;
        this.justification = justification;
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

    public DisabilityPermitApplication getDisabilityPermitApplication() {
        return disabilityPermitApplication;
    }

    public void setDisabilityPermitApplication(final DisabilityPermitApplication disabilityPermitApplication) {
        this.disabilityPermitApplication = disabilityPermitApplication;
    }

    public PermitApplicationVehicleType getType() {
        return type;
    }

    public void setType(final PermitApplicationVehicleType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(final String justification) {
        this.justification = justification;
    }
}
