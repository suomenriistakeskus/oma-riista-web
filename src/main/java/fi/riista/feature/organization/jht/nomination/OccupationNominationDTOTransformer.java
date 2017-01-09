package fi.riista.feature.organization.jht.nomination;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.util.ListTransformer;
import fi.riista.util.jpa.CriteriaUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OccupationNominationDTOTransformer extends ListTransformer<OccupationNomination, OccupationNominationDTO> {

    @Resource
    private PersonRepository personRepository;

    @Resource
    private UserRepository userRepository;

    @Resource
    private RiistanhoitoyhdistysRepository rhyRepository;

    @Resource
    private OccupationRepository occupationRepository;

    @Nonnull
    @Override
    protected List<OccupationNominationDTO> transform(@Nonnull final List<OccupationNomination> list) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        final Function<OccupationNomination, Person> personMapping = getNominationToPersonMapping(list);
        final Function<OccupationNomination, Riistanhoitoyhdistys> rhyMapping = getNominationToRhyMapping(list);
        final Function<OccupationNomination, SystemUser> moderatorMapping = getNominationToModeratorMapping(list);
        final Function<OccupationNomination, Occupation> nominationMapping = getNominationToOccupationMapping(list);

        return list.stream().map(nomination -> {
            final Person person = personMapping.apply(nomination);
            final Riistanhoitoyhdistys rhy = rhyMapping.apply(nomination);
            final SystemUser moderator = moderatorMapping.apply(nomination);
            final Occupation occupation = nominationMapping.apply(nomination);

            return OccupationNominationDTO.create(nomination, person, rhy, moderator, occupation);
        }).collect(Collectors.toList());
    }

    private Function<OccupationNomination, Person> getNominationToPersonMapping(
            final Iterable<OccupationNomination> list) {
        return CriteriaUtils.singleQueryFunction(list, OccupationNomination::getPerson, personRepository, true);
    }

    private Function<OccupationNomination, Riistanhoitoyhdistys> getNominationToRhyMapping(
            final Iterable<OccupationNomination> list) {
        return CriteriaUtils.singleQueryFunction(list, OccupationNomination::getRhy, rhyRepository, true);
    }

    private Function<OccupationNomination, SystemUser> getNominationToModeratorMapping(
            final Iterable<OccupationNomination> list) {
        return CriteriaUtils.singleQueryFunction(list, OccupationNomination::getModeratorUser, userRepository, false);
    }

    private Function<OccupationNomination, Occupation> getNominationToOccupationMapping(
            final Iterable<OccupationNomination> list) {
        return CriteriaUtils.singleQueryFunction(list, OccupationNomination::getOccupation, occupationRepository, false);
    }
}
