package fi.riista.integration.metsastajarekisteri;

import com.google.common.base.MoreObjects;
import fi.riista.integration.metsastajarekisteri.person.DeletionCode;
import fi.riista.validation.FinnishHunterNumber;
import fi.riista.validation.FinnishSocialSecurityNumber;
import javax.validation.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.joda.time.LocalDate;

// LocalDate     = YYYYMMdd (20170131)
// boolean       = (1 = true, 0 = false)
// CSV quote     = "  (required only to escape delimiter in field value)
// CSV multiline = true
// CSV delimiter = ;
// CSV format    = ;;;"";;"a""b""c";;
// CSV example   = 11111111;111111-1034;19000101;;;Meikäläinen;Matti Esko;fi;fi;osoite@email.fi;123;123;20160101;20170101;2016;2017;20160801;20170731;19900101;20200101;;;Kadunnimi 123;01110;Kaupunki;FI;Helsinki;1;1;1;12345678901;12345678902;2017;2016

public class InnofactorImportFileLine {

    // Metsästäjänumero (8 numeroa)
    @FinnishHunterNumber
    private String hunterNumber;

    // Henkilötunnus (11 merkkiä)
    @FinnishSocialSecurityNumber(checksumVerified = true)
    private String ssn;

    // Milloin rivin tiedot ovat (mahdollisesti) muuttuneet viimeksi?
    private LocalDate modificationDate;

    // Milloin on merkitty poistetuksi? (muulloin tyhjä)
    private LocalDate deletionDate;

    // Syykoodi poistamiselle (muulloin tyhjä)
    private DeletionCode deletionStatus;

    // Sukunimi
    @Length(max = 255)
    private String lastName;

    // Etunimet välilyönnillä eroteltuna
    @Length(max = 255)
    private String firstName;

    // Äidinkieli (2 merkkiä, esim fi, en, se)
    @Length(min = 2, max = 2)
    private String languageCode;

    // Riistanvuoksi lehden tilauskieli (2 merkkiä, esim fi, en, se)
    @Length(min = 2, max = 2)
    private String magazineLanguageCode;

    @Email
    @Length(max = 255)
    private String email;

    // Kotikunta (3 numeroinen koodi)
    @Length(min = 3, max = 3)
    private String homeMunicipalityCode;

    // Minkä RHY jäsen henkilö on? Henkilö voi olla maksanut riistanhoitomaksun myös ilman RHY jäsenyyttä.
    @Length(min = 3, max = 3)
    private String membershipRhyOfficialCode;

    // Uusin riistanhoitomaksun maksupäivä ja vastaava kausi
    private LocalDate huntingPaymentOneDay;
    private Integer huntingPaymentOneYear;

    // Edeltävän kauden riistanhoitomaksun maksupäivä ja vastaava kausi
    private LocalDate huntingPaymentTwoDay;
    private Integer huntingPaymentTwoYear;

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

    // Kotiosoite
    @Length(max = 255)
    private String streetAddress;

    @Length(max = 255)
    private String postalCode;

    @Length(max = 255)
    private String postOffice;

    @Length(min = 2, max = 2)
    private String countryCode; // FI, SE, XX = Ei virallista maakoodia (optional)

    @Length(max = 255)
    private String countryName; // (optional)

    // Ei Metsästäjä-lehteä ("rasti ruutuun”)
    private boolean forbidMagazine; //  1 = true 0 = false

    // Osoiteen luovutus kielletty?
    private boolean forbidAddressDelegation; //  1 = true 0 = false

    // Postitusesto / markkinointikielto
    private boolean forbidPosting; //  1 = true 0 = false

    // Laskun viitekoodit ja vastaavat metsästysvuodet
    private String invoiceReferenceCurrent;
    private String invoiceReferencePrevious;
    private Integer invoiceReferenceCurrentYear;
    private Integer invoiceReferencePreviousYear;

    // Syntymäaika
    private LocalDate dateOfBirth;

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("hunterNumber", hunterNumber)
                .add("firstName", firstName)
                .add("lastName", lastName)
                .toString();
    }

    public String getHunterNumber() {
        return hunterNumber;
    }

    public void setHunterNumber(final String hunterNumber) {
        this.hunterNumber = hunterNumber;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(final String ssn) {
        this.ssn = ssn;
    }

    public LocalDate getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate(final LocalDate modificationDate) {
        this.modificationDate = modificationDate;
    }

    public LocalDate getDeletionDate() {
        return deletionDate;
    }

    public void setDeletionDate(final LocalDate deletionDate) {
        this.deletionDate = deletionDate;
    }

    public DeletionCode getDeletionStatus() {
        return deletionStatus;
    }

    public void setDeletionStatus(final DeletionCode deletionStatus) {
        this.deletionStatus = deletionStatus;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(final String languageCode) {
        this.languageCode = languageCode;
    }

    public String getMagazineLanguageCode() {
        return magazineLanguageCode;
    }

    public void setMagazineLanguageCode(final String magazineLanguageCode) {
        this.magazineLanguageCode = magazineLanguageCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getHomeMunicipalityCode() {
        return homeMunicipalityCode;
    }

    public void setHomeMunicipalityCode(final String homeMunicipalityCode) {
        this.homeMunicipalityCode = homeMunicipalityCode;
    }

    public String getMembershipRhyOfficialCode() {
        return membershipRhyOfficialCode;
    }

    public void setMembershipRhyOfficialCode(final String membershipRhyOfficialCode) {
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

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(final String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(final String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPostOffice() {
        return postOffice;
    }

    public void setPostOffice(final String postOffice) {
        this.postOffice = postOffice;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(final String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(final String countryName) {
        this.countryName = countryName;
    }

    public boolean isForbidMagazine() {
        return forbidMagazine;
    }

    public void setForbidMagazine(final boolean forbidMagazine) {
        this.forbidMagazine = forbidMagazine;
    }

    public boolean isForbidAddressDelegation() {
        return forbidAddressDelegation;
    }

    public void setForbidAddressDelegation(final boolean forbidAddressDelegation) {
        this.forbidAddressDelegation = forbidAddressDelegation;
    }

    public boolean isForbidPosting() {
        return forbidPosting;
    }

    public void setForbidPosting(final boolean forbidPosting) {
        this.forbidPosting = forbidPosting;
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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(final LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}
