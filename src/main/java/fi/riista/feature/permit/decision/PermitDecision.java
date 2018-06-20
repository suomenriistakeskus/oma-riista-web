package fi.riista.feature.permit.decision;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.species.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.decision.action.PermitDecisionAction;
import fi.riista.feature.permit.decision.attachment.PermitDecisionAttachment;
import fi.riista.feature.permit.decision.authority.PermitDecisionAuthority;
import fi.riista.feature.permit.decision.delivery.PermitDecisionDelivery;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmount;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.LocalisedString;
import org.joda.time.DateTime;

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
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

@Entity
@Access(AccessType.FIELD)
public class PermitDecision extends LifecycleEntity<Long> {

    // TODO: Remove hard-coded value
    public static final BigDecimal DECISION_PRICE_MOOSELIKE = new BigDecimal("90.00");

    private static final LocalisedString FILENAME_PREFIX =
            new LocalisedString("Päätös", "Beslut");

    public static String getFileName(final Locale locale, final String permitNumber) {
        return String.format("%s-%s.pdf", FILENAME_PREFIX.getAnyTranslation(locale), permitNumber);
    }

    public enum Status {
        // Kesken
        DRAFT,

        // Valmis ja esittelyssä
        COMPLETE,

        // Lukittu ja lähtenyt
        LOCKED,

        // Julkaistu
        PUBLISHED,

        // Oikeudess kumottu
        REVOKED,

        // Tutkimatta jätetty
        IGNORED;
    }

    public enum GrantStatus {
        // Myönnetään kuten anottu
        UNCHANGED,
        // Myönnetään mutta muutoksin
        RESTRICTED,
        // Hylätty
        REJECTED
    }

    public static final String ID_COLUMN_NAME = "permit_decision_id";

    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.DRAFT;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GrantStatus grantStatus = GrantStatus.UNCHANGED;

    @JoinColumn
    @ManyToOne(fetch = FetchType.LAZY)
    private Riistanhoitoyhdistys rhy;

    @JoinColumn
    @ManyToOne(fetch = FetchType.LAZY)
    private GISHirvitalousalue hta;

    @JoinColumn(unique = true)
    @OneToOne(fetch = FetchType.LAZY)
    private HarvestPermitApplication application;

    @ManyToOne(fetch = FetchType.LAZY)
    private SystemUser handler;

    @JoinColumn
    @ManyToOne(fetch = FetchType.LAZY)
    private PermitDecision originalDecision;

    // Yhteyshenkilö
    @ManyToOne(fetch = FetchType.LAZY)
    private Person contactPerson;

    // Luvansaaja
    @ManyToOne(fetch = FetchType.LAZY)
    private HuntingClub permitHolder;

    // Jos päätös on lukitussa tilassa, niin tähän tallennetaa uusimman revision lukitushetki.
    @Column
    private DateTime lockedDate;

    // Ajanhetki jolloin päätöksen on määrä tulla julkiseksi.
    // Ei päätellä automaattisesti vaan asetetaan päätöstä laatiessa.
    @Column
    private DateTime publishDate;

    @Column
    private BigDecimal paymentAmount;

    @NotNull
    @Column(name = "locale_id", nullable = false)
    private Locale locale;

    // Osa-alueet

    @Valid
    @Embedded
    @NotNull
    private PermitDecisionDocument document = new PermitDecisionDocument();

    @Valid
    @Embedded
    @NotNull
    private PermitDecisionCompleteStatus completeStatus = new PermitDecisionCompleteStatus();

    @OneToMany(mappedBy = "permitDecision")
    private List<PermitDecisionSpeciesAmount> speciesAmounts = new LinkedList<>();

    @OneToMany(mappedBy = "permitDecision")
    private List<PermitDecisionAttachment> attachments = new LinkedList<>();

    // Referenssipäätös
    @ManyToOne(fetch = FetchType.LAZY)
    private PermitDecision reference;

    // Välitoimenpiteet
    @OneToMany(mappedBy = "permitDecision")
    private List<PermitDecisionAction> actions = new LinkedList<>();

    @OneToMany(mappedBy = "permitDecision")
    private List<PermitDecisionDelivery> delivery = new LinkedList<>();

    @JoinColumn(unique = true)
    @OneToOne(fetch = FetchType.LAZY)
    private PermitDecisionAuthority presenter;

    @JoinColumn(unique = true)
    @OneToOne(fetch = FetchType.LAZY)
    private PermitDecisionAuthority decisionMaker;

    @Transient
    public void assertStatus(final Status allowed) {
        assertStatus(EnumSet.of(allowed));
    }

    @Transient
    public void assertStatus(final EnumSet<Status> allowed) {
        if (!allowed.contains(this.status)) {
            throw new IllegalStateException(
                    String.format("status should be %s was %s", allowed, this.status));
        }
    }

    @Transient
    public void setStatusDraft() {
        assertStatus(EnumSet.of(Status.LOCKED, Status.PUBLISHED));

        this.status = Status.DRAFT;
    }

    @Transient
    public void setStatusPublished() {
        assertStatus(EnumSet.of(Status.LOCKED));

        this.status = Status.PUBLISHED;
    }

    @Transient
    public void setStatusLocked() {
        assertStatus(EnumSet.of(Status.DRAFT));

        if (grantStatus != GrantStatus.REJECTED && !getCompleteStatus().allComplete()) {
            throw new IllegalStateException("All decision sections must be complete");
        }
        if (grantStatus == GrantStatus.REJECTED && !getCompleteStatus().allCompleteForRejected()) {
            throw new IllegalStateException("All decision sections must be complete");
        }

        this.status = Status.LOCKED;
        this.lockedDate = DateUtil.now();
    }

