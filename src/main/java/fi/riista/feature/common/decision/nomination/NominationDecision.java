package fi.riista.feature.common.decision.nomination;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.decision.AppealStatus;
import fi.riista.feature.common.decision.DecisionBase;
import fi.riista.feature.common.decision.DecisionStatus;
import fi.riista.feature.common.decision.nomination.action.NominationDecisionAction;
import fi.riista.feature.common.decision.nomination.attachment.NominationDecisionAttachment;
import fi.riista.feature.common.decision.nomination.authority.NominationDecisionAuthority;
import fi.riista.feature.common.decision.nomination.delivery.NominationDecisionDelivery;
import fi.riista.feature.common.decision.nomination.document.NominationDecisionCompleteStatus;
import fi.riista.feature.common.decision.nomination.document.NominationDecisionDocument;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.DocumentNumberUtil;
import fi.riista.feature.permit.application.DeliveryAddress;
import fi.riista.util.DateUtil;
import fi.riista.util.LocalisedEnum;
import org.joda.time.DateTime;
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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Entity
@Access(AccessType.FIELD)
public class NominationDecision extends LifecycleEntity<Long> implements DecisionBase {

    public static NominationDecision create(final int decisionNumber,
                                            @Nonnull final Riistanhoitoyhdistys rhy,
                                            @Nonnull final OccupationType occupationType,
                                            @Nonnull final NominationDecisionType decisionType,
                                            @Nonnull final Person contactPerson,
                                            @Nonnull final DeliveryAddress deliveryAddress,
                                            @Nonnull final Locale decisionLocale) {
        requireNonNull(rhy);
        requireNonNull(decisionType);
        requireNonNull(contactPerson);
        requireNonNull(deliveryAddress);
        requireNonNull(decisionLocale);
        checkArgument(occupationType.isJHTOccupation());

        final NominationDecision nominationDecision = new NominationDecision();
        nominationDecision.setDecisionNumber(decisionNumber);
        nominationDecision.setDecisionYear(DateUtil.currentYear());
        nominationDecision.setRhy(rhy);
        nominationDecision.setOccupationType(occupationType);
        nominationDecision.setDecisionType(decisionType);
        nominationDecision.setContactPerson(contactPerson);
        nominationDecision.setDeliveryAddress(deliveryAddress);
        nominationDecision.setLocale(decisionLocale);
        return nominationDecision;
    }

    public static final String ID_COLUMN_NAME = "nomination_decision_id";

    public String getDecisionName() {
        return NominationDecisionName.getDecisionName(decisionType).getTranslation(locale);
    }

    public enum NominationDecisionType implements LocalisedEnum {
        // Nimittäminen
        NOMINATION,

        // Nimityksen peruuttaminen
        NOMINATION_CANCELLATION
    }

    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DecisionStatus status = DecisionStatus.DRAFT;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NominationDecisionType decisionType;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OccupationType occupationType;

    @Column(nullable = false)
    private int decisionYear;

    @Column(nullable = false)
    private int decisionNumber;

    @Column
    private LocalDate proposalDate;

    @Enumerated(EnumType.STRING)
    @Column
    private AppealStatus appealStatus;

    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Riistanhoitoyhdistys rhy;

    @ManyToOne(fetch = FetchType.LAZY)
    private SystemUser handler;

    // Yhteyshenkilö
    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Person contactPerson;

    @NotNull
    @Embedded
    @Valid
    private DeliveryAddress deliveryAddress;

    // Jos päätös on lukitussa tilassa, niin tähän tallennetaan uusimman revision lukitushetki.
    @Column
    private DateTime lockedDate;

    // Ajanhetki jolloin päätöksen on määrä tulla julkiseksi.
    // Ei päätellä automaattisesti vaan asetetaan päätöstä laatiessa.
    @Column
    private DateTime publishDate;

    @NotNull
    @Column(name = "locale_id", nullable = false)
    private Locale locale;

    // Osa-alueet

    @Valid
    @Embedded
    @NotNull
    private NominationDecisionDocument document = new NominationDecisionDocument();

    @Valid
    @Embedded
    @NotNull
    private NominationDecisionCompleteStatus completeStatus = new NominationDecisionCompleteStatus();

    // Referenssipäätös
    @ManyToOne(fetch = FetchType.LAZY)
    private NominationDecision reference;

