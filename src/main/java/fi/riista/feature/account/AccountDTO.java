package fi.riista.feature.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.dto.DoNotValidate;
import fi.riista.feature.common.entity.BaseEntityDTO;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.address.AddressSource;
import fi.riista.util.Patterns;
import fi.riista.validation.FinnishHunterNumber;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.SafeHtml;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
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

    private boolean allowPrintCertificate;

    @Size(min = 2, max = 63)
    @SafeHtml(whitelistType = SafeHtml.WhiteListType.NONE)
    private String username;

    private SystemUser.Role role;

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
    private String ssnMasked;

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

    private boolean denyPost;

    private boolean denyMagazine;

    private List<MyOccupationDTO> occupations;
    private List<MyClubOccupationDTO> clubOccupations;

    private boolean enableSrva;

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

    public final void setUsername(String username) {
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

    public void setByName(String byName) {
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

    public void setAccountRoles(List<AccountRoleDTO> accountRoles) {
        this.accountRoles = accountRoles;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public String getSsnMasked() {
        return ssnMasked;
    }

    public void setSsnMasked(String ssnMasked) {
        this.ssnMasked = ssnMasked;
    }

    public String getHomeMunicipality() {
        return homeMunicipality;
    }

    public void setHomeMunicipality(String homeMunicipality) {
        this.homeMunicipality = homeMunicipality;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public AddressDTO getAddress() {
        return address;
    }

    public void setAddress(AddressDTO address) {
        this.address = address;
    }

    public AddressSource getAddressSource() {
        return addressSource;
    }

    public void setAddressSource(AddressSource addressSource) {
        this.addressSource = addressSource;
    }

    public DateTime getMrSyncTime() {
        return mrSyncTime;
    }

    public void setMrSyncTime(DateTime mrSyncTime) {
        this.mrSyncTime = mrSyncTime;
    }

    public String getHunterNumber() {
        return hunterNumber;
    }

    public void setHunterNumber(String hunterNumber) {
        this.hunterNumber = hunterNumber;
    }

    public OrganisationNameDTO getRhyMembership() {
        return rhyMembership;
    }

    public void setRhyMembership(OrganisationNameDTO rhyMembership) {
        this.rhyMembership = rhyMembership;
    }

    public LocalDate getHuntingCardStart() {
        return huntingCardStart;
    }

    public void setHuntingCardStart(LocalDate huntingCardStart) {
        this.huntingCardStart = huntingCardStart;
    }

    public LocalDate getHuntingCardEnd() {
        return huntingCardEnd;
    }

    public void setHuntingCardEnd(LocalDate huntingCardEnd) {
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

    public void setHunterExamDate(LocalDate hunterExamDate) {
        this.hunterExamDate = hunterExamDate;
    }

    public LocalDate getHunterExamExpirationDate() {
        return hunterExamExpirationDate;
    }

    public void setHunterExamExpirationDate(LocalDate hunterExamExpirationDate) {
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

    public void setHuntingBanStart(LocalDate huntingBanStart) {
        this.huntingBanStart = huntingBanStart;
    }

    public LocalDate getHuntingBanEnd() {
        return huntingBanEnd;
    }

    public void setHuntingBanEnd(LocalDate huntingBanEnd) {
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

    public void setMagazineLanguageCode(String magazineLanguageCode) {
        this.magazineLanguageCode = magazineLanguageCode;
    }

    public boolean isDenyPost() {
        return denyPost;
    }

    public void setDenyPost(boolean denyPost) {
        this.denyPost = denyPost;
    }

    public boolean isDenyMagazine() {
        return denyMagazine;
    }

    public void setDenyMagazine(boolean denyMagazine) {
        this.denyMagazine = denyMagazine;
    }

    public List<MyOccupationDTO> getOccupations() {
        return occupations;
    }

    public void setOccupations(List<MyOccupationDTO> occupations) {
        this.occupations = occupations;
    }

    public List<MyClubOccupationDTO> getClubOccupations() {
        return clubOccupations;
    }

    public void setClubOccupations(List<MyClubOccupationDTO> clubOccupations) {
        this.clubOccupations = clubOccupations;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public boolean isRegistered() {
        return registered;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(final boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
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

    public void setEnableSrva(boolean enableSrva) {
        this.enableSrva = enableSrva;
    }
}
