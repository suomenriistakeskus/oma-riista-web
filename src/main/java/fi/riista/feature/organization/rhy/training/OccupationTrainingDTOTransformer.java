package fi.riista.feature.organization.rhy.training;

import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
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
public class OccupationTrainingDTOTransformer extends ListTransformer<OccupationTraining, OccupationTrainingDTO> {

    @Resource
    private PersonRepository personRepository;

    @Nonnull
    @Override
    protected List<OccupationTrainingDTO> transform(@Nonnull final List<OccupationTraining> list) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        final Function<OccupationTraining, Person> personMapping = getTrainingToPersonMapping(list);

        return list.stream().map(nomination -> {
            final Person person = personMapping.apply(nomination);
            final OccupationTrainingDTO.PersonDTO personDto = OccupationTrainingDTO.PersonDTO.create(person);

            return OccupationTrainingDTO.create(nomination, personDto);
        }).collect(Collectors.toList());
    }

    private Function<OccupationTraining, Person> getTrainingToPersonMapping(final Iterable<OccupationTraining> list) {
        return CriteriaUtils.singleQueryFunction(list, OccupationTraining::getPerson, personRepository, true);
    }
}