    // Välitoimenpiteet
    @OneToMany(mappedBy = "nominationDecision")
    private Set<NominationDecisionAction> actions = new HashSet<>();

    @JoinColumn(unique = true)
    @OneToOne(fetch = FetchType.LAZY)
    private NominationDecisionAuthority presenter;

    @JoinColumn(unique = true)
    @OneToOne(fetch = FetchType.LAZY)
    private NominationDecisionAuthority decisionMaker;

    @OneToMany(mappedBy = "nominationDecision")
    private Set<NominationDecisionAttachment> attachments = new HashSet<>();

    @OneToMany(mappedBy = "nominationDecision")
    private Set<NominationDecisionDelivery> delivery = new HashSet<>();

    @Nonnull
    public String createDocumentNumber() {
        return DocumentNumberUtil.createDocumentNumber(decisionYear, 1, decisionNumber);
    }

    @Override
    public void assertAllowedToLock() {
        if (!getCompleteStatus().allComplete()) {
            throw new IllegalStateException("All decision sections must be complete");
        }
    }

    /* package */ NominationDecision() {}

    // Accessors -->

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

    public DecisionStatus getStatus() {
        return status;
    }

    public void setStatus(final DecisionStatus status) {
        this.status = status;
    }

    public NominationDecisionType getDecisionType() {
        return decisionType;
    }

    public void setDecisionType(final NominationDecisionType decisionType) {
        this.decisionType = decisionType;
    }

    public OccupationType getOccupationType() {
        return occupationType;
    }

    public void setOccupationType(final OccupationType occupationType) {
        this.occupationType = occupationType;
    }

    public int getDecisionYear() {
        return decisionYear;
    }

    public void setDecisionYear(final int decisionYear) {
        this.decisionYear = decisionYear;
    }

    public LocalDate getProposalDate() {
        return proposalDate;
    }

    public void setProposalDate(final LocalDate proposalDate) {
        this.proposalDate = proposalDate;
    }

    public int getDecisionNumber() {
        return decisionNumber;
    }

    private void setDecisionNumber(final int decisionNumber) {
        this.decisionNumber = decisionNumber;
    }

    public AppealStatus getAppealStatus() {
        return appealStatus;
    }

    public void setAppealStatus(final AppealStatus appealStatus) {
        this.appealStatus = appealStatus;
    }

    public Riistanhoitoyhdistys getRhy() {
        return rhy;
    }

    public void setRhy(final Riistanhoitoyhdistys rhy) {
        this.rhy = rhy;
    }

    public SystemUser getHandler() {
        return handler;
    }

    public void setHandler(final SystemUser handler) {
        this.handler = handler;
    }

    public Person getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(final Person contactPerson) {
        this.contactPerson = contactPerson;
    }

    public DeliveryAddress getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(final DeliveryAddress deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public DateTime getLockedDate() {
        return lockedDate;
    }

    public void setLockedDate(final DateTime lockedDate) {
        this.lockedDate = lockedDate;
    }

    public DateTime getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(final DateTime publishDate) {
        this.publishDate = publishDate;
    }

    public NominationDecisionDocument getDocument() {
        return document;
    }

    public void setDocument(final NominationDecisionDocument document) {
        this.document = document;
    }

    public NominationDecisionCompleteStatus getCompleteStatus() {
        return completeStatus;
    }

    public void setCompleteStatus(final NominationDecisionCompleteStatus completeStatus) {
        this.completeStatus = completeStatus;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(final Locale locale) {
        this.locale = locale;
    }

    public NominationDecision getReference() {
        return reference;
    }

    public void setReference(final NominationDecision reference) {
        this.reference = reference;
    }

    /*package*/ Set<NominationDecisionAction> getActions() {
        return actions;
    }

    public NominationDecisionAuthority getPresenter() {
        return presenter;
    }

    public void setPresenter(final NominationDecisionAuthority presenter) {
        this.presenter = presenter;
    }

    public NominationDecisionAuthority getDecisionMaker() {
        return decisionMaker;
    }

    public void setDecisionMaker(final NominationDecisionAuthority decisionMaker) {
        this.decisionMaker = decisionMaker;
    }

    /*package*/ Set<NominationDecisionAttachment> getAttachments() {
        return attachments;
    }

    /*package*/ Set<NominationDecisionDelivery> getDelivery() {
        return delivery;
    }
}
