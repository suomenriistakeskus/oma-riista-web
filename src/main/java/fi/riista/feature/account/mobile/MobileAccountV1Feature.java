package fi.riista.feature.account.mobile;

import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.person.Person;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.SortedSet;

import static java.util.Collections.singleton;

@Component
public class MobileAccountV1Feature extends MobileAccountFeature {

    @Transactional(readOnly = true)
    @Override
    public MobileAccountV1DTO getMobileAccount() {
        final Person person = activeUserService.requireActivePerson();
        final String username = activeUserService.getActiveUsernameOrNull();

        final SortedSet<Integer> years = getBeginningCalendarYearsOfHuntingYearsContainingHarvests(person);
        final Iterable<Occupation> occupations =
                occupationRepository.findActiveByPersonAndOrganisationTypes(person, singleton(OrganisationType.RHY));

        return MobileAccountV1DTO.create(
                username,
                person,
                person.getAddress(),
                person.getRhyMembership(),
                years,
                mobileOccupationDTOFactory.create(occupations));
    }
}
