package fi.riista.feature.organization.jht.training;

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
public class JHTTrainingDTOTransformer extends ListTransformer<JHTTraining, JHTTrainingDTO> {

    @Resource
    private PersonRepository personRepository;

    @Nonnull
    @Override
    protected List<JHTTrainingDTO> transform(@Nonnull final List<JHTTraining> list) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        final Function<JHTTraining, Person> personMapping = getTrainingToPersonMapping(list);

        return list.stream().map(nomination -> {
            final Person person = personMapping.apply(nomination);
            final JHTTrainingDTO.PersonDTO personDto = JHTTrainingDTO.PersonDTO.create(person);

            return JHTTrainingDTO.create(nomination, personDto);
        }).collect(Collectors.toList());
    }

    private Function<JHTTraining, Person> getTrainingToPersonMapping(final Iterable<JHTTraining> list) {
        return CriteriaUtils.singleQueryFunction(list, JHTTraining::getPerson, personRepository, true);
    }
}
