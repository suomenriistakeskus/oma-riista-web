package fi.riista.feature.shootingtest;

import com.querydsl.jpa.impl.JPAQueryFactory;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.QPerson;
import fi.riista.util.ListTransformer;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Component
public class ShootingTestOfficialDTOTransformer extends ListTransformer<ShootingTestOfficial, ShootingTestOfficialDTO> {

    @Resource
    private JPAQueryFactory queryFactory;

    public static ShootingTestOfficialDTO create(@Nonnull final ShootingTestOfficial official,
                                                 @Nonnull final Person person) {

        final ShootingTestOfficialDTO dto = new ShootingTestOfficialDTO();

        dto.setId(official.getId());
        dto.setShootingTestEventId(official.getShootingTestEvent().getId());
        dto.setOccupationId(official.getOccupation().getId());
        dto.setPersonId(person.getId());

        dto.setFirstName(person.getByName());
        dto.setLastName(person.getLastName());

        return dto;
    }

    @Nonnull
    @Override
    protected List<ShootingTestOfficialDTO> transform(@Nonnull final List<ShootingTestOfficial> list) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        final Map<ShootingTestOfficial, Person> personMapping = getPersonOfShootingTestOfficial(list);

        return list.stream().map(official -> create(official, personMapping.get(official))).collect(toList());
    }

    private Map<ShootingTestOfficial, Person> getPersonOfShootingTestOfficial(final List<ShootingTestOfficial> officials) {
        final QShootingTestOfficial OFFICIAL = QShootingTestOfficial.shootingTestOfficial;
        final QOccupation OCCUPATION = QOccupation.occupation;
        final QPerson PERSON = QPerson.person;

        return queryFactory
                .select(OFFICIAL, PERSON)
                .from(OFFICIAL)
                .join(OFFICIAL.occupation, OCCUPATION)
                .join(OCCUPATION.person, PERSON)
                .where(OFFICIAL.in(officials))
                .fetch()
                .stream()
                .collect(toMap(t -> t.get(OFFICIAL), t -> t.get(PERSON)));
    }
}
