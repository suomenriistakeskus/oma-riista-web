package fi.riista.integration.metsastajarekisteri.person.finnish;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import fi.riista.config.BatchConfig;
import fi.riista.config.Constants;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.address.Address;
import fi.riista.feature.organization.address.AddressRepository;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.integration.metsastajarekisteri.InnofactorConstants;
import fi.riista.integration.metsastajarekisteri.exception.InvalidRhyException;
import fi.riista.integration.metsastajarekisteri.person.DeletionCode;
import fi.riista.integration.metsastajarekisteri.person.MetsastajaRekisteriPerson;
import fi.riista.util.CountryCode;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.hibernate.validator.internal.constraintvalidators.bv.EmailValidator;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Service
public class MetsastajaRekisteriFinnishPersonImportService {

    private static final Logger LOG = LoggerFactory.getLogger(MetsastajaRekisteriFinnishPersonImportService.class);

    private static final EmailValidator EMAIL_VALIDATOR = new EmailValidator();

    @Resource
    private PersonRepository personRepository;

    @Resource
    private UserRepository userRepository;

    @Resource
    private AddressRepository addressRepository;

    @Resource
    private RiistanhoitoyhdistysRepository rhyRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(noRollbackFor = RuntimeException.class)
    public void updatePersons(final List<? extends MetsastajaRekisteriPerson> batch, final DateTime syncTime) {
        requireNonNull(syncTime);

        final List<Person> missingPersons = new LinkedList<>();
        final List<Person> deadPersons = new LinkedList<>();
        final List<Address> obsoleteAddresses = new LinkedList<>();

        final Map<String, Person> ssnToPerson = loadPersonsBySsn(batch);
        final ImmutableMap<String, Person> hunterNumberToPerson = loadPersonsByHunterNumber(batch);
        final ImmutableMap<String, Riistanhoitoyhdistys> officialCodeToRhy = loadRhy(batch);

        for (final MetsastajaRekisteriPerson mrPerson : batch) {
            final Person personWithHunterNumber = hunterNumberToPerson.get(mrPerson.getHunterNumber());
            final Person personWithSsn = ssnToPerson.get(mrPerson.getSsn());
            final Person existingPerson = Optional.ofNullable(personWithHunterNumber).orElse(personWithSsn);

            final Riistanhoitoyhdistys rhy = officialCodeToRhy.get(mrPerson.getMembershipRhyOfficialCode());

            // Person already in local database?
            if (existingPerson != null) {
                if (personWithHunterNumber != null) {

                    if (personWithSsn != null &&
                            !personWithHunterNumber.getId().equals(personWithSsn.getId())) {
                        // Person found by ssn and hunter number, but are two different persons -> error
                        LOG.warn("Hunter number {} is not unique, another person (personId={}) with different ssn " +
                                "exists", mrPerson.getHunterNumber(), personWithSsn.getId());
                        continue;
                    } else if (personWithSsn == null) {
                        if (personWithHunterNumber.isForeignPerson()) {
                            LOG.info("Updating foreign person to Finnish person: {}", personWithHunterNumber.getId());
                        } else {
                            LOG.warn("Hunter number {} is not unique, a person (personId={}) with different ssn " +
                                    "exists", mrPerson.getHunterNumber(), personWithHunterNumber.getId());
                            continue;
                        }
                    }

                }

                if (handleDeceased(existingPerson, mrPerson, syncTime)) {
                    deadPersons.add(existingPerson);
                }

                update(existingPerson, mrPerson, rhy, syncTime, obsoleteAddresses);

            } else if (mrPerson.getDeletionCode() == null) {
                final Person person = new Person();

                update(person, mrPerson, rhy, syncTime, obsoleteAddresses);

                LOG.debug("Creating new person {}", mrPerson);

                missingPersons.add(person);
            }
        }

        if (!missingPersons.isEmpty()) {
            personRepository.saveAll(missingPersons);
        }

        if (!obsoleteAddresses.isEmpty()) {
            addressRepository.deleteAll(obsoleteAddresses);
        }

        if (!deadPersons.isEmpty()) {
            userRepository.deactivateAccount(deadPersons);
        }

        // MUST flush session here so that Spring Batch can update metadata tables successfully
        entityManager.flush();
        entityManager.clear();
    }

