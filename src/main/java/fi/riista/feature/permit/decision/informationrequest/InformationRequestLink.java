package fi.riista.feature.permit.decision.informationrequest;

import fi.riista.feature.common.entity.BaseEntity;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.harvestpermit.nestremoval.HarvestPermitNestLocationType;
import fi.riista.feature.permit.decision.PermitDecision;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.LocalDateTime;

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
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static java.util.Objects.requireNonNull;
import org.joda.time.DateTime;

@Entity
@Access(value = AccessType.FIELD)
public class InformationRequestLink extends LifecycleEntity<Long> {

    public static final String ID_COLUMN_NAME = "information_request_link_id";


    // Factories
    public static InformationRequestLink create(@Nonnull final String linkIdentifier,
                                                @Nonnull final PermitDecision permitDecision,
                                                @Nonnull final String recipientEmail,
                                                @Nonnull final String recipientName,
                                                @Nonnull final InformationRequestLinkType type,
                                                @Nonnull final DateTime validUntil,
                                                final String title,
                                                final String description) {
        requireNonNull(linkIdentifier);
        requireNonNull(permitDecision);
        requireNonNull(recipientEmail);
        requireNonNull(recipientName);
        requireNonNull(type);
        requireNonNull(validUntil);

        final InformationRequestLink entity = new InformationRequestLink();
        entity.setLinkIdentifier(linkIdentifier);
        entity.setPermitDecision(permitDecision);
        entity.setRecipientEmail(recipientEmail);
        entity.setRecipientName(recipientName);
        entity.setInformationRequestLinkType(type);
        entity.setValidUntil(validUntil);
        entity.setTitle(title);
        entity.setDescription(description);

        return entity;
    }

    // Attributes

    private Long id;


    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private PermitDecision permitDecision;

    @NotNull
    @Column(nullable = false)
    @Size(max = 255)
    private String linkIdentifier;

    @NotNull
    @Column(nullable = false)
    @Size(max = 255)
    private String recipientEmail;

    @NotNull
    @Column(nullable = false)
    @Size(max = 255)
    private String recipientName;

    @Column
    @Size(max = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Column(nullable = false)
    private DateTime validUntil;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InformationRequestLinkType informationRequestLinkType;

    // Methods

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

    public String getLinkIdentifier() {
        return linkIdentifier;
    }

    public void setLinkIdentifier(final String linkIdentifier) {
        this.linkIdentifier = linkIdentifier;
    }

    public PermitDecision getPermitDecision() {
        return permitDecision;
    }

    public void setPermitDecision(final PermitDecision permitDecision) {
        this.permitDecision = permitDecision;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(final String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(final String recipientName) {
        this.recipientName = recipientName;
    }

    public DateTime getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(final DateTime validUntil) {
        this.validUntil = validUntil;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public InformationRequestLinkType getInformationRequestLinkType() {
        return informationRequestLinkType;
    }

    public void setInformationRequestLinkType(final InformationRequestLinkType informationRequestLinkType) {
        this.informationRequestLinkType = informationRequestLinkType;
    }
}
