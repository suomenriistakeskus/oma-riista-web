package fi.riista.feature.gamediary.mobile;

import com.querydsl.jpa.impl.JPAUtil;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.gamediary.srva.SrvaJpaUtils;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.feature.permit.decision.PermitDecisionRepository;
import fi.riista.util.F;
import fi.riista.util.ListTransformer;
import fi.riista.util.jpa.CriteriaUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.function.Function;

@Component
public class MobileHuntingGroupOccupationDTOTransformer extends ListTransformer<Occupation, MobileHuntingGroupOccupationDTO> {

    @Resource
    private PersonRepository personRepository;

    @Nonnull
    @Override
    protected List<MobileHuntingGroupOccupationDTO> transform(@Nonnull final List<Occupation> list) {
        final Function<Occupation, Person> occupationToPersonMapping = getOccupationToPersonMapping(list);
        return F.mapNonNullsToList(list, occupation ->
                new MobileHuntingGroupOccupationDTO(occupation, occupationToPersonMapping.apply(occupation)));
    }

    @Nonnull
    private Function<Occupation, Person> getOccupationToPersonMapping(final Iterable<Occupation> occupations) {
        return CriteriaUtils.singleQueryFunction(occupations, Occupation::getPerson, personRepository, true);
    }
}