    static boolean handleDeceased(final Person person, final MetsastajaRekisteriPerson item, final DateTime syncTime) {

        // Process dead people only when status changes for performance
        if (DeletionCode.DECEASED == item.getDeletionCode() && Person.DeletionCode.D != person.getDeletionCode()) {

            LOG.info("Deactivating accounts for deceased personId={}", person.getId());

            final LocalDate deletionTime = item.getDeletionDate() == null ? DateUtil.today() : item.getDeletionDate();

            person.setDeletionCode(Person.DeletionCode.D);
            person.getLifecycleFields().setDeletionTime(deletionTime.toDateTimeAtStartOfDay(Constants.DEFAULT_TIMEZONE));
            person.setMrSyncTime(syncTime);

            return true;
        }

        return false;
    }

    static void update(final Person person, final MetsastajaRekisteriPerson item, final Riistanhoitoyhdistys rhy,
                       final DateTime syncTime, final List<Address> obsoleteAddresses) {

        if (item.getDeletionCode() != null) {
            if (person.hasHunterNumber()) {
                // Record timestamp for first change in deletion status
                person.setMrSyncTime(syncTime);
            }

            person.clearHunterInformation();
            person.setHomeMunicipality(null);
            person.setHomeMunicipalityCode(null);

            updateAddress(person, item, obsoleteAddresses);

        } else {
            person.setMrSyncTime(syncTime);

            if (person.isNew() || person.isForeignPerson()) {
                person.setSsn(item.getSsn());
            }

            updateName(person, item);
            updateHunterFields(person, item, rhy);
            updateAddress(person, item, obsoleteAddresses);
            updateEmail(person, item);
            updateOtherFields(person, item);
        }
    }

    static void updateName(final Person person, final MetsastajaRekisteriPerson item) {
        person.setLastName(item.getLastName());
        person.setFirstName(item.getFirstName());

        // Never replace byName unless it is invalid or empty (new person)
        if (person.isNew()) {
            person.setByName(item.getFirstName());
        }
    }

    static void updateEmail(final Person person, final MetsastajaRekisteriPerson item) {
        // Invalid e-mail in local database?
        if (StringUtils.hasText(person.getEmail()) && !EMAIL_VALIDATOR.isValid(person.getEmail(), null)) {
            LOG.warn("Replacing invalid email for personId={} hunterNumber={} with value={} replacement={}",
                    person.getId(), item.getHunterNumber(), person.getEmail(), item.getEmail());

            person.setEmail(item.getEmail());
        }
    }

