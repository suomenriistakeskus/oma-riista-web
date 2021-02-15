package fi.riista.feature.organization.rhy;

import com.google.common.collect.ImmutableMap;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.person.Person;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.F;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static fi.riista.feature.organization.occupation.OccupationType.TOIMINNANOHJAAJA;
import static fi.riista.test.Asserts.assertEmpty;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.MatcherAssert.assertThat;

public class RiistanhoitoyhdistysEmailServiceTest extends EmbeddedDatabaseTest {

    @Resource
    private RiistanhoitoyhdistysEmailService service;

    @Test
    public void testResolveEmails_withIdCollection_whenRhyHasEmail() {
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

        final Riistanhoitoyhdistys rhy1 = model().newRiistanhoitoyhdistys(rka);
        rhy1.setEmail("rhy1@invalid");

        model().newOccupation(rhy1, model().newPerson(), TOIMINNANOHJAAJA);

        // The RHYs created below should not appear in the result.
        model().newRiistanhoitoyhdistys(rka);
        model().newOccupation(model().newRiistanhoitoyhdistys(), model().newPerson(), TOIMINNANOHJAAJA);

        persistInNewTransaction();

        runInTransaction(() -> {
            final Map<Long, Set<String>> emailMapping = service.resolveEmails(F.getUniqueIds(rhy1));
            assertEquals(1, emailMapping.size());

            final Set<String> emails = emailMapping.get(rhy1.getId());
            assertNotNull(emails);

            assertThat(emails, contains(equalTo(rhy1.getEmail())));
        });
    }

    @Test
    public void testResolveEmails_withIdCollection_whenRhyDoesNotHaveEmail() {
        final RiistakeskuksenAlue rka = model().newRiistakeskuksenAlue();

        final Riistanhoitoyhdistys rhy1 = model().newRiistanhoitoyhdistys(rka);
        rhy1.setEmail(null);

        final Person person1 = model().newPerson();
        person1.setEmail("person1@invalid");

        model().newOccupation(rhy1, person1, TOIMINNANOHJAAJA);

        // The RHYs created below should not appear in the result.
        model().newRiistanhoitoyhdistys(rka);
        model().newOccupation(model().newRiistanhoitoyhdistys(), model().newPerson(), TOIMINNANOHJAAJA);

        persistInNewTransaction();

        runInTransaction(() -> {
            final Map<Long, Set<String>> emailMapping = service.resolveEmails(F.getUniqueIds(rhy1));
            assertEquals(1, emailMapping.size());

            final Set<String> emails = emailMapping.get(rhy1.getId());
            assertNotNull(emails);

            assertThat(emails, contains(equalTo(person1.getEmail())));
        });
    }

    @Test
    public void testResolveEmails_withIdCollection_whenEmailsOfRhyAndCoordinatorAreNull() {
        withRhyAndCoordinator((rhy, coordinator) -> {
            rhy.setEmail(null);
            coordinator.setEmail(null);

            persistInNewTransaction();

            runInTransaction(() -> {
                final Map<Long, Set<String>> emailMapping = service.resolveEmails(F.getUniqueIds(rhy));
                assertEquals(1, emailMapping.size());

                final Set<String> emails = emailMapping.get(rhy.getId());
                assertNotNull(emails);

                assertEquals(0, emails.size());
            });
        });
    }

    @Test
    public void testResolveEmails_withIdCollection_whenRhyDoesNotHaveEmailNorCoordinator() {
        withRhy(rhy -> {
            rhy.setEmail(null);

            persistInNewTransaction();

            runInTransaction(() -> {
                final Map<Long, Set<String>> emailMapping = service.resolveEmails(F.getUniqueIds(rhy));
                assertEquals(1, emailMapping.size());

                final Set<String> emails = emailMapping.get(rhy.getId());
                assertNotNull(emails);

                assertEquals(0, emails.size());
            });
        });
    }

    @Test
    public void testResolveEmails_withIdCollection_withMultipleRhys() {
        withRhyAndCoordinator((rhy1, person1) -> withRhyAndCoordinator((rhy2, person2) -> withRhyAndCoordinator((rhy3, person3) -> {

            rhy1.setEmail("rhy1@invalid");
            person1.setEmail(null);

            rhy2.setEmail(null);
            person2.setEmail("person2@invalid");

            rhy3.setEmail(null);
            person3.setEmail(null);

            final Person person4 = model().newPerson();
            person4.setEmail("person4@invalid");
            model().newOccupation(rhy2, person4, TOIMINNANOHJAAJA);

            // The RHYs created below should not appear in the result.
            model().newRiistanhoitoyhdistys();
            model().newOccupation(model().newRiistanhoitoyhdistys(), person1, TOIMINNANOHJAAJA);

            persistInNewTransaction();

            runInTransaction(() -> {
                final Map<Long, Set<String>> emailMapping = service.resolveEmails(F.getUniqueIds(rhy1, rhy2, rhy3));

                final Map<Long, Set<String>> expected = ImmutableMap.of(
                        rhy1.getId(), singleton("rhy1@invalid"),
                        rhy2.getId(), newHashSet("person2@invalid", "person4@invalid"),
                        rhy3.getId(), emptySet());

                assertEquals(expected, emailMapping);
            });
        })));
    }

    @Transactional
    @Test
    public void testResolveEmails_withSingleRhy_bothRhyAndCoordinatorHavingEmail() {
        withRhyAndCoordinator((rhy, coordinator) -> {

            rhy.setEmail("info@rhy.com");
            coordinator.setEmail("coordinator@rhy.com");

            persistInCurrentlyOpenTransaction();

            assertEquals(singleton(rhy.getEmail()), service.resolveEmails(rhy));
        });
    }

    @Transactional
    @Test
    public void testResolveEmails_withSingleRhy_whenOnlyCoordinatorsHaveEmail() {
        withRhyAndCoordinator((rhy, coordinator) -> withPerson(coordinator2 -> withPerson(coordinator3 -> {

            rhy.setEmail(null);
            coordinator.setEmail("coordinator1@rhy.com");

            coordinator2.setEmail("COORDINATOR2@RHY.COM");
            model().newOccupation(rhy, coordinator2, TOIMINNANOHJAAJA);

            // Email of coordinator3 should not be picked
            model().newOccupation(model().newRiistanhoitoyhdistys(), coordinator3, TOIMINNANOHJAAJA);

            persistInCurrentlyOpenTransaction();

            final Set<String> expected = newHashSet(coordinator.getEmail(), coordinator2.getEmail().toLowerCase());

            assertEquals(expected, service.resolveEmails(rhy));
        })));
    }

    @Transactional
    @Test
    public void testResolveEmails_withSingleRhy_whenNeitherRhyNorCoordinatorsHaveEmail() {
        withRhyAndCoordinator((rhy, coordinator) -> withPerson(coordinator2 -> withPerson(coordinator3 -> {

            rhy.setEmail(null);
            coordinator.setEmail(null);

            coordinator2.setEmail(null);
            model().newOccupation(rhy, coordinator2, TOIMINNANOHJAAJA);

            // Email of coordinator3 should not be picked
            model().newOccupation(model().newRiistanhoitoyhdistys(), coordinator3, TOIMINNANOHJAAJA);

            persistInCurrentlyOpenTransaction();

            assertEmpty(service.resolveEmails(rhy));
        })));
    }
}
