package fi.riista.feature.permit.decision.revision;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.common.decision.AppealStatus;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.PermitDecisionDocument;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
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
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Entity
@Access(AccessType.FIELD)
public class PermitDecisionRevision extends LifecycleEntity<Long> {

    private Long id;

    @Size(min = 8, max = 255)
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

    // Paperipostitettava vai ei
    @Column(nullable = false)
    private boolean postalByMail;

    // Paperipostituksen päivämäärä
    @Column
    private DateTime postedByMailDate;

    // Moderaattorin nimi joka postitti
    @Size(max = 255)
    @Column
    private String postedByMailUsername;

    // Julkaisu peruutettu
    @Column(nullable = false)
    private boolean cancelled;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermitDecision.DecisionType decisionType;

    @Enumerated(EnumType.STRING)
    @Column
    private AppealStatus appealStatus;

    @Valid
    @Embedded
    @NotNull
    private PermitDecisionDocument document = new PermitDecisionDocument();

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false)
    private PermitDecision permitDecision;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(nullable = false, unique = true)
    private PersistentFileMetadata pdfMetadata;

    // Publically released version without contact person's information, used with carnivore permits
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(unique = true)
    private PersistentFileMetadata publicPdfMetadata;

    @OneToMany(mappedBy = "decisionRevision")
    private List<PermitDecisionRevisionAttachment> attachments = new LinkedList<>();

    @OneToMany(mappedBy = "decisionRevision")
    private List<PermitDecisionRevisionReceiver> receivers = new LinkedList<>();

    @Transient
    public List<PermitDecisionRevisionAttachment> getSortedAttachments() {
        return this.attachments == null ? emptyList() : this.attachments.stream()
                .sorted(PermitDecisionRevisionAttachment.ATTACHMENT_COMPARATOR)
                .collect(Collectors.toList());
    }

    // Accessors -->

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permit_decision_revision_id", nullable = false)
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

    public boolean isPostalByMail() {
        return postalByMail;
    }

    public void setPostalByMail(final boolean postalByMail) {
        this.postalByMail = postalByMail;
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

    public PermitDecision.DecisionType getDecisionType() {
        return decisionType;
    }

    public void setDecisionType(final PermitDecision.DecisionType decisionType) {
        this.decisionType = decisionType;
    }

    public AppealStatus getAppealStatus() {
        return appealStatus;
    }

    public void setAppealStatus(final AppealStatus appealStatus) {
        this.appealStatus = appealStatus;
    }

    public PermitDecisionDocument getDocument() {
        return document;
    }

    public void setDocument(final PermitDecisionDocument document) {
        this.document = document;
    }

    public PermitDecision getPermitDecision() {
        return permitDecision;
    }

    public void setPermitDecision(final PermitDecision permitDecision) {
        this.permitDecision = permitDecision;
    }

    public PersistentFileMetadata getPdfMetadata() {
        return pdfMetadata;
    }

    public void setPdfMetadata(final PersistentFileMetadata pdfMetadata) {
        this.pdfMetadata = pdfMetadata;
    }

    public PersistentFileMetadata getPublicPdfMetadata() {
        return publicPdfMetadata;
    }

    public void setPublicPdfMetadata(final PersistentFileMetadata blankedPdfMetadata) {
        this.publicPdfMetadata = blankedPdfMetadata;
    }

    public List<PermitDecisionRevisionAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(final List<PermitDecisionRevisionAttachment> attachments) {
        this.attachments = attachments;
    }

    public List<PermitDecisionRevisionReceiver> getReceivers() {
        return receivers;
    }

    public void setReceivers(final List<PermitDecisionRevisionReceiver> receivers) {
        this.receivers = receivers;
    }
}