    static void updateHunterFields(final Person person, final MetsastajaRekisteriPerson item,
                                   final Riistanhoitoyhdistys rhy) {
        if (item.getHunterNumber() == null && person.hasHunterNumber()) {
            LOG.warn("Replacing existing hunterNumber={} with empty value for personId={}",
                    person.getHunterNumber(), person.getId());
        }

        if (person.isHuntingCardValidNow()) {
            if (item.getHuntingCardStart() == null && person.getHuntingCardStart() != null) {
                LOG.warn("Replacing existing huntingCardStart={} with empty value for personId={}",
                        person.getHuntingCardStart(), person.getId());
            }

            if (item.getHunterExamDate() == null && person.getHunterExamDate() != null) {
                LOG.warn("Replacing existing hunterExamDate={} with empty value for personId={}",
                        person.getHunterExamDate(), person.getId());
            }
        }

        if (person.isHuntingBanActiveNow()) {
            if (item.getHuntingBanStart() == null && person.getHuntingBanStart() != null) {
                LOG.warn("Replacing existing huntingBanStart={} with empty value for personId={}",
                        person.getHuntingBanStart(), person.getId());
            }

            if (item.getHuntingBanEnd() == null && person.getHuntingBanEnd() != null) {
                LOG.warn("Replacing existing huntingBanEnd={} with empty value for personId={}",
                        person.getHuntingBanEnd(), person.getId());
            }
        }

        if (item.getHuntingPaymentOneDay() == null && person.getHuntingPaymentOneDay() != null) {
            LOG.warn("Replacing existing huntingPaymentOneDay={} with empty value for personId={}",
                    person.getHuntingPaymentOneDay(), person.getId());
        }

        person.setHunterNumber(item.getHunterNumber());
        person.setHuntingCardStart(item.getHuntingCardStart());
        person.setHuntingCardEnd(item.getHuntingCardEnd());
        person.setHunterExamDate(item.getHunterExamDate());
        person.setHunterExamExpirationDate(item.getHunterExamExpirationDate());
        person.setHuntingBanStart(item.getHuntingBanStart());
        person.setHuntingBanEnd(item.getHuntingBanEnd());

        person.setHuntingPaymentOneDay(item.getHuntingPaymentOneDay());
        person.setHuntingPaymentOneYear(item.getHuntingPaymentOneYear());

        person.setHuntingPaymentTwoDay(item.getHuntingPaymentTwoDay());
        person.setHuntingPaymentTwoYear(item.getHuntingPaymentTwoYear());

        person.setInvoiceReferenceCurrent(item.getInvoiceReferenceCurrent());
        person.setInvoiceReferencePrevious(item.getInvoiceReferencePrevious());
        person.setInvoiceReferenceCurrentYear(item.getInvoiceReferenceCurrentYear());
        person.setInvoiceReferencePreviousYear(item.getInvoiceReferencePreviousYear());

        // Person should not appear in search if exam has expired
        if (!person.isHunterExamValidNow()) {
            person.setHunterNumber(null);
            person.setRhyMembership(null);
            person.setHuntingCardStart(null);
            person.setHuntingCardEnd(null);

            return;
        }

        // Clear hunting card range if not valid right now
        if (!person.isHuntingCardValidNow() && !person.isHuntingCardValidInFuture()) {
            person.setRhyMembership(null);
            person.setHuntingCardStart(null);
            person.setHuntingCardEnd(null);

            return;
        }

        if (rhy != null) {
            person.setRhyMembership(rhy);

        } else if (item.getMembershipRhyOfficialCode() == null ||
                InnofactorConstants.RHY_AHVENANMAA.equals(item.getMembershipRhyOfficialCode()) ||
                InnofactorConstants.RHY_NOT_MEMBER_CODE.equals(item.getMembershipRhyOfficialCode()) ||
                InnofactorConstants.RHY_FOREIGN_MEMBER_CODE.equals(item.getMembershipRhyOfficialCode())) {
            person.setRhyMembership(null);
        } else {
            throw new InvalidRhyException("No such RHY with officialCode=" + item.getMembershipRhyOfficialCode());
        }
    }

    static void updateAddress(final Person person,
                              final MetsastajaRekisteriPerson item,
                              final List<Address> obsoleteAddresses) {

        if (item.getDeletionCode() != null) {
            removeMrAddress(person, obsoleteAddresses);

        } else if (StringUtils.hasText(item.getStreetAddress()) &&
                StringUtils.hasText(item.getPostalCode()) &&
                StringUtils.hasText(item.getPostOffice())) {
            Address address = person.getMrAddress();

            if (address == null) {
                address = new Address();
            }

            person.setMrAddress(address);

            address.setStreetAddress(item.getStreetAddress());
            address.setPostalCode(item.getPostalCode());
            address.setCity(item.getPostOffice());

            if ("FI".equalsIgnoreCase(item.getCountryCode())) {
                address.setCountryCode("FI");
                address.setCountry("Suomi");

            } else if (StringUtils.hasText(item.getCountryCode())) {
                address.setCountryCode(item.getCountryCode());

                final Optional<String> countryName = CountryCode.getCountryName(item.getCountryCode());

                if (countryName.isPresent()) {
                    address.setCountry(countryName.get());
                } else if (StringUtils.hasText(item.getCountryName())) {
                    address.setCountry(item.getCountryName());
                } else {
                    address.setCountry(null);
                }

            } else if (StringUtils.hasText(item.getCountryName())) {
                address.setCountryCode("XX");
                address.setCountry(item.getCountryName());

            } else {
                address.setCountryCode("FI");
                address.setCountry("Suomi");
            }

        } else {
            // Address not available, remove previous invalid address
            removeMrAddress(person, obsoleteAddresses);
        }
    }