    @Transient
    public List<PermitDecisionAttachment> getSortedAttachments() {
        return this.attachments == null ? emptyList() : this.attachments.stream()
                .sorted(PermitDecisionAttachment.ATTACHMENT_COMPARATOR)
                .collect(Collectors.toList());
    }

    @Transient
    public void assertHandler(final SystemUser currentUser) {
        if (handler == null) {
            throw new IllegalStateException("Handler is null");
        }
        if (!isHandler(currentUser)) {
            throw new IllegalStateException("Handler is not same as current user");
        }
    }

    @Transient
    public boolean isHandler(final @Nonnull SystemUser currentUser) {
        return Objects.equals(F.getId(handler), F.getId(currentUser));
    }

    @Transient
    public boolean isDraft() {
        return this.status == Status.DRAFT;
    }

    public void updateGrantStatus() {
        final Map<Integer, Float> applicationSpeciesCodeToAmount = application.getSpeciesAmounts().stream()
                .collect(Collectors.toMap(a -> a.getGameSpecies().getOfficialCode(), HarvestPermitApplicationSpeciesAmount::getAmount));

        final Map<Integer, Float> decisionSpeciesCodeToAmount = speciesAmounts.stream()
                .collect(Collectors.toMap(a -> a.getGameSpecies().getOfficialCode(), PermitDecisionSpeciesAmount::getAmount));

        final boolean speciesAndAmountsEqual = applicationSpeciesCodeToAmount.equals(decisionSpeciesCodeToAmount);

        final double decisionAmountSum = speciesAmounts.stream().mapToDouble(PermitDecisionSpeciesAmount::getAmount).sum();
        final boolean decisionContainsAnyRestrictions = speciesAmounts.stream()
                .anyMatch(a -> a.getRestrictionType() != null);

        if (decisionAmountSum < 0.5) {
            grantStatus = GrantStatus.REJECTED;
        } else if (!speciesAndAmountsEqual || decisionContainsAnyRestrictions) {
            grantStatus = GrantStatus.RESTRICTED;
        } else {
            grantStatus = GrantStatus.UNCHANGED;
        }
    }

    @Transient
    public boolean isPaymentAmountPositive() {
        return paymentAmount != null && paymentAmount.compareTo(BigDecimal.ZERO) > 0;
    }

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
    public void setId(Long id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    private void setStatus(final Status status) {
        this.status = status;
    }

    public GrantStatus getGrantStatus() {
        return grantStatus;
    }

    public void setGrantStatus(final GrantStatus grantStatus) {
        this.grantStatus = grantStatus;
    }

    public Riistanhoitoyhdistys getRhy() {
        return rhy;
    }

    public void setRhy(final Riistanhoitoyhdistys rhy) {
        this.rhy = rhy;
    }

    public GISHirvitalousalue getHta() {
        return hta;
    }

    public void setHta(final GISHirvitalousalue hta) {
        this.hta = hta;
    }

    public HarvestPermitApplication getApplication() {
        return application;
    }

    public void setApplication(final HarvestPermitApplication application) {
        this.application = application;
    }

    public SystemUser getHandler() {
        return handler;
    }

    public void setHandler(final SystemUser handler) {
        this.handler = handler;
    }

    public PermitDecision getOriginalDecision() {
        return originalDecision;
    }

    public void setOriginalDecision(final PermitDecision originalDecision) {
        this.originalDecision = originalDecision;
    }

    public Person getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(final Person contactPerson) {
        this.contactPerson = contactPerson;
    }

    public HuntingClub getPermitHolder() {
        return permitHolder;
    }

    public void setPermitHolder(final HuntingClub permitHolder) {
        this.permitHolder = permitHolder;
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

    public BigDecimal getPaymentAmount() {
        return paymentAmount;
    }

    public void setPaymentAmount(final BigDecimal paymentAmount) {
        this.paymentAmount = paymentAmount;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(final Locale locale) {
        this.locale = locale;
    }

    public PermitDecisionDocument getDocument() {
        return document;
    }

    public void setDocument(final PermitDecisionDocument document) {
        this.document = document;
    }

    public PermitDecisionCompleteStatus getCompleteStatus() {
        return completeStatus;
    }

    public void setCompleteStatus(final PermitDecisionCompleteStatus complete) {
        this.completeStatus = complete;
    }

    public List<PermitDecisionSpeciesAmount> getSpeciesAmounts() {
        return speciesAmounts;
    }

    public void setSpeciesAmounts(final List<PermitDecisionSpeciesAmount> speciesAmounts) {
        this.speciesAmounts = speciesAmounts;
    }

    public List<PermitDecisionAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(final List<PermitDecisionAttachment> attachments) {
        this.attachments = attachments;
    }

    public PermitDecision getReference() {
        return reference;
    }

    public void setReference(final PermitDecision reference) {
        this.reference = reference;
    }

    public List<PermitDecisionAction> getActions() {
        return actions;
    }

    public void setActions(final List<PermitDecisionAction> intermediateActions) {
        this.actions = intermediateActions;
    }

    public List<PermitDecisionDelivery> getDelivery() {
        return delivery;
    }

    public void setDelivery(final List<PermitDecisionDelivery> delivery) {
        this.delivery = delivery;
    }

    public PermitDecisionAuthority getPresenter() {
        return presenter;
    }

    public void setPresenter(final PermitDecisionAuthority presenter) {
        this.presenter = presenter;
    }

    public PermitDecisionAuthority getDecisionMaker() {
        return decisionMaker;
    }

    public void setDecisionMaker(final PermitDecisionAuthority decisionMaker) {
        this.decisionMaker = decisionMaker;
    }
}
