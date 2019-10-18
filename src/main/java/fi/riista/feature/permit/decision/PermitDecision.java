package fi.riista.feature.permit.decision;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gis.hta.GISHirvitalousalue;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.PermitNumberUtil;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.application.DeliveryAddress;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.application.PermitHolder;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.decision.action.PermitDecisionAction;
import fi.riista.feature.permit.decision.attachment.PermitDecisionAttachment;
import fi.riista.feature.permit.decision.authority.PermitDecisionAuthority;
import fi.riista.feature.permit.decision.delivery.PermitDecisionDelivery;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.LocalisedString;
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
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

@Entity
@Access(AccessType.FIELD)
public class PermitDecision extends LifecycleEntity<Long> {

    private static final LocalisedString FILENAME_PREFIX =
            new LocalisedString("Päätös", "Beslut");

    public static String getFileName(final Locale locale, final String permitNumber) {
        return String.format("%s-%s.pdf", FILENAME_PREFIX.getAnyTranslation(locale), permitNumber);
    }

    public static int getDecisionYear(final int applicationHuntingYear,
                                      final @Nonnull List<HarvestPermitApplicationSpeciesAmount> speciesAmounts) {
        return requireNonNull(speciesAmounts).stream()
                .map(HarvestPermitApplicationSpeciesAmount::getBeginDate)
                .filter(Objects::nonNull)
                .mapToInt(LocalDate::getYear)
                .min()
                .orElse(applicationHuntingYear);
    }

    @Nonnull
    public static PermitDecision createForApplication(final @Nonnull HarvestPermitApplication application) {
        requireNonNull(application);

        final DecisionType decisionType = DecisionType.HARVEST_PERMIT;
        final String permitTypeCode = PermitTypeCode.getPermitTypeCode(
                application.getHarvestPermitCategory(), application.getValidityYears());
        final BigDecimal paymentAmount = PermitDecisionPaymentAmount.getDefaultPaymentAmount(
                decisionType, application.getHarvestPermitCategory());
        final GISHirvitalousalue hta = Optional.ofNullable(application.getArea())
                .flatMap(HarvestPermitArea::findLargestHta)
                .orElse(null);

        final int decisionYear = getDecisionYear(application.getApplicationYear(), application.getSpeciesAmounts());

        // Override from 0 to 1 for annually renewed decisions in order to have sane decision and
        // permit numbers.
        final int validityYears = Optional.ofNullable(application.getValidityYears())
                .filter(years -> years > 0)
                .orElse(1);
        final int decisionNumber = requireNonNull(application.getApplicationNumber());

        final PermitDecision decision = new PermitDecision(
                decisionType,
                decisionYear,
                validityYears,
                decisionNumber,
                permitTypeCode,
                application.getRhy(),
                application,
                application.getContactPerson(),
                application.getPermitHolder(),
                application.getDecisionLocale());

        decision.setDeliveryAddress(application.getDeliveryAddress());
        decision.setHuntingClub(application.getHuntingClub());
        decision.setHta(hta);
        decision.setPaymentAmount(paymentAmount);

        return decision;
    }

    public static void amendFromApplication(final @Nonnull PermitDecision decision) {
        requireNonNull(decision);

        // Create clean reference
        final PermitDecision ref = createForApplication(decision.getApplication());

        // Only "safe" fields should be updated here
        decision.setDecisionYear(ref.getDecisionYear());
        decision.setPermitTypeCode(ref.getPermitTypeCode());
        decision.setHta(ref.getHta());
        decision.setRhy(ref.getRhy());
        decision.setValidityYears(ref.getValidityYears());
        decision.setContactPerson(ref.getContactPerson());
        decision.setPermitHolder(ref.getPermitHolder());
        decision.setLocale(ref.getLocale());
        decision.setDeliveryAddress(ref.getDeliveryAddress());
        decision.setHuntingClub(ref.getHuntingClub());
    }

    public enum Status {
        // Kesken
        DRAFT,

        // Lukittu
        LOCKED,

