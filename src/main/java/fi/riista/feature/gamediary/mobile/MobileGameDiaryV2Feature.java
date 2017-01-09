package fi.riista.feature.gamediary.mobile;

import fi.riista.feature.error.MessageExposableValidationException;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.F;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.SortedSet;

import static fi.riista.feature.gamediary.harvest.HarvestSpecVersion._3;
import static fi.riista.feature.gamediary.harvest.HarvestSpecVersion._4;
import static java.util.Collections.singleton;

@Service
public class MobileGameDiaryV2Feature extends MobileGameDiaryFeature {

    @Override
    public EnumSet<HarvestSpecVersion> getSupportedSpecVersions() {
        return EnumSet.of(_3, _4);
    }

    @Transactional(readOnly = true)
    @Override
    public MobileAccountV2DTO getMobileAccount() {
        final Person person = activeUserService.requireActivePerson();
        final String username = activeUserService.getActiveUserInfo().getUsername();

        final SortedSet<Integer> harvestYears = getBeginningCalendarYearsOfHuntingYearsContainingHarvests(person);
        final SortedSet<Integer> observationYears =
                getBeginningCalendarYearsOfHuntingYearsContainingObservations(person);
        final Iterable<Occupation> occupations =
                occupationRepository.findActiveByPersonAndOrganisationTypes(person, singleton(OrganisationType.RHY));

        return MobileAccountV2DTO.create(
                username,
                person,
                person.getAddress(),
                person.getRhyMembership(),
                harvestYears,
                observationYears,
                mobileOccupationDTOFactory.create(occupations));
    }

    @Override
    protected void assertHarvestDTOIsValid(final MobileHarvestDTO dto) {
        super.assertHarvestDTOIsValid(dto);

        if (dto.getId() == null && dto.getMobileClientRefId() == null) {
            throw new MessageExposableValidationException("mobileClientRefId must not be null");
        }

        // Specimens are allowed to be null on creation.
        if (F.hasId(dto) && dto.getSpecimens() == null) {
            throw new MessageExposableValidationException("specimens must not be null");
        }
    }

}
