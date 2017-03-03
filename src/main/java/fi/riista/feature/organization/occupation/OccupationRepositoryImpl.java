package fi.riista.feature.organization.occupation;

import com.google.common.collect.Iterables;
import com.querydsl.core.group.GroupBy;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.QOrganisation;
import fi.riista.feature.organization.person.QPerson;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;

@Repository
public class OccupationRepositoryImpl implements OccupationRepositoryCustom {

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Override
    @Transactional(readOnly = true)
    public List<Occupation> findActiveByOrganisation(final Organisation organisation) {
        Objects.requireNonNull(organisation, "organisation is null");

        final QOccupation occupation = QOccupation.occupation;

        return jpqlQueryFactory.selectFrom(occupation)
                .join(occupation.person).fetchJoin()
                .join(occupation.organisation).fetchJoin()
                .where(occupation.organisation.eq(organisation), occupation.validAndNotDeleted())
                .fetch();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Occupation> findActiveByParentOrganisation(final Organisation parentOrganisation) {
        Objects.requireNonNull(parentOrganisation, "organisation is null");

        final QOccupation occupation = QOccupation.occupation;
        final QOrganisation organisation = QOrganisation.organisation;

        final JPQLQuery<Organisation> childOrganisations = JPAExpressions
                .selectFrom(organisation).where(organisation.parentOrganisation.eq(parentOrganisation));

        return jpqlQueryFactory.selectFrom(occupation)
                .join(occupation.person).fetchJoin()
                .join(occupation.organisation).fetchJoin()
                .where(occupation.validAndNotDeleted(), occupation.organisation.in(childOrganisations))
                .fetch();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Set<Occupation>> findActiveByOccupationTypeGroupByOrganisationId(final OccupationType occupationType) {
        Objects.requireNonNull(occupationType, "occupationType is null");

        final QOccupation occupation = QOccupation.occupation;

        return jpqlQueryFactory.selectFrom(occupation)
                .join(occupation.person).fetchJoin()
                .where(occupation.validAndNotDeleted(), occupation.occupationType.eq(occupationType))
                .transform(GroupBy.groupBy(occupation.organisation.id).as(GroupBy.set(occupation)));
    }

    @Override
    @Transactional
    public void deleteByOrganisation(final Organisation organisation) {
        final QOccupation occupation = QOccupation.occupation;
        jpqlQueryFactory
                .delete(occupation)
                .where(occupation.organisation.eq(organisation))
                .execute();
    }

    @Override
    @Transactional
    public void deleteOccupationInFuture(final Organisation organisation,
                                         final OccupationType occupationType,
                                         final Person person) {
        Objects.requireNonNull(organisation, "organisation is null");
        Objects.requireNonNull(occupationType, "occupationType is null");
        Objects.requireNonNull(person, "person is null");

        final QOccupation occupation = QOccupation.occupation;

        jpqlQueryFactory
                .delete(occupation)
                .where(occupation.person.eq(person)
                        .and(occupation.organisation.eq(organisation))
                        .and(occupation.occupationType.eq(occupationType))
                        .and(occupation.beginDate.isNotNull())
                        .and(occupation.beginDate.gt(DateUtil.today())))
                .execute();
    }

    @Override
    @Transactional
    public void endOccupationsForDeceased() {
        final QOccupation occupation = QOccupation.occupation;
        final QPerson person = QPerson.person;

        jpqlQueryFactory.update(occupation)
                .where(occupation.validAndNotDeleted()
                        .and(occupation.person.in(JPAExpressions.selectFrom(person)
                                .where(person.deletionCode.eq(Person.DeletionCode.D)))))
                .set(occupation.endDate, DateUtil.today().minusDays(1))
                .execute();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean alreadyExists(final OccupationDTO dto) {
        Objects.requireNonNull(dto, "dto is null");
        Objects.requireNonNull(dto.getOrganisationId(), "organisationId is null");
        Objects.requireNonNull(dto.getPersonId(), "personId is null");
        Objects.requireNonNull(dto.getOccupationType(), "occupationType is null");

        final QOccupation occupation = QOccupation.occupation;

        return 0 < jpqlQueryFactory.from(occupation)
                .select(occupation.id.count())
                .where(occupation.organisation.id.eq(dto.getOrganisationId()),
                        occupation.person.id.eq(dto.getPersonId()),
                        occupation.occupationType.eq(dto.getOccupationType()),
                        occupation.validAndNotDeleted())
                .fetchCount();
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Organisation, Occupation> listCoordinators(final List<Organisation> organisations) {
        final List<Organisation> rhys = F.filterToList(organisations,
                org -> org.getOrganisationType() == OrganisationType.RHY);

        if (rhys.isEmpty()) {
            return emptyMap();
        }

        final QOccupation occupation = QOccupation.occupation;

        final List<Occupation> coordinators = jpqlQueryFactory.selectFrom(occupation)
                .join(occupation.organisation)
                .where(occupation.occupationType.eq(OccupationType.TOIMINNANOHJAAJA),
                        occupation.organisation.in(rhys),
                        occupation.validAndNotDeleted())
                .fetch();

        return F.nullSafeGroupBy(coordinators, Occupation::getOrganisation).entrySet().stream()
                .collect(toMap(Map.Entry::getKey, e -> Iterables.getFirst(e.getValue(), null)));
    }
}
