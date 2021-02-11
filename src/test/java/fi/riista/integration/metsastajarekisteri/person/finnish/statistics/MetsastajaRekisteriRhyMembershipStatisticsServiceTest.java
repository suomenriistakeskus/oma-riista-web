package fi.riista.integration.metsastajarekisteri.person.finnish.statistics;

import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.integration.metsastajarekisteri.person.MetsastajaRekisteriPerson;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.MockTimeProvider;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static fi.riista.test.TestUtils.ld;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MetsastajaRekisteriRhyMembershipStatisticsServiceTest extends EmbeddedDatabaseTest {
    @Resource
    private RhyMembershipImportService statisticsService;

    @Resource
    private PersonRepository personRepository;

    @After
    public void resetMocks() {
        MockTimeProvider.resetMock();
    }

    private Person createPerson() {
        final Person person = model().newPerson();
        person.setRhyMembership(model().newRiistanhoitoyhdistys());
        person.setMrAddress(model().newAddress());

        person.setHuntingCardStart(ld(2019, 8, 1));
        person.setHuntingCardEnd(ld(2099, 7, 31));
        person.setHunterExamDate(ld(1996, 4, 12));
        person.setHunterExamExpirationDate(ld(2099, 6, 23));
        person.setHuntingPaymentOneDay(ld(2019, 8, 3));
        person.setHuntingPaymentOneYear(2019);
        person.setHuntingPaymentTwoDay(ld(2018, 8, 3));
        person.setHuntingPaymentTwoYear(2018);
        return person;
    }

    @Test
    public void testRhyMembershipStatisticsIsUpdated() {
        mockDate(ld(2019, 12, 27));
        final Person person = createPerson();
        persistInNewTransaction();

        final MetsastajaRekisteriPerson mrPerson = createWithSsnAndHunterNumber(person);
        statisticsService.updatePersons(Collections.singletonList(mrPerson), DateUtil.now());

        assertForUpdatedPerson(updatedPerson -> {
            assertEquals(
                    mrPerson.getMembershipRhyOfficialCode(),
                    updatedPerson.getRhyMembershipForStatistics().getOfficialCode());
            assertEquals(mrPerson.getHuntingPaymentOneYear(), updatedPerson.getPaymentYearValidForStatistics());
        });
    }

    @Test
    public void testRhyMembershipStatisticsIsUpdated_paymentMissing() {
        mockDate(ld(2019, 12, 27));
        final Person person = createPerson();
        persistInNewTransaction();

        final MetsastajaRekisteriPerson mrPerson = createWithSsnAndHunterNumber(person);
        mrPerson.setHuntingPaymentOneDay(null);
        mrPerson.setHuntingPaymentOneYear(null);
        mrPerson.setHuntingPaymentTwoDay(null);
        mrPerson.setHuntingPaymentTwoYear(null);

        statisticsService.updatePersons(Collections.singletonList(mrPerson), DateUtil.now());

        assertForUpdatedPerson(updatedPerson -> {
            assertEquals(
                    mrPerson.getMembershipRhyOfficialCode(),
                    updatedPerson.getRhyMembershipForStatistics().getOfficialCode());
            assertNull(updatedPerson.getPaymentYearValidForStatistics());
            assertNull(updatedPerson.getRhyMembershipForStatisticsForYear(2019));
        });
    }

    @Test
    public void testRhyMembershipStatisticsIsUpdated_paymentAfterYearChange() {
        mockDate(ld(2020, 1, 4));
        final Person person = createPerson();
        person.setRhyMembershipForStatistics(person.getRhyMembership());
        persistInNewTransaction();

        final MetsastajaRekisteriPerson mrPerson = createWithSsnAndHunterNumber(person);

        statisticsService.updatePersons(Collections.singletonList(mrPerson), DateUtil.now());

        assertForUpdatedPerson(updatedPerson -> {
            assertEquals(mrPerson.getHuntingPaymentOneYear(), updatedPerson.getPaymentYearValidForStatistics());
            assertEquals(person.getRhyMembershipForStatistics(),
                    updatedPerson.getRhyMembershipForStatisticsForYear(2019));
        });
    }

    @Test
    public void testRhyMembershipStatisticsNotUpdatedAfterYearChange() {
        mockDate(ld(2020, 1, 1));
        final Person person = createPerson();
        person.setRhyMembershipForStatistics(person.getRhyMembership());
        final Riistanhoitoyhdistys oldRhy = person.getRhyMembership();
        final Riistanhoitoyhdistys newRhy = model().newRiistanhoitoyhdistys();
        persistInNewTransaction();

        final MetsastajaRekisteriPerson mrPerson = createWithSsnAndHunterNumber(person);
        mrPerson.setMembershipRhyOfficialCode(newRhy.getOfficialCode());

        statisticsService.updatePersons(Collections.singletonList(mrPerson), DateUtil.now());

        assertForUpdatedPerson(updatedPerson -> {
            assertEquals(oldRhy, updatedPerson.getRhyMembershipForStatistics());
        });
    }

    @Test
    public void testRhyMembershipStatisticsIsUpdated_paymentTooLongAfterYearChange() {
        mockDate(ld(2020, 1, 16));
        final Person person = createPerson();
        person.setRhyMembershipForStatistics(person.getRhyMembership());
        persistInNewTransaction();

        final MetsastajaRekisteriPerson mrPerson = createWithSsnAndHunterNumber(person);

        statisticsService.updatePersons(Collections.singletonList(mrPerson), DateUtil.now());

        assertForUpdatedPerson(updatedPerson -> {
            assertEquals(
                    mrPerson.getMembershipRhyOfficialCode(),
                    updatedPerson.getRhyMembershipForStatistics().getOfficialCode());
            assertNull(updatedPerson.getPaymentYearValidForStatistics());
            assertNull(updatedPerson.getRhyMembershipForStatisticsForYear(2019));
        });
    }

    private void assertForUpdatedPerson(final Consumer<Person> assertions) {
        runInTransaction(() -> {
            final List<Person> all = personRepository.findAll();
            assertThat(all, hasSize(1));
            final Person updatedPerson = all.get(0);
            assertions.accept(updatedPerson);
        });
    }

    private static MetsastajaRekisteriPerson createWithSsnAndHunterNumber(final Person p) {
        final MetsastajaRekisteriPerson m = new MetsastajaRekisteriPerson();
        m.setSsn(p.getSsn());
        m.setHunterNumber(p.getHunterNumber());
        m.setFirstName("a");
        m.setLastName("b");
        m.setMembershipRhyOfficialCode(p.getRhyMembership().getOfficialCode());
        m.setHuntingCardStart(p.getHuntingCardStart());
        m.setHuntingCardEnd(p.getHuntingCardEnd());
        m.setHuntingPaymentOneDay(p.getHuntingPaymentOneDay());
        m.setHuntingPaymentOneYear(p.getHuntingPaymentOneYear());
        m.setHuntingPaymentTwoDay(p.getHuntingPaymentTwoDay());
        m.setHuntingPaymentTwoYear(p.getHuntingPaymentTwoYear());
        return m;
    }

    private static void mockDate(final LocalDate localDate) {
        MockTimeProvider.mockTime(DateUtil.toDateNullSafe(localDate).getTime());
    }

}
