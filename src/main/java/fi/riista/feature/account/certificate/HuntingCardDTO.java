package fi.riista.feature.account.certificate;

import fi.riista.feature.common.entity.HasBeginAndEndDate;
import fi.riista.feature.common.EnumLocaliser;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.LocalisedString;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.security.PrivateKey;
import java.util.List;
import java.util.Objects;

public class HuntingCardDTO {

    public static HuntingCardDTO create(@Nonnull final Person person,
                                        @Nonnull final List<Occupation> occupations,
                                        @Nonnull final LocalDate paymentDate,
                                        @Nullable final String languageCode,
                                        @Nullable final PrivateKey signatureKey,
                                        @Nonnull final EnumLocaliser enumLocaliser) {
        Objects.requireNonNull(person);
        Objects.requireNonNull(paymentDate);
        Objects.requireNonNull(occupations);
        Objects.requireNonNull(enumLocaliser);

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

        dto.occupations = F.mapNonNullsToList(occupations, occupation -> {
            Objects.requireNonNull(occupation);

            OccupationDTO occupationDTO = new OccupationDTO();

            final LocalisedString occupationName = enumLocaliser.getLocalisedString(occupation.getOccupationType());
            if (occupationName != null) {
                occupationDTO.occupationName = occupationName.getAnyTranslation(languageCode);
            }
            occupationDTO.organisationOfficialCode = occupation.getOrganisation().getOfficialCode();
            occupationDTO.organisationName = occupation.getOrganisation().getNameLocalisation().getTranslation(languageCode);
            occupationDTO.beginDate = occupation.getBeginDate();
            occupationDTO.endDate = occupation.getEndDate();

            return occupationDTO;
        });

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

        public String getOrganisationOfficialCode() {
            return organisationOfficialCode;
        }

        public String getOrganisationName() {
            return organisationName;
        }

        @Override
        public LocalDate getBeginDate() {
            return beginDate;
        }

        @Override
        public LocalDate getEndDate() {
            return endDate;
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

    private List<OccupationDTO> occupations;

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

    public List<OccupationDTO> getOccupations() {
        return occupations;
    }
}
