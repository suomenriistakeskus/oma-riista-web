package fi.riista.feature.permit.application;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.mooselike.MooselikePermitApplicationAreaMissingException;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.area.IllegalHarvestPermitAreaStateTransitionException;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import fi.riista.util.LocalisedString;
import org.hibernate.annotations.Type;
import javax.validation.constraints.Email;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.DateTime;
import org.springframework.util.StringUtils;

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
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.net.URL;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Entity
@Access(AccessType.FIELD)
public class HarvestPermitApplication extends LifecycleEntity<Long> {
    public static final LocalisedString FILENAME_PREFIX =
            new LocalisedString("Hakemus", "Ansökning");

    public enum Status {
        // Hakemus on jätetty ja valmis käsiteltäväksi
        ACTIVE,

        // Hakemus piilotetaan, kyseessä on peruutettu hakemus mutta sen peruutuspäätös on tehty toisessa järjestelmässä.
        HIDDEN,

        // Moderaattori on avannut hakemuksen täydennettäväksi
        AMENDING,

        // Hakemus on kesken ja näkyy vain yhteyshenkilölle
        DRAFT
    }

    public static final String ID_COLUMN_NAME = "harvest_permit_application_id";

    private Long id;

    @Type(type = "uuid-char")
    @Column
    private UUID uuid;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private int applicationYear;

    // Timestamp used to determine when application was submitted, eg. first time status changed to ACTIVE
    @Column
    private DateTime submitDate;

    @Min(10_000)
    @Column
    private Integer applicationNumber;

    @Column
    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String applicationName;

    // Ampujat, jotka eivät kuulu muuhun pyyntilupaa hakevaan seuraan / seurueeseen
    @Column
    @Min(0)
    private Integer shooterOnlyClub;

    // Ampujat, jotka kuuluvat muuhun hirveä metsästävään seuraan / seurueeseen, mutta eivät metsästä siellä tulevana metsästyskautena.
    @Column
    @Min(0)
    private Integer shooterOtherClubPassive;

    // Ampujat, jotka kuuluvat muuhun hirveä metsästävään seuraan / seurueeseen, ja metsästävät siellä tulevana metsästyskautena.
    @Column
    @Min(0)
    private Integer shooterOtherClubActive;

    @Email
    @Size(max = 255)
    @Column
    private String email1;

    @Email
    @Size(max = 255)
    @Column
    private String email2;

    @Column
    private Boolean deliveryByMail;

    @Embedded
    @Valid
    private DeliveryAddress deliveryAddress;

    // TODO: Viittau LH:n tarjoamaan PDF-tiedostoon, joka voidaan poistaa kun LH:sta tulleita hakemuksia ei enää tarvita.
    @Column(length = 2048) // max url length in IE
    private URL printingUrl;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private HarvestPermitCategory harvestPermitCategory;

    @Embedded
    @Valid
    private PermitHolder permitHolder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permit_holder_id")
    private HuntingClub huntingClub;

    @ManyToOne(fetch = FetchType.LAZY)
    private Person contactPerson;

    @ManyToOne(fetch = FetchType.LAZY)
    private HarvestPermitArea area;

    @ManyToOne(fetch = FetchType.LAZY)
    private Riistanhoitoyhdistys rhy;

