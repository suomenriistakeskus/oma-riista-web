package fi.riista.feature.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.common.dto.BaseEntityDTO;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.address.AddressSource;
import fi.riista.util.Patterns;
import fi.riista.validation.DoNotValidate;
import fi.riista.validation.FinnishHunterNumber;
import javax.validation.constraints.Email;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

public class AccountDTO extends BaseEntityDTO<Long> {
    public Long id;

    @JsonProperty(value = "rev")
    public Integer rev;

    private boolean registered;

    private boolean rememberMe;

    private boolean active;

    private boolean foreignPerson;

    private boolean allowPrintCertificate;

    @Size(min = 2, max = 63)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String username;

    private SystemUser.Role role;


    private Set<SystemUserPrivilege> privileges = Collections.emptySet();

    @DoNotValidate
    private List<AccountRoleDTO> accountRoles;

    private Long personId;

    @Email
    private String email;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String firstName;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String lastName;

    @Size(min = 2, max = 255)
    @Pattern(regexp = Patterns.BY_NAME)
    private String byName;

    private TimeZone timeZone;

    private Locale locale;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String dateOfBirth;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String homeMunicipality;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String languageCode;

    //@PhoneNumber
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String phoneNumber;

    @Valid
    private AddressDTO address;

    private AddressSource addressSource;

    private DateTime mrSyncTime;

    @FinnishHunterNumber
    private String hunterNumber;

    @DoNotValidate
    private OrganisationNameDTO rhyMembership;

    private LocalDate huntingCardStart;

    private LocalDate huntingCardEnd;

    private LocalDate huntingPaymentDate;

    private boolean huntingPaymentPending;

    private Set<Integer> huntingPaymentPdfYears;

    private LocalDate hunterExamDate;

    private LocalDate hunterExamExpirationDate;

    private boolean hunterExamValid;

    private LocalDate huntingBanStart;

    private LocalDate huntingBanEnd;

    private boolean huntingBanActive;

    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String magazineLanguageCode;

    private boolean denyMagazine;

    @DoNotValidate
    private List<MyOccupationDTO> occupations;

    @DoNotValidate
    private List<MyClubOccupationDTO> clubOccupations;

    private boolean enableSrva;
    private boolean enableShootingTests;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Integer getRev() {
        return rev;
    }

    @Override
    public void setRev(final Integer rev) {
        this.rev = rev;
    }

    public String getUsername() {
        return username;
    }

    public final void setUsername(final String username) {
        this.username = username;
    }

    public SystemUser.Role getRole() {
        return role;
    }

    public void setRole(final SystemUser.Role role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
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

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(final TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(final Locale locale) {
        this.locale = locale;
    }

    public List<AccountRoleDTO> getAccountRoles() {
        return accountRoles;
    }

    public void setAccountRoles(final List<AccountRoleDTO> accountRoles) {
        this.accountRoles = accountRoles;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(final Long personId) {
        this.personId = personId;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(final String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getHomeMunicipality() {
        return homeMunicipality;
    }

    public void setHomeMunicipality(final String homeMunicipality) {
        this.homeMunicipality = homeMunicipality;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(final String languageCode) {
        this.languageCode = languageCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public AddressDTO getAddress() {
        return address;
    }

    public void setAddress(final AddressDTO address) {
        this.address = address;
    }

    public AddressSource getAddressSource() {
        return addressSource;
    }

    public void setAddressSource(final AddressSource addressSource) {
        this.addressSource = addressSource;
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

    public OrganisationNameDTO getRhyMembership() {
        return rhyMembership;
    }

    public void setRhyMembership(final OrganisationNameDTO rhyMembership) {
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

    public LocalDate getHuntingPaymentDate() {
        return huntingPaymentDate;
    }

    public void setHuntingPaymentDate(final LocalDate huntingPaymentDate) {
        this.huntingPaymentDate = huntingPaymentDate;
    }

    public boolean isHuntingPaymentPending() {
        return huntingPaymentPending;
    }

    public void setHuntingPaymentPending(final boolean huntingPaymentPending) {
        this.huntingPaymentPending = huntingPaymentPending;
    }

    public Set<Integer> getHuntingPaymentPdfYears() {
        return huntingPaymentPdfYears;
    }

    public void setHuntingPaymentPdfYears(final Set<Integer> huntingPaymentPdfYears) {
        this.huntingPaymentPdfYears = huntingPaymentPdfYears;
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

    public boolean isHunterExamValid() {
        return hunterExamValid;
    }

    public void setHunterExamValid(final boolean hunterExamValid) {
        this.hunterExamValid = hunterExamValid;
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

    public boolean isHuntingBanActive() {
        return huntingBanActive;
    }

    public void setHuntingBanActive(final boolean huntingBanActive) {
        this.huntingBanActive = huntingBanActive;
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

    public List<MyOccupationDTO> getOccupations() {
        return occupations;
    }

    public void setOccupations(final List<MyOccupationDTO> occupations) {
        this.occupations = occupations;
    }

    public List<MyClubOccupationDTO> getClubOccupations() {
        return clubOccupations;
    }

    public void setClubOccupations(final List<MyClubOccupationDTO> clubOccupations) {
        this.clubOccupations = clubOccupations;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(final boolean registered) {
        this.registered = registered;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(final boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }

    public boolean isForeignPerson() {
        return foreignPerson;
    }

    public void setForeignPerson(final boolean foreignPerson) {
        this.foreignPerson = foreignPerson;
    }

    public boolean isAllowPrintCertificate() {
        return allowPrintCertificate;
    }

    public void setAllowPrintCertificate(final boolean allowPrintCertificate) {
        this.allowPrintCertificate = allowPrintCertificate;
    }

    public boolean isEnableSrva() {
        return enableSrva;
    }

    public void setEnableSrva(final boolean enableSrva) {
        this.enableSrva = enableSrva;
    }

    public boolean isEnableShootingTests() {
        return enableShootingTests;
    }

    public void setEnableShootingTests(final boolean enableShootingTests) {
        this.enableShootingTests = enableShootingTests;
    }

    public Set<SystemUserPrivilege> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(final Set<SystemUserPrivilege> privileges) {
        this.privileges = privileges;
    }
}
