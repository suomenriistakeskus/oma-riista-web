package fi.riista.integration.metsastajarekisteri;

import com.google.common.base.MoreObjects;
import fi.riista.validation.FinnishHunterNumber;
import fi.riista.validation.FinnishSocialSecurityNumber;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.joda.time.LocalDate;

public class MetsastajaRekisteriPerson {

    public enum DeletionCode {
        DECEASED,
        OTHER
    }

    private DeletionCode deletionCode;

    private LocalDate deletionDate;

    // Henkilötunnus (11 merkkiä)
    @FinnishSocialSecurityNumber(checksumVerified = true)
    private String ssn;

    // Metsästäjänumero (8 numeroa)
    @FinnishHunterNumber
    private String hunterNumber;

    // Sukunimi
    @Length(max=255)
    private String lastName;

    // Etunimet välilyönnillä eroteltuna
    @Length(max=255)
    private String firstName;

    // Äidinkieli (2 merkkiä, esim fi, en, se)
    @Length(min=2, max=2)
    private String languageCode;

    // Riistanvuoksi lehden tilauskieli (2 merkkiä, esim fi, en, se)
    @Length(min=2, max=2)
    private String magazineLanguageCode;

    @Email
    @Length(max=255)
    private String email;

    // Kotikunta (3 numeroinen koodi)
    @Length(min = 3, max = 3)
    private String homeMunicipalityCode;

    // Minkä RHY jäsen henkilö on? Henkilö voi olla maksanut riistanhoitomaksun myös ilman RHY jäsenyyttä.
    @Length(min = 3, max = 3)
    private String membershipRhyOfficialCode;

    // Uusin riistanhoitomaksun maksupäivä ja kauden vuosinumero
    private LocalDate huntingPaymentOneDay;
    private Integer huntingPaymentOneYear;

    // Edeltävän kauden riistanhoitomaksun maksupäivä ja kauden vuosinumero
    private LocalDate huntingPaymentTwoDay;
    private Integer huntingPaymentTwoYear;

    // Laskun viitekoodit ja vastaavat metsästysvuodet
    private String invoiceReferenceCurrent;
    private String invoiceReferencePrevious;
    private Integer invoiceReferenceCurrentYear;
    private Integer invoiceReferencePreviousYear;

    // Mistä päivästä lähtien nykyinen maksettu riistanhoitomaksu on voimassa?
    private LocalDate huntingCardStart;

    // Mihin päivään asti nykyinen maksettu riistanhoitomaksu on voimassa?
    private LocalDate huntingCardEnd;

    // Metsästäjäntutkinnon suorituspäivämäärä
    private LocalDate hunterExamDate;

    // Metsästäjäntutkinnon vanhenemispäivä (nyk. 5 vuotta suorituksesta)
    private LocalDate hunterExamExpirationDate;

    // Metsästyskiellon alkamispäivämäärä
    private LocalDate huntingBanStart;

    // Metsästyskiellon loppumispäivämäärä
    private LocalDate huntingBanEnd;

    // VTJ Kotiosoite
    @Length(max = 255)
    private String streetAddress;

    @Length(max = 255)
    private String postalCode;

    @Length(max = 255)
    private String postOffice;

    @Length(max = 255)
    private String countryCode; // FI, SE,

    @Length(min = 2, max = 2)
    private String countryName;

    // Ei Metsästäjä-lehteä ("rasti ruutuun”)
    private boolean denyMagazine;

    // Osoiteen luovutus kielletty?
    private boolean forbidAddressDelegation;

    // Postitusesto / markkinointikielto
    private boolean forbidPosting;

    public DeletionCode getDeletionCode() {
        return deletionCode;
    }

    public void setDeletionCode(final DeletionCode deletionCode) {
        this.deletionCode = deletionCode;
    }

    public LocalDate getDeletionDate() {
        return deletionDate;
    }

    public void setDeletionDate(final LocalDate deletionDate) {
        this.deletionDate = deletionDate;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public String getHunterNumber() {
        return hunterNumber;
    }

    public void setHunterNumber(String hunterNumber) {
        this.hunterNumber = hunterNumber;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getMagazineLanguageCode() {
        return magazineLanguageCode;
    }

    public void setMagazineLanguageCode(String magazineLanguageCode) {
        this.magazineLanguageCode = magazineLanguageCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getHomeMunicipalityCode() {
        return homeMunicipalityCode;
    }

    public void setHomeMunicipalityCode(String homeMunicipalityCode) {
        this.homeMunicipalityCode = homeMunicipalityCode;
    }

    public String getMembershipRhyOfficialCode() {
        return membershipRhyOfficialCode;
    }

    public void setMembershipRhyOfficialCode(String membershipRhyOfficialCode) {
        this.membershipRhyOfficialCode = membershipRhyOfficialCode;
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

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPostOffice() {
        return postOffice;
    }

    public void setPostOffice(String postOffice) {
        this.postOffice = postOffice;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public boolean isDenyMagazine() {
        return denyMagazine;
    }

    public void setDenyMagazine(boolean denyMagazine) {
        this.denyMagazine = denyMagazine;
    }

    public boolean isForbidAddressDelegation() {
        return forbidAddressDelegation;
    }

    public void setForbidAddressDelegation(boolean forbidAddressDelegation) {
        this.forbidAddressDelegation = forbidAddressDelegation;
    }

    public boolean isForbidPosting() {
        return forbidPosting;
    }

    public void setForbidPosting(boolean forbidPosting) {
        this.forbidPosting = forbidPosting;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                //.add("ssn", ssn)
                .add("firstName", firstName)
                .add("lastName", lastName)
                .add("hunterNumber", hunterNumber)
                .toString();
    }
}
