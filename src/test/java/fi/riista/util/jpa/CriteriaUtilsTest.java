package fi.riista.util.jpa;

import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.Sets;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.Person_;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.util.F;
import fi.riista.util.Functions;

import org.junit.Test;

import javax.annotation.Resource;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class CriteriaUtilsTest extends EmbeddedDatabaseTest {

    @Resource
    private UserRepository userRepo;

    @Resource
    private PersonRepository personRepo;

    @Test
    @HibernateStatisticsAssertions(queryCount = 0)
    public void testSingleQueryFunction_withSingleEntryIterable() {
        final SystemUser user = createUserWithPerson();

        persistInNewTransaction();

        runInTransaction(() -> {
            final SystemUser user2 = userRepo.findOne(user.getId());

            // We want to enforce statistics assertions for the target
            // method to be called next.
            clearHibernateStatistics();

            final Function<SystemUser, Person> personFunc = CriteriaUtils.singleQueryFunction(
                    Collections.singletonList(user2), SystemUser::getPerson, personRepo, false);

            assertEquals(user.getPerson().getId(), personFunc.apply(user2).getId());
        });
    }

    @Test
    @HibernateStatisticsAssertions(queryCount = 1)
    public void testSingleQueryFunction_withMultipleEntries() {
        final SystemUser user = createUserWithPerson("user1");
        final SystemUser user2 = createUserWithPerson("user2");

        persistInNewTransaction();

        runInTransaction(() -> {
            final List<SystemUser> users = userRepo.findAll();

            // Clear statistics to isolate assertions for the next method to be called.
            clearHibernateStatistics();

            final Function<SystemUser, Person> personFunc =
                    CriteriaUtils.singleQueryFunction(users, SystemUser::getPerson, personRepo, false);

            final Set<Long> personIds = users.stream().map(Functions.idOf(personFunc)).collect(toSet());

            assertEquals(F.getUniqueIds(user.getPerson(), user2.getPerson()), personIds);
        });
    }

    @Test
    @HibernateStatisticsAssertions(queryCount = 1)
    public void testSingleQueryFunction_withMultipleEntriesAndRelationConstraint() {
        final SystemUser user = createUserWithPerson("user1");
        final SystemUser user2 = createUserWithPerson("user2");
        user2.getPerson().setHunterNumber(null);

        persistInNewTransaction();

        runInTransaction(() -> {
            final List<SystemUser> users = userRepo.findAll();

            // Clear statistics to isolate assertions for the next method to be called.
            clearHibernateStatistics();

            final Function<SystemUser, Person> personFunc = CriteriaUtils.singleQueryFunction(
                    users, SystemUser::getPerson, personRepo, JpaSpecs.isNotNull(Person_.hunterNumber), false);

            final Set<Long> personIds = users.stream().map(Functions.idOf(personFunc)).collect(toSet());

            assertEquals(Sets.newHashSet(user.getPerson().getId(), null), personIds);
        });
    }

}
