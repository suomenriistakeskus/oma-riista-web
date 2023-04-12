package fi.riista.feature.account.certificate;

import fi.riista.feature.account.AccountShootingTestDTO;
import fi.riista.feature.common.entity.HasBeginAndEndDate;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class HuntingCardDTO {

    public static HuntingCardDTO create(@Nonnull final Person person,
                                        @Nonnull final List<OccupationDTO> occupations,
                                        @Nonnull final LocalDate paymentDate,
                                        @Nonnull final List<AccountShootingTestDTO> qualifiedShootingTests,
                                        @Nullable final String languageCode,
                                        @Nullable final PrivateKey signatureKey) {
        requireNonNull(person);
        requireNonNull(occupations);
        requireNonNull(paymentDate);
        requireNonNull(qualifiedShootingTests);

        final HuntingCardDTO dto = new HuntingCardDTO();

        dto.firstName = person.getFirstName();
        dto.lastName = person.getLastName();
        dto.byName = person.getByName();
        dto.hunterNumber = person.getHunterNumber();
        dto.dateOfBirth = person.parseDateOfBirth();

        if (person.getRhyMembership() != null) {
            dto.rhyName = person.getRhyMembership().getNameLocalisation().getAnyTranslation(languageCode);
            dto.rhyOfficialCode = person.getRhyMembership().getOfficialCode();
        }

        if (person.getAddress() != null) {
            dto.streetAddress = person.getAddress().getStreetAddress();
            dto.postalCode = person.getAddress().getPostalCode();
            dto.postOffice = person.getAddress().getCity();
        }

        if (person.hasHomeMunicipality()) {
            dto.homeMunicipalityCode = person.getHomeMunicipalityCode();
            dto.homeMunicipalityName = person.getHomeMunicipalityName().getAnyTranslation(languageCode);
        }

        dto.paymentDate = paymentDate;
        dto.huntingCardStart = person.getHuntingCardStart();
        dto.huntingCardEnd = person.getHuntingCardEnd();

        if (signatureKey != null) {
            dto.qrCode = HuntingCardQRCodeGenerator.forPerson(person).build(signatureKey, languageCode);
        }

        dto.isMultipage = (occupations.size() + qualifiedShootingTests.size()) > 5;

        if (dto.isMultipage && occupations.size() > 10) {
            dto.occupationsPage1 = occupations.subList(0, 10);
            dto.occupationsPage2 = occupations.subList(10, occupations.size());
        } else {
            dto.occupationsPage1 = occupations;
        }

        dto.shootingTests = qualifiedShootingTests;

        return dto;
    }

    public static class OccupationDTO implements HasBeginAndEndDate {

        private String occupationName;
        private String organisationOfficialCode;
        private String organisationName;

        private LocalDate beginDate;
        private LocalDate endDate;

        public String getOccupationName() {
            return occupationName;
        }

        public void setOccupationName(final String occupationName) {
            this.occupationName = occupationName;
        }

        public String getOrganisationOfficialCode() {
            return organisationOfficialCode;
        }

        public void setOrganisationOfficialCode(final String organisationOfficialCode) {
            this.organisationOfficialCode = organisationOfficialCode;
        }

        public String getOrganisationName() {
            return organisationName;
        }

        public void setOrganisationName(final String organisationName) {
            this.organisationName = organisationName;
        }

        @Override
        public LocalDate getBeginDate() {
            return beginDate;
        }

        public void setBeginDate(final LocalDate beginDate) {
            this.beginDate = beginDate;
        }

        @Override
        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(final LocalDate endDate) {
            this.endDate = endDate;
        }
    }

    private String firstName;
    private String byName;
    private String lastName;
    private String hunterNumber;
    private LocalDate dateOfBirth;

    private String rhyName;
    private String rhyOfficialCode;
    private String streetAddress;
    private String postalCode;
    private String postOffice;

    private String homeMunicipalityName;
    private String homeMunicipalityCode;

    private LocalDate huntingCardStart;
    private LocalDate huntingCardEnd;

    private LocalDate paymentDate;
    private LocalDate currentDate = DateUtil.today();

    private String qrCode;

    private List<OccupationDTO> occupationsPage1;
    private List<OccupationDTO> occupationsPage2;

    private List<AccountShootingTestDTO> shootingTests;

    private boolean isMultipage;

    public String getFirstName() {
        return firstName;
    }

    public String getByName() {
        return byName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getHunterNumber() {
        return hunterNumber;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getRhyName() {
        return rhyName;
    }

    public String getRhyOfficialCode() {
        return rhyOfficialCode;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getPostOffice() {
        return postOffice;
    }

    public String getHomeMunicipalityName() {
        return homeMunicipalityName;
    }

    public String getHomeMunicipalityCode() {
        return homeMunicipalityCode;
    }

    public LocalDate getHuntingCardStart() {
        return huntingCardStart;
    }

    public LocalDate getHuntingCardEnd() {
        return huntingCardEnd;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public LocalDate getCurrentDate() {
        return currentDate;
    }

    public String getQrCode() {
        return qrCode;
    }

    public List<OccupationDTO> getOccupationsPage1() {
        return occupationsPage1;
    }

    public List<OccupationDTO> getOccupationsPage2() {
        return occupationsPage2;
    }

    public List<AccountShootingTestDTO> getShootingTests() {
        return shootingTests;
    }

    public boolean getIsMultipage() {
        return isMultipage;
    }
}
