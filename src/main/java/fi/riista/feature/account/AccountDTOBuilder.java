package fi.riista.feature.account;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.address.AddressDTO;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class AccountDTOBuilder {

    private AccountDTO dto = new AccountDTO();

    public static AccountDTOBuilder create() {
        return new AccountDTOBuilder();
    }

    private AccountDTOBuilder() {
    }

    public AccountDTO build() {
        final AccountDTO ret = this.dto;
        this.dto = new AccountDTO();
        return ret;
    }

    public AccountDTOBuilder withUser(@Nonnull final SystemUser user) {
        Objects.requireNonNull(user);

        dto.setId(user.getId());
        dto.setRev(user.getConsistencyVersion());
        dto.setRole(user.getRole());
        dto.setUsername(user.getUsername());
        dto.setTimeZone(user.getTimeZone());
        dto.setLocale(user.getLocale());

        if (user.getPerson() != null) {
            withPerson(user.getPerson());
        } else {
            dto.setEmail(user.getEmail());
            dto.setFirstName(user.getFirstName());
            dto.setByName(user.getFirstName());
            dto.setLastName(user.getLastName());
        }

        return this;
    }

    public AccountDTOBuilder withRememberMe(final boolean value) {
        dto.setRememberMe(value);
        return this;
    }

    public AccountDTOBuilder withPerson(@Nonnull final Person person) {
        Objects.requireNonNull(person);

        dto.setPersonId(person.getId());
        dto.setSsnMasked(person.getSsnMasked());
        dto.setEmail(person.getEmail());
        dto.setFirstName(person.getFirstName());
        dto.setByName(person.getByName());
        dto.setLastName(person.getLastName());
        dto.setHomeMunicipality(person.getHomeMunicipalityName().getAnyTranslation(person.getLanguageCode()));
        dto.setLanguageCode(person.getLanguageCode());
        dto.setPhoneNumber(person.getPhoneNumber());
        dto.setRegistered(person.isRegistered());
        dto.setActive(person.isActive());
        dto.setHunterNumber(person.getHunterNumber());
        dto.setHuntingCardStart(person.getHuntingCardStart());
        dto.setHuntingCardEnd(person.getHuntingCardEnd());

        final int huntingYear = DateUtil.getFirstCalendarYearOfCurrentHuntingYear();
        final Optional<LocalDate> paymentDate = person.getHuntingPaymentDateForNextOrCurrentSeason();

        dto.setHuntingPaymentPending(person.isPaymentPendingForHuntingYear(huntingYear));
        dto.setAllowPrintCertificate(person.canPrintCertificate() && paymentDate.isPresent());
        dto.setHuntingPaymentDate(paymentDate.orElse(null));
        dto.setHuntingPaymentPdfYears(person.getHuntingPaymentPdfYears());

        dto.setHunterExamDate(person.getHunterExamDate());
        dto.setHunterExamExpirationDate(person.getHunterExamExpirationDate());
        dto.setHunterExamValid(person.isHunterExamValidNow());

        dto.setHuntingBanStart(person.getHuntingBanStart());
        dto.setHuntingBanEnd(person.getHuntingBanEnd());
        dto.setHuntingBanActive(person.isHuntingBanActiveNow());

        dto.setDenyPost(person.isDenyPost());
        dto.setDenyMagazine(person.isDenyMagazine());
        dto.setMagazineLanguageCode(person.getMagazineLanguageCode());
        dto.setMrSyncTime(person.getMrSyncTime());

        if (person.getRhyMembership() != null) {
            dto.setRhyMembership(OrganisationNameDTO.createWithOfficialCode(person.getRhyMembership()));
        }

        dto.setEnableSrva(person.isEnableSrva() == null ? false : person.isEnableSrva());

        // If there is no address, we return blank address which user can then edit.
        final AddressDTO addressDTO = AddressDTO.from(Optional.ofNullable(person.getAddress()).orElseGet(Address::new));
        addressDTO.setEditable(person.isAddressEditable());
        dto.setAddress(addressDTO);
        dto.setAddressSource(person.getAddressSource());

        dto.setOccupations(F.mapNonNullsToList(person.getNotClubSpecificOccupations(), MyOccupationDTO::create));
        dto.setClubOccupations(createClubOccupationDTOs(person.getClubSpecificOccupations()));

        return this;
    }

    private static List<MyClubOccupationDTO> createClubOccupationDTOs(final Collection<Occupation> clubSpecificOccupations) {
        final Map<Organisation, List<Occupation>> organisationToSubOrganisationOccupations =
                F.nullSafeGroupBy(clubSpecificOccupations, o -> o.getOrganisation().getParentOrganisation());

        return clubSpecificOccupations.stream()
                .filter(o -> o.getOrganisation().getOrganisationType() == OrganisationType.CLUB)
                .map(o -> MyClubOccupationDTO.create(o, organisationToSubOrganisationOccupations.get(o.getOrganisation())))
                .collect(Collectors.toList());
    }

    public AccountDTOBuilder withRoles(@Nullable final List<AccountRoleDTO> roles) {
        dto.setAccountRoles(roles);
        return this;
    }
}
