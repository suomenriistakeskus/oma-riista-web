package fi.riista.feature.common.decision.nomination.revision;

import fi.riista.feature.common.decision.nomination.NominationDecision;
import fi.riista.feature.common.decision.nomination.document.NominationDecisionDocument;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.DateTime;

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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Entity
@Access(AccessType.FIELD)
public class NominationDecisionRevision extends LifecycleEntity<Long> {

    private Long id;

    @Size(min = 8, max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column
    private String externalId;

    // Lukituspäivä
    @NotNull
    @Column(nullable = false)
    private DateTime lockedDate;

    // Suunniteltu julkaisupäivä
    @NotNull
    @Column(nullable = false)
    private DateTime scheduledPublishDate;

    // Julkaisupäivä
    @Column
    private DateTime publishDate;

    // Paperipostituksen päivämäärä
    @Column
    private DateTime postedByMailDate;

    // Moderaattorin nimi joka postitti
    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column
    private String postedByMailUsername;

    // Julkaisu peruutettu
    @Column(nullable = false)
    private boolean cancelled;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NominationDecision.NominationDecisionType decisionType;

    @Valid
    @Embedded
    @NotNull
    private NominationDecisionDocument document = new NominationDecisionDocument();

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private NominationDecision nominationDecision;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false, unique = true)
    private PersistentFileMetadata pdfMetadata;

    @OneToMany(mappedBy = "decisionRevision")
    private Set<NominationDecisionRevisionAttachment> attachments = new HashSet<>();

    @OneToMany(mappedBy = "decisionRevision")
    private Set<NominationDecisionRevisionReceiver> receivers = new HashSet<>();

    // Accessors -->

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nomination_decision_revision_id", nullable = false)
    @Access(value = AccessType.PROPERTY)
    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(final String externalId) {
        this.externalId = externalId;
    }

    public DateTime getLockedDate() {
        return lockedDate;
    }

    public void setLockedDate(final DateTime lockedDate) {
        this.lockedDate = lockedDate;
    }

    public DateTime getScheduledPublishDate() {
        return scheduledPublishDate;
    }

    public void setScheduledPublishDate(final DateTime scheduledPublishDate) {
        this.scheduledPublishDate = scheduledPublishDate;
    }

    public DateTime getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(final DateTime publishDate) {
        this.publishDate = publishDate;
    }

    public DateTime getPostedByMailDate() {
        return postedByMailDate;
    }

    public void setPostedByMailDate(final DateTime postedByMailDate) {
        this.postedByMailDate = postedByMailDate;
    }

    public String getPostedByMailUsername() {
        return postedByMailUsername;
    }

    public void setPostedByMailUsername(final String postedByMailUsername) {
        this.postedByMailUsername = postedByMailUsername;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(final boolean cancelled) {
        this.cancelled = cancelled;
    }

    public NominationDecision.NominationDecisionType getDecisionType() {
        return decisionType;
    }

    public void setDecisionType(final NominationDecision.NominationDecisionType decisionType) {
        this.decisionType = decisionType;
    }

    public NominationDecisionDocument getDocument() {
        return document;
    }

    public void setDocument(final NominationDecisionDocument document) {
        this.document = document;
    }

    public NominationDecision getNominationDecision() {
        return nominationDecision;
    }

    public void setNominationDecision(final NominationDecision nominationDecision) {
        this.nominationDecision = nominationDecision;
    }

    public PersistentFileMetadata getPdfMetadata() {
        return pdfMetadata;
    }

    public void setPdfMetadata(final PersistentFileMetadata pdfMetadata) {
        this.pdfMetadata = pdfMetadata;
    }

    /*package*/ Set<NominationDecisionRevisionAttachment> getAttachments() {
        return attachments;
    }

    /*package*/ Set<NominationDecisionRevisionReceiver> getReceivers() {
        return receivers;
    }
}
