package fi.riista.integration.metsastajarekisteri;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.util.DateUtil;
import fi.riista.util.ValueGenerator;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Objects;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class MetsastajaRekisteriImportServiceTest extends EmbeddedDatabaseTest {

    @Resource
    private MetsastajaRekisteriImportService importService;

    @Resource
    private PersonRepository personRepository;

    private Person p1;

    @Before
    public void initContent() {
        this.p1 = model().newPerson();
        this.p1.setMrAddress(model().newAddress());
        this.p1.setOtherAddress(model().newAddress());

        this.p1.setHuntingCardStart(d(2014, 8, 1));
        this.p1.setHuntingCardEnd(d(2099, 7, 31));
        this.p1.setHunterExamDate(d(1996, 4, 12));
        this.p1.setHunterExamExpirationDate(d(2099, 6, 23));
        this.p1.setHuntingPaymentOneDay(d(2015, 1, 3));
        this.p1.setHuntingPaymentOneYear(2014);
        this.p1.setHuntingPaymentTwoDay(d(2013, 8, 3));
        this.p1.setHuntingPaymentTwoYear(2013);

        persistInNewTransaction();
    }

    @Test
    public void testNewPerson() {
        final MetsastajaRekisteriPerson mr = new MetsastajaRekisteriPerson();
        mr.setSsn(ssn());

        generateValidName(mr);
        generateValidAddress(mr);

        importService.updatePersons(singletonList(mr), DateTime.now());

        runInTransaction(() -> {
            final Person person = personRepository.findBySsn(mr.getSsn()).orElse(null);

            assertNotNull("Address missing", person.getMrAddress());
            assertTrue("Name should be set", hasSameName(person, mr));
            assertTrue("Name should be set", hasSameAddress(person.getMrAddress(), mr));
        });
    }

    @Test
    public void testNewPerson_HunterFields_Updated() {
        final MetsastajaRekisteriPerson mr = new MetsastajaRekisteriPerson();
        mr.setSsn(ssn());
        mr.setHunterNumber(hunterNumber());

        generateValidName(mr);
        generateValidHuntingInformation(mr);

        importService.updatePersons(singletonList(mr), DateTime.now());

        runInTransaction(() -> {
            final Person person = personRepository.findBySsn(mr.getSsn()).orElse(null);

            assertSameHunterInformation(person, mr);
        });
    }

    @Test
    public void testExistingPerson_NameGiven_NameIsChanged() {
        final MetsastajaRekisteriPerson mr = createWithSsnAndHunterNumber(p1);

        generateValidName(mr);

        importService.updatePersons(singletonList(mr), DateTime.now());

        runInTransaction(() -> {
            final Person person = personRepository.findOne(p1.getId());
            assertTrue("Name should change", hasSameName(person, mr));
        });
    }

    @Test
    public void testExistingPerson_AddressEmpty_AddressNotUpdated() {
        final MetsastajaRekisteriPerson mr = createWithSsnAndHunterNumber(p1);

        mr.setStreetAddress(null);
        mr.setPostalCode("");
        mr.setPostOffice("");

        importService.updatePersons(singletonList(mr), DateTime.now());

        runInTransaction(() -> {
            final Person person = personRepository.findOne(p1.getId());

            assertNull("Empty or partial value should remove address", person.getMrAddress());
            assertNotNull("Other addresses should remain", person.getOtherAddress());
            assertFalse("Address should change", hasSameAddress(person.getOtherAddress(), p1.getOtherAddress()));
        });
    }

    @Test
    public void testExistingPerson_AddressGiven_AddressUpdated() {
        final MetsastajaRekisteriPerson mr = createWithSsnAndHunterNumber(p1);

        generateValidAddress(mr);

        importService.updatePersons(singletonList(mr), DateTime.now());

        runInTransaction(() -> {
            final Person person = personRepository.findOne(p1.getId());

            assertNotNull(person.getMrAddress());
            assertNotNull("Other address should not be changed", person.getOtherAddress());

            assertTrue("Address should be updated", hasSameAddress(person.getMrAddress(), mr));
        });
    }

    @Test
    public void testHandleDeceased() {
        final MetsastajaRekisteriPerson mr = createWithSsnAndHunterNumber(p1);

        mr.setDeletionCode(MetsastajaRekisteriPerson.DeletionCode.DECEASED);

        importService.updatePersons(singletonList(mr), DateTime.now());

        runInTransaction(() -> {
            final Person person = personRepository.findOne(p1.getId());

            assertTrue("Name should not change", hasSameName(person, p1));
            assertNull("Should remove address MR address", person.getMrAddress());
            assertEquals(Person.DeletionCode.D, person.getDeletionCode());
            assertEquals(DateUtil.today(), new LocalDate(person.getDeletionTime()));
        });
    }

    @Test
    public void testHandleDeleted() {
        final MetsastajaRekisteriPerson mr = createWithSsnAndHunterNumber(p1);

        mr.setDeletionCode(MetsastajaRekisteriPerson.DeletionCode.OTHER);

        generateValidAddress(mr);
        generateValidName(mr);

        importService.updatePersons(singletonList(mr), DateTime.now());

        runInTransaction(() -> {
            final Person person = personRepository.findOne(p1.getId());

            assertTrue("Name should not change", hasSameName(person, p1));
            assertNull("Should remove address", person.getMrAddress());
            assertNull("Should not mark local user deleted", person.getDeletionCode());
            assertNull("Should not mark local user deleted", person.getDeletionTime());

            assertNull(person.getHunterNumber());
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

    private static MetsastajaRekisteriPerson createWithSsnAndHunterNumber(Person p) {
        MetsastajaRekisteriPerson m = new MetsastajaRekisteriPerson();
        m.setSsn(p.getSsn());
        m.setHunterNumber(p.getHunterNumber());
        m.setFirstName("a");
        m.setLastName("b");
        return m;
    }

    private void generateValidAddress(MetsastajaRekisteriPerson mr) {
        mr.setStreetAddress("Street " + postalCode());
        mr.setPostalCode(hunterNumber());
        mr.setPostOffice("Tampere");
        mr.setCountryCode("FI");
        mr.setCountryName("Suomi");
    }

    private static void generateValidName(MetsastajaRekisteriPerson mr) {
        mr.setFirstName(ValueGenerator.personName());
        mr.setLastName(ValueGenerator.personName());
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

    private static boolean hasSameName(Person p1, Person p2) {
        Objects.requireNonNull(p1);
        Objects.requireNonNull(p2);

        return Objects.equals(p1.getFirstName(), p2.getFirstName()) &&
                Objects.equals(p1.getLastName(), p2.getLastName()) &&
                Objects.equals(p1.getByName(), p2.getByName());
    }

    private static boolean hasSameAddress(Address address, MetsastajaRekisteriPerson mrPerson) {
        Objects.requireNonNull(address);
        Objects.requireNonNull(mrPerson);

        return Objects.equals(mrPerson.getStreetAddress(), address.getStreetAddress())
                && Objects.equals(mrPerson.getPostalCode(), address.getPostalCode())
                && Objects.equals(mrPerson.getPostOffice(), address.getCity())
                && Objects.equals(mrPerson.getCountryCode(), address.getCountryCode())
                && Objects.equals(mrPerson.getCountryName(), address.getCountry());
    }

    private static boolean hasSameAddress(Address a1, Address a2) {
        Objects.requireNonNull(a1);
        Objects.requireNonNull(a2);

        return Objects.equals(a2.getStreetAddress(), a1.getStreetAddress())
                && Objects.equals(a2.getPostalCode(), a1.getPostalCode())
                && Objects.equals(a2.getCountryCode(), a1.getCity())
                && Objects.equals(a2.getCountryCode(), a1.getCountryCode())
                && Objects.equals(a2.getCountry(), a1.getCountry());
    }

    private static boolean hasSameName(Person person, MetsastajaRekisteriPerson mrPerson) {
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

    private static LocalDate d(int year, int month, int day) {
        return new LocalDate(year, month, day);
    }
}
