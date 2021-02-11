package fi.riista.integration.metsastajarekisteri.person.finnish.statistics;

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
import fi.riista.integration.metsastajarekisteri.person.MetsastajaRekisteriPerson;
import fi.riista.util.F;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Comparator.naturalOrder;
import static java.util.Objects.requireNonNull;

@Service
public class RhyMembershipImportService {
    private static final Logger LOG = LoggerFactory.getLogger(RhyMembershipImportService.class);


    @Resource
    private PersonRepository personRepository;

    @Resource
    private RiistanhoitoyhdistysRepository rhyRepository;

    @PersistenceContext
    private EntityManager entityManager;


    @Transactional(noRollbackFor = RuntimeException.class)
    public void updatePersons(final List<? extends MetsastajaRekisteriPerson> batch, final DateTime syncTime) {
        requireNonNull(syncTime);

        final Map<String, Person> ssnToPerson = loadPersonsBySsn(batch);
        final ImmutableMap<String, Person> hunterNumberToPerson = loadPersonsByHunterNumber(batch);
        final ImmutableMap<String, Riistanhoitoyhdistys> officialCodeToRhy = loadRhy(batch);

        for (final MetsastajaRekisteriPerson mrPerson : batch) {
            final Person personWithHunterNumber = hunterNumberToPerson.get(mrPerson.getHunterNumber());
            final Person personWithSsn = ssnToPerson.get(mrPerson.getSsn());
            final Person existingPerson = Optional.ofNullable(personWithHunterNumber).orElse(personWithSsn);

            final Riistanhoitoyhdistys rhy = officialCodeToRhy.get(mrPerson.getMembershipRhyOfficialCode());


            if (existingPerson != null) {
                if (duplicatePersons(personWithHunterNumber, personWithSsn)) {
                    LOG.warn("Different persons found with hunternumber (personId={}) and ssn (personId={})",
                            personWithHunterNumber.getId(), personWithSsn.getId());
                    continue;
                }
                update(existingPerson, mrPerson, rhy, syncTime);
            }
        }

        // MUST flush session here so that Spring Batch can update metadata tables successfully
        entityManager.flush();
        entityManager.clear();
    }

    private static boolean duplicatePersons(final Person personWithHunterNumber, final Person personWithSsn) {
        return personWithHunterNumber != null && personWithSsn != null && !personWithHunterNumber.equals(personWithSsn);
    }

    private static void update(final Person existingPerson, final MetsastajaRekisteriPerson mrPerson,
                               final Riistanhoitoyhdistys rhy, final DateTime syncTime) {

        final RhyMembershipImportMode.Phase phase =
                RhyMembershipImportMode.getPhase(syncTime);
        switch (phase) {
            case STORE_MEMBERS:
                existingPerson.setRhyMembershipForStatistics(rhy);
                storePaymentYear(existingPerson, mrPerson);
                break;
            case STORE_PAYMENT:
                storePaymentYear(existingPerson, mrPerson);
                break;
            case NOT_APPLICABLE:
                break;
            default:
                throw new IllegalStateException("Unknown phase: " + phase);
        }
    }

    private static void storePaymentYear(final Person existingPerson, final MetsastajaRekisteriPerson mrPerson) {
        final Integer paymentYear = Stream.of(
                mrPerson.getHuntingPaymentOneYear(),
                mrPerson.getHuntingPaymentTwoYear())
                .filter(Objects::nonNull)
                .max(naturalOrder())
                .orElse(null);

        // Currently not used, but kept if manual calculations are needed after Jan 16th.
        existingPerson.setPaymentYearValidForStatistics(paymentYear);
    }

    // Load persons in batch using list of SSN. Fetch associations also.
    private ImmutableMap<String, Person> loadPersonsBySsn(final List<? extends MetsastajaRekisteriPerson> list) {
        if (list.isEmpty()) {
            return ImmutableMap.of();
        }

        final List<String> ssnArray = F.mapNonNullsToList(list, input -> requireNonNull(input.getSsn()));

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
