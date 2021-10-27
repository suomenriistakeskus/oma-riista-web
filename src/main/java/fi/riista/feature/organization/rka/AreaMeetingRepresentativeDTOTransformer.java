package fi.riista.feature.organization.rka;

import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationNameDTO;
import fi.riista.feature.organization.OrganisationRepository;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.feature.organization.person.PersonWithHunterNumberDTO;
import fi.riista.util.F;
import fi.riista.util.ListTransformer;
import fi.riista.util.jpa.CriteriaUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.function.Function;

@Component
public class AreaMeetingRepresentativeDTOTransformer extends ListTransformer<Occupation, AreaMeetingRepresentativeDTO> {

    @Resource
    private PersonRepository personRepository;

    @Resource
    private OrganisationRepository organisationRepository;

    @Nonnull
    @Override
    protected List<AreaMeetingRepresentativeDTO> transform(@Nonnull final List<Occupation> list) {
        final Function<Occupation, Person> personMapping = createPersonMapping(list, Occupation::getPerson);
        final Function<Occupation, Person> substituteMapping = createPersonMapping(list, Occupation::getSubstitute);
        final Function<Occupation, Organisation> organisationMapping = createOrganisationMapping(list);

        return F.mapNonNullsToList(list, occupation ->
                AreaMeetingRepresentativeDTO.create(OrganisationNameDTO.createWithOfficialCode(organisationMapping.apply(occupation)),
                PersonWithHunterNumberDTO.create(personMapping.apply(occupation)),
                PersonWithHunterNumberDTO.create(substituteMapping.apply(occupation))));
    }

    private Function<Occupation, Person> createPersonMapping(final Iterable<Occupation> occupations,
                                                             final Function<Occupation, Person> extractor) {

        return CriteriaUtils.singleQueryFunction(occupations, extractor, personRepository, true);
    }

    private Function<Occupation, Organisation> createOrganisationMapping(final Iterable<Occupation> occupations) {

        return CriteriaUtils.singleQueryFunction(occupations, Occupation::getOrganisation, organisationRepository, true);
    }
}