    // HACK: Only marked optional=false to skip eager fetch. Use only for JPQL queries!
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "application")
    private PermitDecision decision;

    @ManyToMany
    @JoinTable(name = "harvest_permit_application_partner",
            joinColumns = {@JoinColumn(name = ID_COLUMN_NAME, referencedColumnName = ID_COLUMN_NAME)},
            inverseJoinColumns = {@JoinColumn(name = Organisation.ID_COLUMN_NAME, referencedColumnName = Organisation.ID_COLUMN_NAME)})
    private Set<HuntingClub> permitPartners = new HashSet<>();

    @OneToMany(mappedBy = "harvestPermitApplication")
    private List<HarvestPermitApplicationSpeciesAmount> speciesAmounts = new LinkedList<>();

    @OneToMany(mappedBy = "harvestPermitApplication")
    private List<HarvestPermitApplicationAttachment> attachments = new LinkedList<>();

    @ManyToMany
    @JoinTable(name = "harvest_permit_application_rhy",
            joinColumns = {@JoinColumn(name = ID_COLUMN_NAME, referencedColumnName = ID_COLUMN_NAME)},
            inverseJoinColumns = {@JoinColumn(name = Organisation.ID_COLUMN_NAME, referencedColumnName = Organisation.ID_COLUMN_NAME)})
    private Set<Riistanhoitoyhdistys> relatedRhys = new HashSet<>();

    @NotNull
    @Column(name = "locale_id", nullable = false)
    private Locale locale;

    @NotNull
    @Column(name = "decision_locale_id", nullable = false)
    private Locale decisionLocale;

    @Transient
    public void startAmending() {
        assertStatus(EnumSet.of(Status.ACTIVE, Status.AMENDING));
        setStatus(Status.AMENDING);
    }

    @Transient
    public void stopAmending() {
        assertStatus(Status.AMENDING);
        setStatus(Status.ACTIVE);
    }

    @Transient
    public Stream<String> streamEmails() {
        return Stream.of(email1, email2).filter(StringUtils::hasText);
    }

    @Transient
    public List<String> getAttachmentFilenames(final HarvestPermitApplicationAttachment.Type type) {
        return attachments.stream()
                .filter(a -> a.getAttachmentType() == type)
                .map(HarvestPermitApplicationAttachment::getAttachmentMetadata)
                .map(PersistentFileMetadata::getOriginalFilename)
                .filter(StringUtils::hasText)
                .collect(toList());
    }

    @Transient
    public void assertStatus(final Status allowed) {
        assertStatus(EnumSet.of(allowed));
    }

    @Transient
    public void assertStatus(final EnumSet<Status> allowed) {
        if (!allowed.contains(this.status)) {
            throw new IllegalHarvestPermitAreaStateTransitionException(
                    String.format("status should be %s was %s", allowed, this.status));
        }
    }

    public void assertHasPermitArea() {
        if (this.area == null) {
            throw new MooselikePermitApplicationAreaMissingException(getId());
        }
    }

    @Transient
    public Integer getValidityYears() {
        return getSpeciesAmounts().stream()
                .map(HarvestPermitApplicationSpeciesAmount::getValidityYears)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    // VALIDATION CONSTRAINTS

    @AssertTrue
    public boolean isSubmitDateSetWhenActive() {
        return this.status == Status.DRAFT || this.status == Status.HIDDEN || this.submitDate != null;
    }

    @AssertTrue
    public boolean isApplicationNumberSetWhenActive() {
        return this.status == Status.DRAFT || this.status == Status.HIDDEN || this.applicationNumber != null;
    }

    @AssertTrue
    public boolean isPermitHolderSetWhenActive() {
        return this.status == Status.DRAFT || this.status == Status.HIDDEN || this.permitHolder != null;
    }

    @AssertTrue
    public boolean isDeliveryAddresSetWhenActive() {
        return this.status == Status.DRAFT || this.status == Status.HIDDEN || this.deliveryAddress != null;
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
    public void setId(Long id) {
        this.id = id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(final UUID uuid) {
        this.uuid = uuid;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public int getApplicationYear() {
        return applicationYear;
    }

    public void setApplicationYear(final int huntingYear) {
        this.applicationYear = huntingYear;
    }

    public DateTime getSubmitDate() {
        return submitDate;
    }

    public void setSubmitDate(final DateTime submitDate) {
        this.submitDate = submitDate;
    }

    public Integer getApplicationNumber() {
        return applicationNumber;
    }

    public void setApplicationNumber(final Integer applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(final String applicationName) {
        this.applicationName = applicationName;
    }

    public Integer getShooterOnlyClub() {
        return shooterOnlyClub;
    }

    public void setShooterOnlyClub(final Integer shooterOnlyClub) {
        this.shooterOnlyClub = shooterOnlyClub;
    }

    public Integer getShooterOtherClubPassive() {
        return shooterOtherClubPassive;
    }

    public void setShooterOtherClubPassive(final Integer shooterOtherClubPassive) {
        this.shooterOtherClubPassive = shooterOtherClubPassive;
    }

    public Integer getShooterOtherClubActive() {
        return shooterOtherClubActive;
    }

    public void setShooterOtherClubActive(final Integer shooterOtherClubActive) {
        this.shooterOtherClubActive = shooterOtherClubActive;
    }

    public String getEmail1() {
        return email1;
    }

    public void setEmail1(final String email1) {
        this.email1 = email1;
    }

    public String getEmail2() {
        return email2;
    }

    public void setEmail2(final String email2) {
        this.email2 = email2;
    }

    public Boolean getDeliveryByMail() {
        return deliveryByMail;
    }

    public void setDeliveryByMail(final Boolean deliveryByMail) {
        this.deliveryByMail = deliveryByMail;
    }

    public DeliveryAddress getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(DeliveryAddress deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public URL getPrintingUrl() {
        return printingUrl;
    }

    public void setPrintingUrl(final URL printingUrl) {
        this.printingUrl = printingUrl;
    }

    public HarvestPermitCategory getHarvestPermitCategory() {
        return harvestPermitCategory;
    }

    public void setHarvestPermitCategory(HarvestPermitCategory harvestPermitCategory) {
        this.harvestPermitCategory = harvestPermitCategory;
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

    public Person getContactPerson() {
        return contactPerson;
    }

    @Transient
    public void setHuntingClubAndPermitHolder(final HuntingClub club) {
        setHuntingClub(club);
        setPermitHolder(PermitHolder.createHolderForClub(club));
    }

    public void setContactPerson(final Person contactPerson) {
        this.contactPerson = contactPerson;
    }

    public Riistanhoitoyhdistys getRhy() {
        return rhy;
    }

    public void setRhy(Riistanhoitoyhdistys rhy) {
        this.rhy = rhy;
    }

    public HarvestPermitArea getArea() {
        return area;
    }

    public void setArea(HarvestPermitArea area) {
        this.area = area;
    }

    public List<HarvestPermitApplicationSpeciesAmount> getSpeciesAmounts() {
        return speciesAmounts;
    }

    public void setSpeciesAmounts(final List<HarvestPermitApplicationSpeciesAmount> speciesAmounts) {
        this.speciesAmounts = speciesAmounts;
    }

    public List<HarvestPermitApplicationAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<HarvestPermitApplicationAttachment> attachments) {
        this.attachments = attachments;
    }

    public Set<HuntingClub> getPermitPartners() {
        return permitPartners;
    }

    public void setPermitPartners(Set<HuntingClub> permitPartners) {
        this.permitPartners = permitPartners;
    }

    public Set<Riistanhoitoyhdistys> getRelatedRhys() {
        return relatedRhys;
    }

    public void setRelatedRhys(Set<Riistanhoitoyhdistys> relatedRhys) {
        this.relatedRhys = relatedRhys;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(final Locale locale) {
        this.locale = locale;
    }

    public Locale getDecisionLocale() {
        return decisionLocale;
    }

    public void setDecisionLocale(Locale decisionLocale) {
        this.decisionLocale = decisionLocale;
    }
}