        // Julkaistu
        PUBLISHED
    }

    public enum DecisionType {
        // Pyyntilupa
        HARVEST_PERMIT,

        // Päätös hakemuksen peruuttamiseen
        CANCEL_APPLICATION,

        // Hakemuksen tutkimatta jättäminen
        IGNORE_APPLICATION
    }

    public enum GrantStatus {
        // Myönnetään kuten anottu
        UNCHANGED,
        // Myönnetään mutta muutoksin
        RESTRICTED,
        // Hylätty
        REJECTED
    }

    public enum AppealStatus {
        // Päätöksestä valitettu
        INITIATED,

        // Valitus jätetty käsittelemättä
        IGNORED,

        // Oikeuden ratkaisu, Ei muutosta
        UNCHANGED,

        // Oikeuden ratkaisu, Päätös kumottu,
        REPEALED,

        // Oikeuden ratkaisu, Päätös osittain kumottu
        PARTIALLY_REPEALED,

        // Oikeuden ratkaisu, Palautettu uudelleen käsiteltäväksi
        RETREATMENT
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
    private DecisionType decisionType;

    @NotNull
    @Size(min = 3, max = 3)
    @Column(length = 3, nullable = false)
    private String permitTypeCode;

    // Components of permit number

    @Column(nullable = false)
    private int decisionYear;

    // For decisions, minimum validity years is 1, even for
    // annually renewed permits that have validity period 0 in applications.
    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private int validityYears;

    @Column(nullable = false)
    private int decisionNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GrantStatus grantStatus = GrantStatus.UNCHANGED;

    @Enumerated(EnumType.STRING)
    @Column
    private AppealStatus appealStatus;

    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Riistanhoitoyhdistys rhy;

    @JoinColumn
    @ManyToOne(fetch = FetchType.LAZY)
    private GISHirvitalousalue hta;

    @NotNull
    @JoinColumn(nullable = false, unique = true)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    private HarvestPermitApplication application;

    @ManyToOne(fetch = FetchType.LAZY)
    private SystemUser handler;

    @JoinColumn
    @ManyToOne(fetch = FetchType.LAZY)
    private PermitDecision originalDecision;

