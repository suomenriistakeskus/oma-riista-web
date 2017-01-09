package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.person.Person;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.SortedSet;

import static fi.riista.feature.gamediary.harvest.HarvestSpecVersion._1;
import static fi.riista.feature.gamediary.harvest.HarvestSpecVersion._2;
import static java.util.Collections.singleton;

@Service
public class MobileGameDiaryV1Feature extends MobileGameDiaryFeature {

    @Override
    public EnumSet<HarvestSpecVersion> getSupportedSpecVersions() {
        return EnumSet.of(_1, _2);
    }

    @Transactional(readOnly = true)
    @Override
    public MobileAccountV1DTO getMobileAccount() {
        final Person person = activeUserService.requireActivePerson();
        final String username = activeUserService.getActiveUserInfo().getUsername();

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
