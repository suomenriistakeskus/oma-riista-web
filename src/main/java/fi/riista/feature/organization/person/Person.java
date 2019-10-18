package fi.riista.feature.organization.person;

import fi.riista.feature.account.payment.HuntingPaymentInfo;
import fi.riista.feature.account.payment.HuntingPaymentUtil;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.common.entity.Municipality;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.address.AddressSource;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.LocalisedString;
import fi.riista.validation.FinnishHunterNumber;
import fi.riista.validation.FinnishSocialSecurityNumber;
import fi.riista.validation.FinnishSocialSecurityNumberValidator;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Hibernate;
import org.hibernate.UnresolvableObjectException;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityNotFoundException;
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
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static fi.riista.util.DateUtil.today;

@Entity
@Access(value = AccessType.FIELD)
public class Person extends LifecycleEntity<Long> {

    public enum DeletionCode {
        D // DEAD
    }

    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "CHAR(1)")
    private DeletionCode deletionCode;

    @FinnishSocialSecurityNumber
    @Column(unique = true, length = 11)
    private String ssn;

    @Column
    private LocalDate dateOfBirth;

    @NotBlank
    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(nullable = false)
    private String firstName;

    @NotBlank
    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(nullable = false)
    private String lastName;

    @NotBlank
    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column(nullable = false)
    private String byName;

    // Native language
    @Size(max = 2)
    @Column(length = 2)
    private String languageCode;

    @Email
    @Size(max = 255)
    @Column
    private String email;

    @Size(max = 255)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    @Column
    private String phoneNumber;

    // Referenced row might not exist
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_municipality_code", insertable = false, updatable = false)
    private Municipality homeMunicipality;

    // Duplicate mapping to avoid missing foreign row
    @Pattern(regexp = "^\\d{0,3}$")
    @Column(name = "home_municipality_code", length = 3)
    private String homeMunicipalityCode;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(unique = true)
    private Address mrAddress;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(unique = true)
    private Address otherAddress;

    @OneToMany(mappedBy = "person")
    private Set<Occupation> occupations = new HashSet<>();

    @OneToMany(mappedBy = "person")
    private Set<SystemUser> systemUsers = new HashSet<>();

    @Size(max = 255)
    @Column(unique = true, length = 255)
    private String lhPersonId;

    // Metsästäjärekisteri: Milloin tiedot on viimeksi tuotu järjestelmään?
    @Column
    private DateTime mrSyncTime;

    @FinnishHunterNumber
    @Column
    private String hunterNumber;

    // Metsästäjärekisteri: Minkä RHY jäsen henkilö on?
    // Henkilö voi olla maksanut riistanhoitomaksun myös ilman RHY jäsenyyttä.
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private Riistanhoitoyhdistys rhyMembership;

    // Metsästäjärekisteri: Mistä päivästä lähtien nykyinen maksettu riistanhoitomaksu on voimassa?
    @Column
    private LocalDate huntingCardStart;

    // Metsästäjärekisteri: Mihin päivään asti nykyinen maksettu riistanhoitomaksu on voimassa?
    @Column
    private LocalDate huntingCardEnd;

    // Metsästäjärekisteri: Uusimman metsästyskauden maksupäivä
    @Column
    private LocalDate huntingPaymentOneDay;

    // Metsästäjärekisteri: Uusimman metsästyskauden maksupäivää vastaava kausi (2014 = 1.8.2014 - 31.7.2015)
    @Column
    private Integer huntingPaymentOneYear;

    // Metsästäjärekisteri: Edellisen metsästyskauden maksupäivä
    @Column
    private LocalDate huntingPaymentTwoDay;

    // Metsästäjärekisteri: Edellisen metsästyskauden maksupäivää vastaava kausi
    @Column
    private Integer huntingPaymentTwoYear;

    // Metsästäjärekisteri: Laskun viitekoodit ja vastaavat metsästysvuodet
    @Size(max = 255)
    @Column
    private String invoiceReferenceCurrent;

    @Size(max = 255)
    @Column
    private String invoiceReferencePrevious;

    @Column
    private Integer invoiceReferenceCurrentYear;

    @Column
    private Integer invoiceReferencePreviousYear;

    // Metsästäjärekisteri: Metsästäjäntutkinnon suorituspäivämäärä
    // Tutkinnon suorituspäivä voi olla aiempi kuin metsästäjärekisterin liittymispäivä,
    // koska silloin saa mahdollisuuden maksaa riistanhoitomaksun.
    // Toiminnanohjaaja lisää suorittaneet metsästäjärekisteriin,
    // josta syntyy rekisteriin liittymispäivämäärä.
    // Toistaiseksi kenttään on tuotu importissa suoraan tuo liittymispäivämäärä.
    @Column
    private LocalDate hunterExamDate;

    // Metsästäjärekisteri: Metsästäjäntutkinnon vanhenemispäivä (nyk. 5 vuotta suorituksesta)
    @Column
    private LocalDate hunterExamExpirationDate;

    // Metsästäjärekisteri: Metsästyskiellon alkupäivä
    @Column
    private LocalDate huntingBanStart;

    // Metsästäjärekisteri: Metsästyskiellon päättymispäivä
    @Column
    private LocalDate huntingBanEnd;

    // Metsästäjärekisteri: Lehden tilauskieli ja materiaalin (joulukortti) kieli
    // Tieto syötetään tutkinnon suorituksen yhteydessä täytettävällä lomakkeella henkilön itsensä toimesta.
    @Size(max = 2)
    @Column(length = 2)
    private String magazineLanguageCode;

    // Metsästäjärekisteri: Ei Metsästäjä-lehteä ("rasti ruutuun”)
    @Column(nullable = false)
    private boolean denyMagazine;

    @OneToMany(mappedBy = "author")
    private Set<Harvest> authoredHarvests = new HashSet<>();

    @OneToMany(mappedBy = "actualShooter")
    private Set<Harvest> huntedHarvests = new HashSet<>();

    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private Set<Observation> authoredObservations = new HashSet<>();

    @OneToMany(mappedBy = "observer", fetch = FetchType.LAZY)
    private Set<Observation> actualObservations = new HashSet<>();

    @Column
    private Boolean enableSrva;

    @Column
    private Boolean enableShootingTests;

    @AssertTrue
    public boolean isSsnOrDateOfBirthSet() {
        return ssn != null || dateOfBirth != null;
    }

    public static String maskSsn(final String ssn) {
        return StringUtils.isEmpty(ssn) ? StringUtils.EMPTY : StringUtils.substring(ssn, 0, 6) + "*****";
    }

    public String getSsnMasked() {
        return maskSsn(this.ssn);
    }

    public LocalDate parseDateOfBirth() {
        // TODO: Handle date of birth for Finnish persons also
        if (isForeignPerson()) {
            return dateOfBirth;
        } else {
            return this.ssn != null ? FinnishSocialSecurityNumberValidator.parseBirthDate(this.ssn) : null;

        }
    }

    public boolean isAdult() {
        return DateUtil.isAdultBirthDate(Objects.requireNonNull(parseDateOfBirth(), "unknown dateOfBirth"));
    }

    public boolean isArtificialPerson() {
        return ssn != null && ssn.charAt(7) == '9';
    }

    public boolean isForeignPerson() {
        return ssn == null;
    }

    public String getFullName() {
        return String.format("%s %s", getFirstName(), getLastName());
    }

    public Address getAddress() {
        return F.firstNonNull(mrAddress, otherAddress);
    }

    public AddressSource getAddressSource() {
        return mrAddress != null ? AddressSource.METSASTAJAREKISTERI : AddressSource.OTHER;
    }

    public boolean isAddressEditable() {
        return mrAddress == null;
    }

    public boolean isRegistered() {
        for (SystemUser systemUser : systemUsers) {
            if (isRoleUser(systemUser)) {
                return true;
            }
        }
        return false;
    }

    public boolean isActive() {
        for (SystemUser systemUser : systemUsers) {
            if (isRoleUser(systemUser) && systemUser.isActive()) {
                return true;
            }
        }
        return false;
    }

    public boolean isDeceased() {
        return this.deletionCode == DeletionCode.D;
    }

    public void deactivate() {
        for (SystemUser systemUser : systemUsers) {
            systemUser.setActive(false);
        }
    }

    public boolean hasHunterNumber() {
        return StringUtils.isNotBlank(this.hunterNumber);
    }

    public List<String> listUsernames() {
        return systemUsers.stream().map(SystemUser::getUsername).collect(Collectors.toList());
    }

    public boolean isHuntingCardValidNow() {
        return huntingCardStart != null && huntingCardEnd != null &&
                DateUtil.overlapsInclusive(huntingCardStart, huntingCardEnd, today());
    }

    public boolean isHuntingCardValidInFuture() {
        return huntingCardStart != null && today().isBefore(huntingCardStart);
    }

    public boolean isHunterExamValidNow() {
        return (this.hunterExamDate != null || this.hunterExamExpirationDate != null) &&
                DateUtil.overlapsInclusive(this.hunterExamDate, this.hunterExamExpirationDate, today());
    }

    public boolean isHuntingBanActiveNow() {
        return huntingBanStart != null && huntingBanEnd != null &&
                DateUtil.overlapsInclusive(huntingBanStart, huntingBanEnd, today());
    }

    public boolean isPaymentDateMissing(final int huntingYear) {
        return !getHuntingPaymentDateForHuntingYear(huntingYear).isPresent();
    }

    public boolean isInvoiceReferenceAvailable(final int huntingYear) {
        return getInvoiceReferenceForHuntingYear(huntingYear).isPresent();
    }

    public HuntingPaymentInfo getPaymentInfo(final int huntingYear) {
        if (!HuntingPaymentUtil.isPaymentAllowedForHuntingSeason(huntingYear)) {
            throw new IllegalStateException("Payment PDF generation is not allowed");
        }

        final LocalDate dateOfBirth = parseDateOfBirth();
        final String invoiceReference = getInvoiceReferenceForHuntingYear(huntingYear)
                .orElseThrow(() -> new IllegalArgumentException("Could not calculate paymentInfo"));

        return HuntingPaymentInfo.create(huntingYear, dateOfBirth, invoiceReference);
    }

    public Optional<LocalDate> getHuntingPaymentDateForNextOrCurrentSeason() {
        final int huntingYear = DateUtil.huntingYear();

        return getHuntingPaymentDateForNextOrCurrentSeason(huntingYear);
    }

    Optional<LocalDate> getHuntingPaymentDateForNextOrCurrentSeason(final int currentHuntingYear) {
        final Optional<LocalDate> next = this.getHuntingPaymentDateForHuntingYear(currentHuntingYear + 1);

        return next.isPresent() ? next : this.getHuntingPaymentDateForHuntingYear(currentHuntingYear);
    }

    private Optional<LocalDate> getHuntingPaymentDateForHuntingYear(final int huntingYear) {
        return pickByHuntingYear(huntingYear, Arrays.asList(
                Tuple.of(this.huntingPaymentOneYear, this.huntingPaymentOneDay),
                Tuple.of(this.huntingPaymentTwoYear, this.huntingPaymentTwoDay)));
    }

    private Optional<String> getInvoiceReferenceForHuntingYear(final int huntingYear) {
        return pickByHuntingYear(huntingYear, Arrays.asList(
                Tuple.of(this.invoiceReferenceCurrentYear, this.invoiceReferenceCurrent),
                Tuple.of(this.invoiceReferencePreviousYear, this.invoiceReferencePrevious)));
    }

    private static <T> Optional<T> pickByHuntingYear(final int huntingYear, final List<Tuple2<Integer, T>> options) {
        return options.stream()
                .filter(option -> option._1 != null && option._1.equals(huntingYear))
                .map(Tuple2::_2)
                .findAny();
    }

    public boolean isPaymentPendingForHuntingYear(final int huntingYear) {
        // Payment for current or next hunting year completed?
        if (huntingPaymentOneYear != null) {
            if (huntingYear == huntingPaymentOneYear || huntingYear == huntingPaymentOneYear - 1) {
                return false;
            }
        }

        if (huntingPaymentTwoYear != null) {
            if (huntingYear == huntingPaymentTwoYear || huntingYear == huntingPaymentTwoYear - 1) {
                return false;
            }
        }

        return true;
    }

    public Set<Integer> getHuntingPaymentPdfYears() {
        return HuntingPaymentUtil.getHuntingPaymentPdfYears(this);
    }

    public boolean canPrintCertificate() {
        return hasHunterNumber() &&
                !this.isDeleted() && this.deletionCode == null &&
                this.rhyMembership != null &&
                isHuntingCardValidNow() &&
                getHuntingPaymentDateForNextOrCurrentSeason().isPresent() &&
                !isArtificialPerson() &&
                !isHuntingBanActiveNow();
    }

    public void clearHunterInformation() {
        this.hunterNumber = null;
        this.huntingCardStart = null;
        this.huntingCardEnd = null;
        this.hunterExamDate = null;
        this.hunterExamExpirationDate = null;
        this.huntingBanStart = null;
        this.huntingBanEnd = null;
        this.rhyMembership = null;
        this.huntingPaymentOneDay = null;
        this.huntingPaymentOneYear = null;
        this.huntingPaymentTwoDay = null;
        this.huntingPaymentTwoYear = null;
    }

    public List<Occupation> getClubSpecificOccupations() {
        return occupations.stream()
                .filter(o -> o.getOccupationType().isClubOrGroupOccupation())
                .filter(o -> o.isValidNow() && !o.isDeleted())
                .collect(Collectors.toList());
    }

    public Iterable<Occupation> getNotClubSpecificOccupations() {
        return occupations.stream()
                .filter(o -> !o.getOccupationType().isClubOrGroupOccupation())
                .filter(o -> o.isValidNow() && !o.isDeleted())
                .collect(Collectors.toList());
    }

    private static boolean isRoleUser(final SystemUser systemUser) {
        return SystemUser.Role.ROLE_USER == systemUser.getRole();
    }

    public boolean hasHomeMunicipality() {
        return this.homeMunicipalityCode != null;
    }

    @Nonnull
    public LocalisedString getHomeMunicipalityName() {
        if (this.homeMunicipality != null) {
            if (!Hibernate.isInitialized(this.homeMunicipality)) {
                try {
                    // Referenced row might not exist
                    return this.homeMunicipality.getNameLocalisation();
                } catch (UnresolvableObjectException | EntityNotFoundException o) {
                    this.homeMunicipality = null;
                }
            } else {
                return this.homeMunicipality.getNameLocalisation();
            }
        }
        return LocalisedString.EMPTY;
    }

    public boolean isSrvaEnabled() {
        return Boolean.TRUE.equals(getEnableSrva());
    }

    public boolean isShootingTestsEnabled() {
        return Boolean.TRUE.equals(getEnableShootingTests());
    }

    // Accessors -->

    @Override
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Access(value = AccessType.PROPERTY)
    @Column(name = "person_id", nullable = false)
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public DeletionCode getDeletionCode() {
        return deletionCode;
    }

    public void setDeletionCode(final DeletionCode deletionCode) {
        this.deletionCode = deletionCode;
    }

    public String getSsn() {
        return this.ssn;
    }

    public void setSsn(final String ssn) {
        this.ssn = ssn != null ? ssn.trim().toUpperCase() : null;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(final LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getByName() {
        return byName;
    }

    public void setByName(final String byName) {
        this.byName = byName;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(final String languageCode) {
        this.languageCode = languageCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email != null ? email.trim().toLowerCase() : null;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setHomeMunicipality(final Municipality municipality) {
        this.homeMunicipality = municipality;
    }

    public String getHomeMunicipalityCode() {
        return homeMunicipalityCode;
    }

    public void setHomeMunicipalityCode(final String homeMunicipalityCode) {
        this.homeMunicipalityCode = homeMunicipalityCode;
    }

    public Address getMrAddress() {
        return mrAddress;
    }

    public void setMrAddress(final Address mrAddress) {
        this.mrAddress = mrAddress;
    }

    public Address getOtherAddress() {
        return otherAddress;
    }

    public void setOtherAddress(final Address otherAddress) {
        this.otherAddress = otherAddress;
    }

    public Set<Occupation> getOccupations() {
        return occupations;
    }

    public void setOccupations(final Set<Occupation> occupations) {
        this.occupations = occupations;
    }

    public String getLhPersonId() {
        return lhPersonId;
    }

    public void setLhPersonId(final String lhPersonId) {
        this.lhPersonId = lhPersonId;
    }

    public DateTime getMrSyncTime() {
        return mrSyncTime;
    }

    public void setMrSyncTime(final DateTime mrSyncTime) {
        this.mrSyncTime = mrSyncTime;
    }

    public String getHunterNumber() {
        return hunterNumber;
    }

    public void setHunterNumber(final String hunterNumber) {
        this.hunterNumber = hunterNumber;
    }

    public Riistanhoitoyhdistys getRhyMembership() {
        return rhyMembership;
    }

    public void setRhyMembership(final Riistanhoitoyhdistys rhyMembership) {
        this.rhyMembership = rhyMembership;
    }

    public LocalDate getHuntingCardStart() {
        return huntingCardStart;
    }

    public void setHuntingCardStart(final LocalDate huntingCardStart) {
        this.huntingCardStart = huntingCardStart;
    }

    public LocalDate getHuntingCardEnd() {
        return huntingCardEnd;
    }

    public void setHuntingCardEnd(final LocalDate huntingCardEnd) {
        this.huntingCardEnd = huntingCardEnd;
    }

    public LocalDate getHuntingPaymentOneDay() {
        return huntingPaymentOneDay;
    }

    public void setHuntingPaymentOneDay(final LocalDate huntingPaymentOneDay) {
        this.huntingPaymentOneDay = huntingPaymentOneDay;
    }

    public Integer getHuntingPaymentOneYear() {
        return huntingPaymentOneYear;
    }

    public void setHuntingPaymentOneYear(final Integer huntingPaymentOneYear) {
        this.huntingPaymentOneYear = huntingPaymentOneYear;
    }

    public LocalDate getHuntingPaymentTwoDay() {
        return huntingPaymentTwoDay;
    }

    public void setHuntingPaymentTwoDay(final LocalDate huntingPaymentTwoDay) {
        this.huntingPaymentTwoDay = huntingPaymentTwoDay;
    }

    public Integer getHuntingPaymentTwoYear() {
        return huntingPaymentTwoYear;
    }

    public void setHuntingPaymentTwoYear(final Integer huntingPaymentTwoYear) {
        this.huntingPaymentTwoYear = huntingPaymentTwoYear;
    }

    public String getInvoiceReferenceCurrent() {
        return invoiceReferenceCurrent;
    }

    public void setInvoiceReferenceCurrent(final String invoiceReferenceCurrent) {
        this.invoiceReferenceCurrent = invoiceReferenceCurrent;
    }

    public String getInvoiceReferencePrevious() {
        return invoiceReferencePrevious;
    }

    public void setInvoiceReferencePrevious(final String invoiceReferencePrevious) {
        this.invoiceReferencePrevious = invoiceReferencePrevious;
    }

    public Integer getInvoiceReferenceCurrentYear() {
        return invoiceReferenceCurrentYear;
    }

    public void setInvoiceReferenceCurrentYear(final Integer invoiceReferenceCurrentYear) {
        this.invoiceReferenceCurrentYear = invoiceReferenceCurrentYear;
    }

    public Integer getInvoiceReferencePreviousYear() {
        return invoiceReferencePreviousYear;
    }

    public void setInvoiceReferencePreviousYear(final Integer invoiceReferencePreviousYear) {
        this.invoiceReferencePreviousYear = invoiceReferencePreviousYear;
    }

    public LocalDate getHunterExamDate() {
        return hunterExamDate;
    }

    public void setHunterExamDate(final LocalDate hunterExamDate) {
        this.hunterExamDate = hunterExamDate;
    }

    public LocalDate getHunterExamExpirationDate() {
        return hunterExamExpirationDate;
    }

    public void setHunterExamExpirationDate(final LocalDate hunterExamExpirationDate) {
        this.hunterExamExpirationDate = hunterExamExpirationDate;
    }

    public LocalDate getHuntingBanStart() {
        return huntingBanStart;
    }

    public void setHuntingBanStart(final LocalDate huntingBanStart) {
        this.huntingBanStart = huntingBanStart;
    }

    public LocalDate getHuntingBanEnd() {
        return huntingBanEnd;
    }

    public void setHuntingBanEnd(final LocalDate huntingBanEnd) {
        this.huntingBanEnd = huntingBanEnd;
    }

    public String getMagazineLanguageCode() {
        return magazineLanguageCode;
    }

    public void setMagazineLanguageCode(final String magazineLanguageCode) {
        this.magazineLanguageCode = magazineLanguageCode;
    }

    public boolean isDenyMagazine() {
        return denyMagazine;
    }

    public void setDenyMagazine(final boolean denyMagazine) {
        this.denyMagazine = denyMagazine;
    }

    public Boolean getEnableSrva() {
        return enableSrva;
    }

    public void setEnableSrva(final Boolean enableSrva) {
        this.enableSrva = enableSrva;
    }

    public Boolean getEnableShootingTests() {
        return enableShootingTests;
    }

    public void setEnableShootingTests(final Boolean enableShootingTests) {
        this.enableShootingTests = enableShootingTests;
    }

    // Following collection getters exposed in package-private scope only for property introspection.

    Set<Harvest> getAuthoredHarvests() {
        return authoredHarvests;
    }

    Set<Harvest> getHuntedHarvests() {
        return huntedHarvests;
    }

    Set<Observation> getAuthoredObservations() {
        return authoredObservations;
    }

    Set<Observation> getActualObservations() {
        return actualObservations;
    }
}