    // Yhteyshenkilö
    @NotNull
    @JoinColumn(nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Person contactPerson;

    @NotNull
    @Embedded
    @Valid
    private DeliveryAddress deliveryAddress;

    // Luvansaaja
    @NotNull
    @Embedded
    @Valid
    private PermitHolder permitHolder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permit_holder_id")
    private HuntingClub huntingClub;

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

    // moottorikäyttöisten kulkuneuvojen käytön rajoituksista
    @Column(name = "legal_section_32", nullable = false)
    private boolean legalSection32;

    // pyyntivälineitä ja pyyntimenetelmiä koskevista kielloista
    @Column(name = "legal_section_33", nullable = false)
    private boolean legalSection33;

    // valtioneuvoston asetuksen kielloista
    @Column(name = "legal_section_34", nullable = false)
    private boolean legalSection34;

    // metsästysaseen kuljettamista koskevista säännöksistä
    @Column(name = "legal_section_35", nullable = false)
    private boolean legalSection35;

    // koiran kiinnipitovelvollisuudesta
    @Column(name = "legal_section_51", nullable = false)
    private boolean legalSection51;

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

    public PermitDecision(final @Nonnull DecisionType decisionType,
                          final int decisionYear,
                          final int validityYears,
                          final int decisionNumber,
                          final @Nonnull String permitTypeCode,
                          final @Nonnull Riistanhoitoyhdistys rhy,
                          final @Nonnull HarvestPermitApplication application,
                          final @Nonnull Person contactPerson,
                          final @Nonnull PermitHolder permitHolder,
                          final @Nonnull Locale locale) {
        this.decisionType = requireNonNull(decisionType);
        this.decisionYear = decisionYear;
        this.validityYears = validityYears;
        this.decisionNumber = decisionNumber;
        this.permitTypeCode = requireNonNull(permitTypeCode);
        this.rhy = requireNonNull(rhy);
        this.application = requireNonNull(application);
        this.contactPerson = requireNonNull(contactPerson);
        this.permitHolder = requireNonNull(permitHolder);
        this.locale = requireNonNull(locale);
    }

    // For Hibernate
    PermitDecision() {
    }

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
    public void assertDecisionType(final DecisionType allowed) {
        assertDecisionType(EnumSet.of(allowed));
    }

    @Transient
    public void assertDecisionType(final EnumSet<DecisionType> allowed) {
        if (!allowed.contains(this.decisionType)) {
            throw new IllegalStateException(
                    String.format("decisionType should be %s was %s", allowed, this.decisionType));
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

    @Nonnull
    @Transient
    public String getDecisionName() {
        requireNonNull(application);
        requireNonNull(decisionType);
        requireNonNull(locale);

        return PermitDecisionName.getDecisionName(decisionType, application.getHarvestPermitCategory()).getTranslation(locale);
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

    @Transient
    public boolean isPaymentAmountPositive() {
        return paymentAmount != null && paymentAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    @Transient
    public boolean isAnnualUnprotectedBird() {
        return PermitTypeCode.isAnnualUnprotectedBird(permitTypeCode);
    }

    @Nonnull
    @Transient
    public String createPermitNumber() {
        return PermitNumberUtil.createPermitNumber(decisionYear, validityYears, decisionNumber);
    }

    @Nonnull
    @Transient
    public String createPermitNumber(final int year) {
        return PermitNumberUtil.createPermitNumber(year, validityYears, decisionNumber);
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
    public void setId(final Long id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    private void setStatus(final Status status) {
        this.status = status;
    }

    public DecisionType getDecisionType() {
        return decisionType;
    }

    public void setDecisionType(final DecisionType decisionType) {
        this.decisionType = decisionType;
    }

    public String getPermitTypeCode() {
        return permitTypeCode;
    }

    public void setPermitTypeCode(final String permitTypeCode) {
        this.permitTypeCode = permitTypeCode;
    }

    public int getDecisionYear() {
        return decisionYear;
    }

    public void setDecisionYear(final int decisionYear) {
        this.decisionYear = decisionYear;
    }

    public int getValidityYears() {
        return validityYears;
    }

    public void setValidityYears(final int validityYears) {
        this.validityYears = validityYears;
    }

    public int getDecisionNumber() {
        return decisionNumber;
    }

    public void setDecisionNumber(final int decisionNumber) {
        this.decisionNumber = decisionNumber;
    }

    public GrantStatus getGrantStatus() {
        return grantStatus;
    }

    public void setGrantStatus(final GrantStatus grantStatus) {
        this.grantStatus = grantStatus;
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

    public DeliveryAddress getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(DeliveryAddress deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public PermitHolder getPermitHolder() {
        return permitHolder;
    }

    public void setPermitHolder(final PermitHolder permitHolder) {
        this.permitHolder = permitHolder;
    }

    public HuntingClub getHuntingClub() {
        return huntingClub;
    }

    public void setHuntingClub(final HuntingClub huntingClub) {
        this.huntingClub = huntingClub;
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

    public boolean isLegalSection32() {
        return legalSection32;
    }

    public void setLegalSection32(final boolean legalSection32) {
        this.legalSection32 = legalSection32;
    }

    public boolean isLegalSection33() {
        return legalSection33;
    }

    public void setLegalSection33(final boolean legalSection33) {
        this.legalSection33 = legalSection33;
    }

    public boolean isLegalSection34() {
        return legalSection34;
    }

    public void setLegalSection34(final boolean legalSection34) {
        this.legalSection34 = legalSection34;
    }

    public boolean isLegalSection35() {
        return legalSection35;
    }

    public void setLegalSection35(final boolean legalSection35) {
        this.legalSection35 = legalSection35;
    }

    public boolean isLegalSection51() {
        return legalSection51;
    }

    public void setLegalSection51(final boolean legalSection51) {
        this.legalSection51 = legalSection51;
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
