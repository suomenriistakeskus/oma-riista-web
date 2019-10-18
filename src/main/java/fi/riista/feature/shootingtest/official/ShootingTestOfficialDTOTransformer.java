package fi.riista.feature.shootingtest.official;

import com.querydsl.jpa.impl.JPAQueryFactory;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.QPerson;
import fi.riista.util.F;
import fi.riista.util.ListTransformer;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static com.querydsl.core.group.GroupBy.groupBy;
import static java.util.Collections.emptyList;

@Component
public class ShootingTestOfficialDTOTransformer extends ListTransformer<ShootingTestOfficial, ShootingTestOfficialDTO> {

    @Resource
    private JPAQueryFactory queryFactory;

    @Nonnull
    @Override
    protected List<ShootingTestOfficialDTO> transform(@Nonnull final List<ShootingTestOfficial> list) {
        if (list.isEmpty()) {
            return emptyList();
        }

        final Map<ShootingTestOfficial, Person> personMapping = getPersonOfShootingTestOfficial(list);

        return F.mapNonNullsToList(list, official -> {
            return ShootingTestOfficialDTO.create(official, personMapping.get(official));
        });
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
                .transform(groupBy(OFFICIAL).as(PERSON));
    }
}