    private static void removeMrAddress(final Person person, final List<Address> obsoleteAddresses) {
        if (person.getMrAddress() != null) {
            LOG.warn("Removing MR address for personId={} hunterNumber={} with value={}",
                    person.getId(), person.getHunterNumber(), person.getMrAddress());
            obsoleteAddresses.add(person.getMrAddress());
            person.setMrAddress(null);
        }
    }

    static void updateOtherFields(final Person person, final MetsastajaRekisteriPerson item) {
        person.setDenyMagazine(item.isDenyMagazine());
        person.setHomeMunicipalityCode(item.getHomeMunicipalityCode());

        if (item.getLanguageCode() != null) {
            person.setLanguageCode(item.getLanguageCode());
        }

        if (item.getMagazineLanguageCode() != null) {
            person.setMagazineLanguageCode(item.getMagazineLanguageCode());
        }
    }

    // Load persons in batch using list of SSN. Fetch associations also.
    private ImmutableMap<String, Person> loadPersonsBySsn(final List<? extends MetsastajaRekisteriPerson> list) {
        final List<String> ssnArray = F.mapNonNullsToList(list, input -> requireNonNull(input.getSsn()));

        if (ssnArray.isEmpty()) {
            return ImmutableMap.of();
        }

        // Use padded list to optimize SQL. Use fixed size collection for postgresql query planner cache optimization
        final List<String> cycledList = ImmutableList.copyOf(Iterables.limit(
                Iterables.cycle(ssnArray), BatchConfig.BATCH_SIZE));

        return Maps.uniqueIndex(personRepository.findBySsnAndFetchMrAddress(cycledList), Person::getSsn);
    }

    // Load persons in batch using list of SSN. Fetch associations also. Address is stored only for
    // Finnish persons so address fetching is done with ssn based query.
    private ImmutableMap<String, Person> loadPersonsByHunterNumber(final List<? extends MetsastajaRekisteriPerson> list) {
        final List<String> hunterNumberArray = F.mapNonNullsToList(list, input -> input.getHunterNumber());

        if (hunterNumberArray.isEmpty()) {
            return ImmutableMap.of();
        }

        // Use padded list to optimize SQL. Use fixed size collection for postgresql query planner cache optimization
        final List<String> cycledList = ImmutableList.copyOf(Iterables.limit(
                Iterables.cycle(hunterNumberArray), BatchConfig.BATCH_SIZE));

        return Maps.uniqueIndex(personRepository.findAllByHunterNumberIn(cycledList), Person::getHunterNumber);
    }

    private ImmutableMap<String, Riistanhoitoyhdistys> loadRhy(final List<? extends MetsastajaRekisteriPerson> list) {
        final List<String> rhyOfficialCodeList =
                F.mapNonNullsToList(list, MetsastajaRekisteriPerson::getMembershipRhyOfficialCode);

        if (rhyOfficialCodeList.isEmpty()) {
            return ImmutableMap.of();
        }

        // Use padded list to optimize SQL
        final List<String> paddedList = ImmutableList.copyOf(Iterables.limit(
                Iterables.cycle(rhyOfficialCodeList), BatchConfig.BATCH_SIZE));

        return Maps.uniqueIndex(rhyRepository.findByOfficialCode(paddedList), Organisation::getOfficialCode);
    }
}
