package fi.riista.integration.metsastajarekisteri.person.foreign;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import fi.riista.config.BatchConfig;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.rhy.RiistanhoitoyhdistysRepository;
import fi.riista.feature.shootingtest.ShootingTestParticipantRepository;
import fi.riista.integration.metsastajarekisteri.InnofactorConstants;
import fi.riista.integration.metsastajarekisteri.exception.InvalidRhyException;
import fi.riista.integration.metsastajarekisteri.person.MetsastajaRekisteriPerson;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static fi.riista.feature.organization.person.Person.DeletionCode.D;
import static fi.riista.util.Collect.indexingBy;
import static java.util.Objects.requireNonNull;

@Service
public class MetsastajaRekisteriForeignPersonImportService {

    private static final Logger LOG = LoggerFactory.getLogger(MetsastajaRekisteriForeignPersonImportService.class);

    @Resource
    private PersonRepository personRepository;

    @Resource
    private RiistanhoitoyhdistysRepository rhyRepository;

    @Resource
    private ShootingTestParticipantRepository shootingTestParticipantRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private List<String> foreignersWithSsn;

    @Autowired
    public void assignHuntersWithSsn(@Value("${metsastajarekisteri.import.foreign.huntersWithSsn}") String property) {
        if (StringUtils.hasText(property)) {
            final ArrayList<String> list = F.mapNonNullsToList(
                    Arrays.asList(property.split(",")),
                    String::trim);
            this.foreignersWithSsn = list;
        } else {
            this.foreignersWithSsn = ImmutableList.of();
        }
        LOG.debug("Assigned {} predefined foreign hunters with ssn", foreignersWithSsn.size());
    }

    @Transactional(noRollbackFor = RuntimeException.class)
    public void updateForeignPersons(final List<? extends MetsastajaRekisteriPerson> batch, final DateTime syncTime) {
        requireNonNull(syncTime, "syncTime is null");

        final List<Person> missingPersons = new ArrayList<>();
        final List<Person> deletedPersons = new ArrayList<>();

        final Map<String, Person> hunterNumberToPerson = loadPersons(batch);
        final ImmutableMap<String, Riistanhoitoyhdistys> officialCodeToRhy = loadRhy(batch);

        for (final MetsastajaRekisteriPerson mrPerson : batch) {
            final Person existingPerson = hunterNumberToPerson.get(mrPerson.getHunterNumber());
            final Riistanhoitoyhdistys rhy = officialCodeToRhy.get(mrPerson.getMembershipRhyOfficialCode());
            if (existingPerson != null) {

                // If person with ssn exists, do not overwrite Finnish person's data
                if (existingPerson.getSsn() != null) {
                    if (foreignersWithSsn.contains(mrPerson.getHunterNumber())) {
                        LOG.info("Updating existing person with ssn and hunter number: {}", mrPerson.getHunterNumber());
                    } else {
                        LOG.warn("Hunter number {} is not unique", mrPerson.getHunterNumber());
                        continue;
                    }
                }

                if (markDeletedIfDeletionCodePresent(existingPerson, mrPerson, syncTime)) {
                    deletedPersons.add(existingPerson);
                } else {
                    update(existingPerson, mrPerson, syncTime, rhy);
                }

            } else if (mrPerson.getDeletionCode() == null) {

                final Person person = new Person();
                update(person, mrPerson, syncTime, rhy);
                LOG.debug("Creating new foreign person {}", mrPerson);
                missingPersons.add(person);
            }
        }

        if (!missingPersons.isEmpty()) {
            personRepository.saveAll(missingPersons);
        }

        if (!deletedPersons.isEmpty()) {
            deletedPersons.forEach(person -> {
                person.clearHunterInformation();
                person.setHomeMunicipality(null);
                person.setHomeMunicipalityCode(null);
            });
        }

        // MUST flush session here so that Spring Batch can update metadata tables successfully.
        entityManager.flush();
        entityManager.clear();
    }

    private static boolean markDeletedIfDeletionCodePresent(final Person person,
                                                            final MetsastajaRekisteriPerson input,
                                                            final DateTime syncTime) {

        final boolean isDeletedInMR = input.getDeletionCode() != null;

        // For performance reasons update deletion status only on the first time it is encountered.
        if (isDeletedInMR && !person.isDeleted()) {

            LOG.info("Marking foreign person deleted (id={})", person.getId());

            final DateTime deletionTime = Optional
                    .ofNullable(input.getDeletionDate())
                    .map(DateUtil::toDateTimeNullSafe)
                    .orElseGet(DateUtil::now);

            // TODO: Foreign persons are not necessarily deceased.
            person.setDeletionCode(D);
            person.getLifecycleFields().setDeletionTime(deletionTime);
            person.setMrSyncTime(syncTime);
        }

        return isDeletedInMR;
    }

    private static void update(final Person person,
                               final MetsastajaRekisteriPerson mrPerson,
                               final DateTime syncTime,
                               final Riistanhoitoyhdistys rhy) {

        if (person.isNew()) {
            person.setHunterNumber(mrPerson.getHunterNumber());
            person.setByName(mrPerson.getFirstName());
        } else if (person.isDeleted()) {
            person.setDeletionCode(null);
        }

        person.setFirstName(mrPerson.getFirstName());
        person.setLastName(mrPerson.getLastName());
        person.setDateOfBirth(mrPerson.getDateOfBirth());
        updateHunterFields(person, mrPerson, rhy);
        person.setMrSyncTime(syncTime);
    }

    static void updateHunterFields(final Person person, final MetsastajaRekisteriPerson item,
                                   final Riistanhoitoyhdistys rhy) {

        if (person.isHuntingCardValidNow()) {
            if (item.getHuntingCardStart() == null && person.getHuntingCardStart() != null) {
                LOG.warn("Replacing existing huntingCardStart={} with empty value for personId={}",
                        person.getHuntingCardStart(), person.getId());
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

        // Clear hunting card range if not valid right now
        if (!person.isHuntingCardValidNow() && !person.isHuntingCardValidInFuture()) {
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

    // Load persons in batch using a list of hunter numbers.
    private Map<String, Person> loadPersons(final List<? extends MetsastajaRekisteriPerson> list) {
        final List<String> hunterNumbers = F.mapNonNullsToList(list, input -> requireNonNull(input.getHunterNumber()));

        if (hunterNumbers.isEmpty()) {
            return ImmutableMap.of();
        }

        // Search for all persons to detect possible duplicates
        return personRepository.findAllByHunterNumberIn(hunterNumbers)
                .stream()
                .collect(indexingBy(Person::getHunterNumber));
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
