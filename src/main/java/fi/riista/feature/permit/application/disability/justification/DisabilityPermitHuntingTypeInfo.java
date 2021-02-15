package fi.riista.feature.permit.application.disability.justification;

import fi.riista.feature.common.entity.BaseEntity;
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
public class DisabilityPermitHuntingTypeInfo extends BaseEntity<Long> {

    public static final String ID_COLUMN_NAME = "disability_permit_hunting_type_info_id";

    private Long id;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private DisabilityPermitApplication disabilityPermitApplication;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private HuntingType huntingType;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(columnDefinition = "text")
    private String huntingTypeDescription;

    public DisabilityPermitHuntingTypeInfo() {}

    public DisabilityPermitHuntingTypeInfo(final @Nonnull DisabilityPermitApplication disabilityPermitApplication,
                                           final HuntingType huntingType,
                                           final String huntingTypeDescription) {
        requireNonNull(disabilityPermitApplication);

        this.disabilityPermitApplication = disabilityPermitApplication;
        this.huntingType = huntingType;
        this.huntingTypeDescription = huntingTypeDescription;
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

    public HuntingType getHuntingType() {
        return huntingType;
    }

    public void setHuntingType(final HuntingType huntingType) {
        this.huntingType = huntingType;
    }

    public String getHuntingTypeDescription() {
        return huntingTypeDescription;
    }

    public void setHuntingTypeDescription(final String huntingTypeDescription) {
        this.huntingTypeDescription = huntingTypeDescription;
    }
}
