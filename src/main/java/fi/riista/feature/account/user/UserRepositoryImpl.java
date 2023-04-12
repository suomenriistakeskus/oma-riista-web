package fi.riista.feature.account.user;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.QPerson;
import fi.riista.util.F;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.querydsl.core.group.GroupBy.groupBy;

@Repository
public class UserRepositoryImpl implements UserRepositoryCustom {
    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Override
    @Transactional
    public void deactivateAccountsForDeceased() {
        final QSystemUser systemUser = QSystemUser.systemUser;
        final QPerson person = QPerson.person;

        jpqlQueryFactory.update(systemUser)
                .where(systemUser.active.isTrue()
                        .and(systemUser.role.eq(SystemUser.Role.ROLE_USER))
                        .and(systemUser.person.isNotNull())
                        .and(systemUser.person.in(JPAExpressions.selectFrom(person)
                                .where(person.deletionCode.eq(Person.DeletionCode.D)))))
                .set(systemUser.active, false)
                .execute();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isModeratorOrAdmin(final long userId) {
        final QSystemUser USER = QSystemUser.systemUser;
        return Optional.ofNullable(jpqlQueryFactory.select(USER.role).from(USER).where(USER.id.eq(userId)).fetchFirst())
                .map(SystemUser.Role::isModeratorOrAdmin)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, String> getModeratorFullNames(final Iterable<? extends LifecycleEntity<Long>> entities) {
        final HashSet<Long> creatorIds = F.mapNonNullsToSet(entities, LifecycleEntity::getCreatedByUserId);

        if (creatorIds.isEmpty()) {
            return Collections.emptyMap();
        }

        final QSystemUser USER = QSystemUser.systemUser;

        return jpqlQueryFactory
                .select(USER.id, USER.firstName, USER.lastName)
                .from(USER)
                .where(USER.role.in(SystemUser.Role.ROLE_ADMIN, SystemUser.Role.ROLE_MODERATOR))
                .fetch()
                .stream()
                .collect(Collectors.<Tuple, Long, String> toMap(
                        tuple -> tuple.get(USER.id),
                        tuple -> String.format("%s %s", tuple.get(USER.firstName), tuple.get(USER.lastName))));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SystemUser> findActiveByPerson(Person person) {
        return Optional.ofNullable(findActiveByPersonIn(Collections.singletonList(person)).get(person.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, SystemUser> findActiveByPersonIn(final List<Person> persons) {
        final QSystemUser USER = QSystemUser.systemUser;
        final QPerson PERSON = QPerson.person;

        if (persons.isEmpty()) {
            return Collections.emptyMap();
        }

        return jpqlQueryFactory
                .from(USER)
                .join(USER.person, PERSON)
                .where(USER.active.isTrue())
                .where(PERSON.in(persons))
                .transform(groupBy(PERSON.id).as(USER));
    }
}
