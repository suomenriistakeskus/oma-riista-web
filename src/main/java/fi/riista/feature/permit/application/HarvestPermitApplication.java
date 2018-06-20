package fi.riista.feature.permit.application;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.attachment.HarvestPermitApplicationAttachment;
import fi.riista.feature.permit.application.species.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.area.HarvestPermitArea;
import fi.riista.feature.permit.area.IllegalHarvestPermitAreaStateTransitionException;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.util.LocalisedString;
import fi.riista.validation.FinnishHuntingPermitNumber;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.DateTime;
import org.springframework.util.StringUtils;

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
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
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
import java.util.Set;
import java.util.stream.Stream;

@Entity
@Access(AccessType.FIELD)
public class HarvestPermitApplication extends LifecycleEntity<Long> {
    private static final LocalisedString FILENAME_PREFIX =
            new LocalisedString("Hakemus", "Ansökning");

    public static String getPdfFileName(final Locale locale, final int applicationNumber) {
        return String.format("%s-%d.pdf", FILENAME_PREFIX.getAnyTranslation(locale), applicationNumber);
    }
    public static String getArchiveFileName(final Locale locale, final int applicationNumber) {
        return String.format("%s-%d.zip", FILENAME_PREFIX.getAnyTranslation(locale), applicationNumber);
    }

    public enum Status {
        // Hakemus on jätetty ja valmis käsiteltäväksi
        ACTIVE,

        // Hakemus peruutettu LH:ssa
        CANCELLED,

        // Moderaattori on avannut hakemuksen täydennettäväksi
        AMENDING,

        // Hakemus on kesken ja näkyy vain yhteyshenkilölle
        DRAFT
    }

    public static final String ID_COLUMN_NAME = "harvest_permit_application_id";

    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private int huntingYear;

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

    @FinnishHuntingPermitNumber
    @Column
    private String permitNumber;

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

    // TODO: Viittau LH:n tarjoamaan PDF-tiedostoon, joka voidaan poistaa kun LH:sta tulleita hakemuksia ei enää tarvita.
    @Column(length = 2048) // max url length in IE
    private URL printingUrl;

    @Size(min = 3, max = 3)
    @NotNull
    @Column(nullable = false, length = 3)
    private String permitTypeCode;

    @ManyToOne(fetch = FetchType.LAZY)
    private HuntingClub permitHolder;

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

    @Transient
    public void startAmending() {
        assertStatus(EnumSet.of(HarvestPermitApplication.Status.ACTIVE, HarvestPermitApplication.Status.AMENDING));
        setStatus(HarvestPermitApplication.Status.AMENDING);
    }

    @Transient
    public void stopAmending() {
        assertStatus(HarvestPermitApplication.Status.AMENDING);
        setStatus(HarvestPermitApplication.Status.ACTIVE);
    }

    @Transient
    public Stream<String> streamEmails() {
        return Stream.of(email1, email2).filter(StringUtils::hasText);
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
            throw new HarvestPermitApplicationAreaMissingException(getId());
        }
    }

    // VALIDATION CONSTRAINTS

    @AssertTrue
    public boolean isSubmitDateSetWhenActive() {
        return this.status == Status.DRAFT || this.submitDate != null;
    }

    @AssertTrue
    public boolean isApplicationNumberSetWhenActive() {
        return this.status == Status.DRAFT || this.applicationNumber != null;
    }

    @AssertTrue
    public boolean isPermitNumberSetWhenActive() {
        return this.status == Status.DRAFT || this.permitNumber != null;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public int getHuntingYear() {
        return huntingYear;
    }

    public void setHuntingYear(final int huntingYear) {
        this.huntingYear = huntingYear;
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

    public String getPermitNumber() {
        return permitNumber;
    }

    public void setPermitNumber(final String permitNumber) {
        this.permitNumber = permitNumber;
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

    public URL getPrintingUrl() {
        return printingUrl;
    }

    public void setPrintingUrl(final URL printingUrl) {
        this.printingUrl = printingUrl;
    }

    public String getPermitTypeCode() {
        return permitTypeCode;
    }

    public void setPermitTypeCode(final String permitTypeCode) {
        this.permitTypeCode = permitTypeCode;
    }

    public HuntingClub getPermitHolder() {
        return permitHolder;
    }

    public void setPermitHolder(HuntingClub permitHolder) {
        this.permitHolder = permitHolder;
    }

    public Person getContactPerson() {
        return contactPerson;
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
}
