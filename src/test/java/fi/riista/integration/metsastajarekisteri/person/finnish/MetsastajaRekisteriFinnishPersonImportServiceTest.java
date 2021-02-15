package fi.riista.integration.metsastajarekisteri.person.finnish;

import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.integration.metsastajarekisteri.person.DeletionCode;
import fi.riista.integration.metsastajarekisteri.person.MetsastajaRekisteriPerson;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MetsastajaRekisteriFinnishPersonImportServiceTest extends EmbeddedDatabaseTest {

    @Resource
    private MetsastajaRekisteriFinnishPersonImportService importService;

    @Resource
    private PersonRepository personRepository;

    private Person existingPerson;

    @Before
    public void initContent() {
        initializeExistingPerson();

        persistInNewTransaction();
    }

    private void initializeExistingPerson() {
        final Person person = createPerson();

        this.existingPerson = person;
    }

    private Person createPerson() {
        final Person person = model().newPerson();
        person.setMrAddress(model().newAddress());
        person.setOtherAddress(model().newAddress());

        person.setHuntingCardStart(d(2014, 8, 1));
        person.setHuntingCardEnd(d(2099, 7, 31));
        person.setHunterExamDate(d(1996, 4, 12));
        person.setHunterExamExpirationDate(d(2099, 6, 23));
        person.setHuntingPaymentOneDay(d(2015, 1, 3));
        person.setHuntingPaymentOneYear(2014);
        person.setHuntingPaymentTwoDay(d(2013, 8, 3));
        person.setHuntingPaymentTwoYear(2013);
        return person;
    }

    @Test
    public void testNewPerson() {
        final MetsastajaRekisteriPerson mr = new MetsastajaRekisteriPerson();
        mr.setSsn(ssn());

        generateValidName(mr);
        generateValidAddress(mr);

        importService.updatePersons(singletonList(mr), DateUtil.now());

        runInTransaction(() -> {
            final Person person = personRepository.findBySsn(mr.getSsn()).orElse(null);

            assertNotNull("Address missing", person.getMrAddress());
            assertTrue("Name should be set", hasSameName(person, mr));
            assertTrue("Name should be set", hasSameAddress(person.getMrAddress(), mr));
        });
    }

    @Test
    public void testNewPerson_withDuplicateHunterNumberFinnishNewPerson() {
        // New Finnish person with same hunter number as existingPerson
        final MetsastajaRekisteriPerson mr = new MetsastajaRekisteriPerson();
        mr.setSsn(ssn());
        mr.setHunterNumber(existingPerson.getHunterNumber());
        generateValidName(mr);
        generateValidAddress(mr);

        importService.updatePersons(singletonList(mr), DateUtil.now());

        runInTransaction(() -> {
            final List<Person> all = personRepository.findAll();
            assertThat(all, hasSize(1));
            final Person person = all.get(0);
            assertEquals(existingPerson, person);
            assertNull(person.getMrSyncTime());
        });
    }


    @Test
    public void testNewPerson_withDuplicateHunterNumberFinnishUpdatedPerson() {
        final Person existingPerson2 = createPerson();
        persistInNewTransaction();

        // New Finnish person with same hunter number as existingPerson
        final MetsastajaRekisteriPerson mr = new MetsastajaRekisteriPerson();
        mr.setSsn(existingPerson2.getSsn());
        mr.setHunterNumber(existingPerson.getHunterNumber());
        generateValidName(mr);
        generateValidAddress(mr);

        importService.updatePersons(singletonList(mr), DateUtil.now());

        runInTransaction(() -> {
            final Optional<Person> person1Optional = personRepository.findBySsn(existingPerson.getSsn());
            assertTrue(person1Optional.isPresent());
            final Person person1 = person1Optional.get();
            assertEquals(existingPerson, person1);
            assertNull(person1.getMrSyncTime());
            assertEquals(existingPerson.getHunterNumber(), person1.getHunterNumber());

            final Optional<Person> person2Optional = personRepository.findBySsn(existingPerson2.getSsn());
            assertTrue(person2Optional.isPresent());
            final Person person2 = person2Optional.get();
            assertEquals(existingPerson2, person2);
            assertNull(person2.getMrSyncTime());
            assertEquals(existingPerson2.getHunterNumber(), person2.getHunterNumber());
        });
    }

    @Test
    public void testForeignPersonTransitioningToFinnishPerson() {
        runInTransaction(personRepository::deleteAll);

        // Existing foreign person (no ssn)
        initializeExistingPerson();
        existingPerson.setSsn(null);
        existingPerson.setDateOfBirth(new LocalDate(2000, 1, 1));
        persistInNewTransaction();

        final MetsastajaRekisteriPerson mr = new MetsastajaRekisteriPerson();
        mr.setSsn(ssn());
        mr.setHunterNumber(existingPerson.getHunterNumber());
        mr.setFirstName(existingPerson.getFirstName());
        mr.setLastName(existingPerson.getLastName());
        generateValidAddress(mr);

        importService.updatePersons(singletonList(mr), DateUtil.now());

        runInTransaction(() -> {
            final List<Person> all = personRepository.findAll();
            assertThat(all, hasSize(1));

            final Person person = all.get(0);

            assertFalse("Should be Finnish person", person.isForeignPerson());
            assertNotNull("Address missing", person.getMrAddress());
            assertTrue("Name should be set", hasSameName(person, mr));
            assertTrue("Name should be set", hasSameAddress(person.getMrAddress(), mr));
            assertFalse(person.isForeignPerson());
            assertEquals(mr.getSsn(), person.getSsn());
        });
    }

    @Test
    public void testNewPerson_HunterFields_Updated() {
        final MetsastajaRekisteriPerson mr = new MetsastajaRekisteriPerson();
        mr.setSsn(ssn());
        mr.setHunterNumber(hunterNumber());

        generateValidName(mr);
        generateValidHuntingInformation(mr);

        importService.updatePersons(singletonList(mr), DateUtil.now());

        runInTransaction(() -> {
            final Person person = personRepository.findBySsn(mr.getSsn()).orElse(null);

            assertSameHunterInformation(person, mr);
        });
    }

    @Test
    public void testExistingPerson_NameGiven_NameIsChanged() {
        final MetsastajaRekisteriPerson mr = createWithSsnAndHunterNumber(existingPerson);

        generateValidName(mr);

        importService.updatePersons(singletonList(mr), DateUtil.now());

        runInTransaction(() -> {
            final Person person = personRepository.findById(existingPerson.getId()).orElse(null);
            assertTrue("Name should change", hasSameName(person, mr));
        });
    }

    @Test
    public void testExistingPerson_AddressEmpty_AddressNotUpdated() {
        final MetsastajaRekisteriPerson mr = createWithSsnAndHunterNumber(existingPerson);

        mr.setStreetAddress(null);
        mr.setPostalCode("");
        mr.setPostOffice("");

        importService.updatePersons(singletonList(mr), DateUtil.now());

        runInTransaction(() -> {
            final Person person = personRepository.findById(existingPerson.getId()).orElse(null);

            assertNull("Empty or partial value should remove address", person.getMrAddress());
            assertNotNull("Other addresses should remain", person.getOtherAddress());
            assertFalse("Address should change", hasSameAddress(person.getOtherAddress(),
                    existingPerson.getOtherAddress()));
        });
    }

    @Test
    public void testExistingPerson_AddressGiven_AddressUpdated() {
        final MetsastajaRekisteriPerson mr = createWithSsnAndHunterNumber(existingPerson);

        generateValidAddress(mr);

        importService.updatePersons(singletonList(mr), DateUtil.now());

        runInTransaction(() -> {
            final Person person = personRepository.findById(existingPerson.getId()).orElse(null);

            assertNotNull(person.getMrAddress());
            assertNotNull("Other address should not be changed", person.getOtherAddress());

            assertTrue("Address should be updated", hasSameAddress(person.getMrAddress(), mr));
        });
    }

    @Test
    public void testHandleDeceased() {
        final MetsastajaRekisteriPerson mr = createWithSsnAndHunterNumber(existingPerson);

        mr.setDeletionCode(DeletionCode.DECEASED);

        importService.updatePersons(singletonList(mr), DateUtil.now());

        runInTransaction(() -> {
            final Person person = personRepository.findById(existingPerson.getId()).orElse(null);

            assertTrue("Name should not change", hasSameName(person, existingPerson));
            assertNull("Should remove address MR address", person.getMrAddress());
            assertEquals(Person.DeletionCode.D, person.getDeletionCode());
            assertEquals(DateUtil.today(), new LocalDate(person.getDeletionTime()));
        });
    }

    @Test
    public void testHandle_personWitHunterExamExpired() {
        existingPerson.setHunterNumber(null);
        existingPerson.clearHunterInformation();
        persistInNewTransaction();

        final MetsastajaRekisteriPerson mr = createWithSsnAndHunterNumber(existingPerson);
        generateValidAddress(mr);

        importService.updatePersons(singletonList(mr), DateUtil.now());

        runInTransaction(() -> {
            final List<Person> all = personRepository.findAll();
            assertThat(all, hasSize(1));
            final Person person = all.get(0);

            assertEquals(existingPerson.getId(), person.getId());
            assertNotNull("Address missing", person.getMrAddress());
            assertTrue("Name should be set", hasSameName(person, mr));
            assertTrue("Name should be set", hasSameAddress(person.getMrAddress(), mr));
            assertNotNull(person.getMrSyncTime());
        });
    }

    @Test
    public void testHandleDeleted() {
        final MetsastajaRekisteriPerson mr = createWithSsnAndHunterNumber(existingPerson);

        mr.setDeletionCode(DeletionCode.OTHER);

        generateValidAddress(mr);
        generateValidName(mr);

        importService.updatePersons(singletonList(mr), DateUtil.now());

        runInTransaction(() -> {
            final Person person = personRepository.findById(existingPerson.getId()).orElse(null);

            assertTrue("Name should not change", hasSameName(person, existingPerson));
            assertNull("Should remove address", person.getMrAddress());
            assertNull("Should not mark local user deleted", person.getDeletionCode());
            assertNull("Should not mark local user deleted", person.getDeletionTime());

            assertFalse(person.hasHunterNumber());
            assertNull(person.getHuntingCardStart());
            assertNull(person.getHuntingCardStart());
            assertNull(person.getHunterExamDate());
            assertNull(person.getHunterExamExpirationDate());
            assertNull(person.getHuntingPaymentOneDay());
            assertNull(person.getHuntingPaymentOneYear());
            assertNull(person.getHuntingPaymentTwoDay());
            assertNull(person.getHuntingPaymentTwoYear());
            assertNull(person.getRhyMembership());
        });
    }

    private static MetsastajaRekisteriPerson createWithSsnAndHunterNumber(final Person p) {
        final MetsastajaRekisteriPerson m = new MetsastajaRekisteriPerson();
        m.setSsn(p.getSsn());
        m.setHunterNumber(p.getHunterNumber());
        m.setFirstName("a");
        m.setLastName("b");
        return m;
    }

    private void generateValidAddress(final MetsastajaRekisteriPerson mr) {
        mr.setStreetAddress("Street " + postalCode());
        mr.setPostalCode(hunterNumber());
        mr.setPostOffice("Tampere");
        mr.setCountryCode("FI");
        mr.setCountryName("Suomi");
    }

    private void generateValidName(final MetsastajaRekisteriPerson mr) {
        mr.setFirstName(personName());
        mr.setLastName(personName());
    }

    private static void generateValidHuntingInformation(final MetsastajaRekisteriPerson mr) {
        mr.setHuntingCardStart(d());
        mr.setHuntingCardEnd(mr.getHuntingCardStart().plusYears(1));
        mr.setHunterExamDate(d());
        mr.setHunterExamExpirationDate(mr.getHunterExamDate().plusYears(5));
        mr.setHuntingPaymentOneDay(d());
        mr.setHuntingPaymentOneYear(mr.getHuntingPaymentOneDay().getYear());
        mr.setHuntingPaymentTwoDay(d());
        mr.setHuntingPaymentTwoYear(mr.getHuntingPaymentTwoDay().getYear());
        mr.setInvoiceReferenceCurrent("1234567");
        mr.setInvoiceReferenceCurrentYear(2016);
        mr.setInvoiceReferencePrevious("2345678");
        mr.setInvoiceReferencePreviousYear(2015);
    }

    private static boolean hasSameName(final Person p1, final Person p2) {
        Objects.requireNonNull(p1);
        Objects.requireNonNull(p2);

        return Objects.equals(p1.getFirstName(), p2.getFirstName()) &&
                Objects.equals(p1.getLastName(), p2.getLastName()) &&
                Objects.equals(p1.getByName(), p2.getByName());
    }

    private static boolean hasSameAddress(final Address address, final MetsastajaRekisteriPerson mrPerson) {
        Objects.requireNonNull(address);
        Objects.requireNonNull(mrPerson);

        return Objects.equals(mrPerson.getStreetAddress(), address.getStreetAddress())
                && Objects.equals(mrPerson.getPostalCode(), address.getPostalCode())
                && Objects.equals(mrPerson.getPostOffice(), address.getCity())
                && Objects.equals(mrPerson.getCountryCode(), address.getCountryCode())
                && Objects.equals(mrPerson.getCountryName(), address.getCountry());
    }

    private static boolean hasSameAddress(final Address a1, final Address a2) {
        Objects.requireNonNull(a1);
        Objects.requireNonNull(a2);

        return Objects.equals(a2.getStreetAddress(), a1.getStreetAddress())
                && Objects.equals(a2.getPostalCode(), a1.getPostalCode())
                && Objects.equals(a2.getCountryCode(), a1.getCity())
                && Objects.equals(a2.getCountryCode(), a1.getCountryCode())
                && Objects.equals(a2.getCountry(), a1.getCountry());
    }

    private static boolean hasSameName(final Person person, final MetsastajaRekisteriPerson mrPerson) {
        return Objects.equals(mrPerson.getFirstName(), person.getFirstName())
                && Objects.equals(mrPerson.getLastName(), person.getLastName());
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

    private static LocalDate d() {
        return DateUtil.today().minusDays(30);
    }

    private static LocalDate d(final int year, final int month, final int day) {
        return new LocalDate(year, month, day);
    }
}
