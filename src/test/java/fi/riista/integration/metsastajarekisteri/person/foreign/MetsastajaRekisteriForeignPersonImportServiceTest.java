package fi.riista.integration.metsastajarekisteri.person.foreign;

import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.shootingtest.ShootingTestEvent;
import fi.riista.feature.shootingtest.ShootingTestFixtureMixin;
import fi.riista.integration.metsastajarekisteri.InnofactorConstants;
import fi.riista.integration.metsastajarekisteri.person.DeletionCode;
import fi.riista.integration.metsastajarekisteri.person.MetsastajaRekisteriPerson;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

import static fi.riista.util.DateUtil.today;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.joda.time.DateTime.now;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MetsastajaRekisteriForeignPersonImportServiceTest extends EmbeddedDatabaseTest
        implements ShootingTestFixtureMixin {

    @Resource
    private MetsastajaRekisteriForeignPersonImportService importService;

    @Resource
    private PersonRepository personRepository;

    private Person existingPerson;

    @Before
    public void initContent() {
        this.existingPerson = model().newForeignPerson();
    }

    @Test
    public void testNewPerson() {
        final MetsastajaRekisteriPerson mr = create(hunterNumber());

        importService.updateForeignPersons(singletonList(mr), now());

        runInTransaction(() -> {
            final Person person = personRepository.findByHunterNumber(mr.getHunterNumber()).orElse(null);

            assertNotNull("Foreign person should be persisted", person);
            assertTrue("Name should be set", hasSameName(mr, person));
            assertEquals("Date of birth should be set", mr.getDateOfBirth(), person.getDateOfBirth());
            assertFalse(person.isDeleted());
        });
    }


    @Test
    public void testNewPerson_withDuplicateHunterNumber() {
        // New foreign person with same hunter number as existingPerson
        final MetsastajaRekisteriPerson mr = new MetsastajaRekisteriPerson();
        mr.setSsn(null);
        mr.setHunterNumber(existingPerson.getHunterNumber());
        mr.setFirstName("Fanny");
        mr.setLastName("Foreigner");
        mr.setDateOfBirth(new LocalDate(2000, 1, 1));

        persistInNewTransaction();
        importService.updateForeignPersons(singletonList(mr), DateTime.now());

        runInTransaction(() -> {
            final List<Person> all = personRepository.findAll();
            assertThat(all, hasSize(1));
            assertEquals(existingPerson, all.get(0));
        });
    }

    @Test
    public void testNewPerson_notSavedIfDeleted_usingDeceasedCode() {
        testNewPersonShouldNotBeSavedIfDeleted(DeletionCode.DECEASED);
    }

    @Test
    public void testNewPerson_notSavedIfDeleted_usingOtherCode() {
        testNewPersonShouldNotBeSavedIfDeleted(DeletionCode.OTHER);
    }

    private void testNewPersonShouldNotBeSavedIfDeleted(final DeletionCode deletionCode) {
        final MetsastajaRekisteriPerson mr = create(hunterNumber());
        mr.setDeletionCode(deletionCode);

        importService.updateForeignPersons(singletonList(mr), now());

        final Person person = personRepository.findFinnishPersonByHunterNumber(mr.getHunterNumber()).orElse(null);
        assertNull("Should not be found", person);
    }

    @Test
    public void testExistingPerson_nameAndBirthDateUpdated() {
        final MetsastajaRekisteriPerson mr = create(existingPerson.getHunterNumber());

        persistInNewTransaction();
        importService.updateForeignPersons(singletonList(mr), now());

        runInTransaction(() -> {
            final Person person = personRepository.findById(existingPerson.getId()).orElse(null);

            assertTrue("Name should change", hasSameName(mr, person));
            assertEquals("Date of birth should change", mr.getDateOfBirth(), person.getDateOfBirth());
            assertFalse(person.isDeleted());
        });
    }

    @Test
    public void testExistingPerson_whenDeletedButShootingTestParticipationsExist_usingDeceasedCode() {
        final MetsastajaRekisteriPerson mr = create(existingPerson);
        mr.setDeletionCode(DeletionCode.DECEASED);

        testDeletionWhenShootingTestParticipationsExist(mr);
    }

    @Test
    public void testExistingPerson_whenDeletedButShootingTestParticipationsExist_usingOtherCode() {
        final MetsastajaRekisteriPerson mr = create(existingPerson);
        mr.setDeletionCode(DeletionCode.OTHER);

        testDeletionWhenShootingTestParticipationsExist(mr);
    }

    private void testDeletionWhenShootingTestParticipationsExist(final MetsastajaRekisteriPerson mr) {
        final ShootingTestEvent event = openEvent(model().newRiistanhoitoyhdistys(), today());
        createParticipantWithOneAttempt(event, existingPerson);
        event.close();

        testDeletion_softDelete(mr);
    }

    @Test
    public void testExistingPerson_whenDeletedAndNoShootingTestParticipations_usingDeceasedCode() {
        final MetsastajaRekisteriPerson mr = create(existingPerson);
        mr.setDeletionCode(DeletionCode.DECEASED);

        testDeletion_softDelete(mr);
    }

    @Test
    public void testExistingPerson_whenDeletedAndNoShootingTestParticipations_usingOtherCode() {
        final MetsastajaRekisteriPerson mr = create(existingPerson);
        mr.setDeletionCode(DeletionCode.OTHER);

        testDeletion_softDelete(mr);
    }

    private void testDeletion_softDelete(final MetsastajaRekisteriPerson mr) {
        persistInNewTransaction();

        importService.updateForeignPersons(singletonList(mr), now());

        runInTransaction(() -> {
            final Person person = personRepository.getOne(existingPerson.getId());

            assertNotNull("Foreign person should still exist", person);
            assertTrue("Name should not change", hasSameName(mr, person));
            assertEquals("Date of birth should not change", mr.getDateOfBirth(), person.getDateOfBirth());

            assertTrue("Should be soft-deleted", person.isDeleted());
            assertEquals(today(), new LocalDate(person.getDeletionTime()));
        });
    }

    @Test
    public void testExistingPerson_whenDeletionReverted() {
        persistInNewTransaction();
        runInTransaction(() -> {
            final Person person = personRepository.findById(existingPerson.getId()).orElse(null);
            person.setDeletionCode(Person.DeletionCode.D);
        });

        final MetsastajaRekisteriPerson mr = create(existingPerson);

        importService.updateForeignPersons(singletonList(mr), now());

        runInTransaction(() -> {
            final Person person = personRepository.findById(existingPerson.getId()).orElse(null);

            assertFalse("Deletion status should be removed", person.isDeleted());
        });
    }

    @Test
    public void testPredefinedForeignerWithSsnIsUpdated() {
        final Person personWithSsn = model().newPerson("First", "LastName", "010101-0101", "88888888");
        persistInNewTransaction();

        final MetsastajaRekisteriPerson mr = create(personWithSsn.getHunterNumber());

        importService.assignHuntersWithSsn(personWithSsn.getHunterNumber());
        importService.updateForeignPersons(singletonList(mr), now());

        runInTransaction(() -> {
            final Person person = personRepository.findById(personWithSsn.getId()).orElse(null);

            assertFalse("Should still be Finnish person", person.isForeignPerson());
            assertEquals(mr.getFirstName(), person.getFirstName());
            assertEquals(mr.getLastName(), person.getLastName());
            assertEquals(mr.getDateOfBirth(), person.getDateOfBirth());
        });
    }

    @Test
    public void testPredefinedForeignerWithSsnIsNotDeleted() {
        final Person personWithSsn = model().newPerson("First", "LastName", "010101-0101", "88888888");
        persistInNewTransaction();

        final MetsastajaRekisteriPerson mr = create(personWithSsn.getHunterNumber());
        mr.setDeletionCode(DeletionCode.DECEASED);

        importService.assignHuntersWithSsn(personWithSsn.getHunterNumber());
        importService.updateForeignPersons(singletonList(mr), now());

        runInTransaction(() -> {
            final Person person = personRepository.findById(personWithSsn.getId()).orElse(null);

            assertNotNull(person);
            assertFalse("Should still be Finnish person", person.isForeignPerson());
            assertEquals(Person.DeletionCode.D, person.getDeletionCode());
        });
    }

    @Test
    public void testNewPerson_HunterFields_Updated() {
        final MetsastajaRekisteriPerson mr = create(hunterNumber());

        importService.updateForeignPersons(singletonList(mr), DateUtil.now());

        runInTransaction(() -> {
            final Person person = personRepository.findByHunterNumber(mr.getHunterNumber()).orElse(null);

            assertSameHunterInformation(person, mr);
            assertNull(person.getRhyMembership());
        });
    }

    @Test
    public void testNewPerson_HunterFields_HuntingCardExpired() {
        existingPerson.setHuntingCardStart(today().minusMonths(1));
        existingPerson.setHuntingCardEnd(today().plusMonths(1));
        persistInNewTransaction();

        final MetsastajaRekisteriPerson mr = create(existingPerson.getHunterNumber());
        mr.setHuntingCardEnd(today().minusDays(1));

        importService.updateForeignPersons(singletonList(mr), DateUtil.now());

        runInTransaction(() -> {
            final Person person = personRepository.findByHunterNumber(mr.getHunterNumber()).orElse(null);

            assertNull(person.getHuntingCardStart());
            assertNull(person.getHuntingCardEnd());
        });
    }

    @Test
    public void testNewPerson_HunterFields_RhyMembership() {
        final Riistanhoitoyhdistys rhy = model().newRiistanhoitoyhdistys();
        persistInNewTransaction();

        final MetsastajaRekisteriPerson mr = create(existingPerson.getHunterNumber());
        mr.setMembershipRhyOfficialCode(rhy.getOfficialCode());

        importService.updateForeignPersons(singletonList(mr), DateUtil.now());

        runInTransaction(() -> {
            final Person person = personRepository.findByHunterNumber(mr.getHunterNumber()).orElse(null);

            assertEquals(rhy, person.getRhyMembership());
        });
    }

    private static MetsastajaRekisteriPerson create(final Person person) {
        final MetsastajaRekisteriPerson mr = new MetsastajaRekisteriPerson();
        mr.setHunterNumber(person.getHunterNumber());
        mr.setFirstName(person.getFirstName());
        mr.setLastName(person.getLastName());
        mr.setDateOfBirth(person.getDateOfBirth());
        return mr;
    }

    private void populateValidName(final MetsastajaRekisteriPerson mr) {
        mr.setFirstName(personName());
        mr.setLastName(personName());
    }

    private void populateValidDateOfBirth(final MetsastajaRekisteriPerson mr) {
        final int ageModulo = InnofactorConstants.MAX_PERSON_AGE - InnofactorConstants.MIN_FOREIGN_PERSON_AGE;
        final int age = InnofactorConstants.MIN_FOREIGN_PERSON_AGE + nextPositiveInt() % ageModulo;

        mr.setDateOfBirth(today().minusYears(age));
    }

    private MetsastajaRekisteriPerson create(final String hunterNumber) {
        final MetsastajaRekisteriPerson mr = new MetsastajaRekisteriPerson();
        mr.setHunterNumber(hunterNumber);
        populateValidName(mr);
        populateValidDateOfBirth(mr);
        mr.setHuntingCardStart(d(2014, 8, 1));
        mr.setHuntingCardEnd(d(2099, 7, 31));
        mr.setHunterExamDate(d(1996, 4, 12));
        mr.setHunterExamExpirationDate(d(2099, 6, 23));
        mr.setHuntingPaymentOneDay(d(2015, 1, 3));
        mr.setHuntingPaymentOneYear(2014);
        mr.setHuntingPaymentTwoDay(d(2013, 8, 3));
        mr.setHuntingPaymentTwoYear(2013);
        return mr;
    }

    private static boolean hasSameName(final MetsastajaRekisteriPerson p1, final Person p2) {
        requireNonNull(p1);
        requireNonNull(p2);

        return Objects.equals(p1.getFirstName(), p2.getFirstName()) &&
                Objects.equals(p1.getLastName(), p2.getLastName());
    }

    private static LocalDate d(final int year, final int month, final int day) {
        return new LocalDate(year, month, day);
    }

    private static void assertSameHunterInformation(final Person person, final MetsastajaRekisteriPerson mr) {
        assertEquals(mr.getHunterNumber(), person.getHunterNumber());
        assertEquals(mr.getHuntingCardStart(), person.getHuntingCardStart());
        assertEquals(mr.getHuntingCardEnd(), person.getHuntingCardEnd());
        assertEquals(mr.getHunterExamDate(), person.getHunterExamDate());
        assertEquals(mr.getHunterExamExpirationDate(), person.getHunterExamExpirationDate());
        assertEquals(mr.getHuntingPaymentOneDay(), person.getHuntingPaymentOneDay());
        assertEquals(mr.getHuntingPaymentOneYear(), person.getHuntingPaymentOneYear());
        assertEquals(mr.getHuntingPaymentTwoDay(), person.getHuntingPaymentTwoDay());
        assertEquals(mr.getHuntingPaymentTwoYear(), person.getHuntingPaymentTwoYear());
        assertEquals(mr.getInvoiceReferenceCurrent(), person.getInvoiceReferenceCurrent());
        assertEquals(mr.getInvoiceReferencePrevious(), person.getInvoiceReferencePrevious());
        assertEquals(mr.getInvoiceReferenceCurrentYear(), person.getInvoiceReferenceCurrentYear());
        assertEquals(mr.getInvoiceReferencePreviousYear(), person.getInvoiceReferencePreviousYear());
    }

}
