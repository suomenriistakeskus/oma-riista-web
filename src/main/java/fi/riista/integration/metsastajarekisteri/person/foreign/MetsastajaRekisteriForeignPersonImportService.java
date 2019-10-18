package fi.riista.integration.metsastajarekisteri.person.foreign;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.feature.shootingtest.ShootingTestParticipantRepository;
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
import java.util.stream.Collectors;

import static fi.riista.feature.organization.person.Person.DeletionCode.D;
import static fi.riista.util.Collect.indexingBy;
import static java.util.Objects.requireNonNull;

@Service
public class MetsastajaRekisteriForeignPersonImportService {

    private static final Logger LOG = LoggerFactory.getLogger(MetsastajaRekisteriForeignPersonImportService.class);

    @Resource
    private PersonRepository personRepository;

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

        for (final MetsastajaRekisteriPerson mrPerson : batch) {
            final Person existingPerson = hunterNumberToPerson.get(mrPerson.getHunterNumber());

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
                    update(existingPerson, mrPerson, syncTime);
                }

            } else if (mrPerson.getDeletionCode() == null) {

                final Person person = new Person();
                update(person, mrPerson, syncTime);
                LOG.debug("Creating new foreign person {}", mrPerson);
                missingPersons.add(person);
            }
        }

        if (!missingPersons.isEmpty()) {
            personRepository.save(missingPersons);
        }

        if (!deletedPersons.isEmpty()) {
            removeDeletedForeignPersonsNotHavingShootingTestParticipations(deletedPersons);
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
            person.getLifecycleFields().setDeletionTime(deletionTime.toDate());
            person.setMrSyncTime(syncTime);
        }

        return isDeletedInMR;
    }

    private static void update(final Person person,
                               final MetsastajaRekisteriPerson mrPerson,
                               final DateTime syncTime) {

        if (person.isNew()) {
            person.setHunterNumber(mrPerson.getHunterNumber());
            person.setByName(mrPerson.getFirstName());
        } else if (person.isDeleted()) {
            person.setDeletionCode(null);
        }

        person.setFirstName(mrPerson.getFirstName());
        person.setLastName(mrPerson.getLastName());
        person.setDateOfBirth(mrPerson.getDateOfBirth());

        person.setMrSyncTime(syncTime);
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

    private void removeDeletedForeignPersonsNotHavingShootingTestParticipations(final List<Person> deletedPersons) {
        // Do not delete persons with ssn
        final List<Person> deletedForeignPerons =
                deletedPersons.stream().filter(Person::isForeignPerson).collect(Collectors.toList());

        shootingTestParticipantRepository
                .countShootingTestParticipationsForForeignPersons(deletedForeignPerons)
                .forEach((foreignPerson, numParticipations) -> {
                    if (numParticipations == 0) {

                        LOG.info("Removing deleted foreign person with id={}", foreignPerson.getId());

                        personRepository.delete(foreignPerson);
                    }
                });
    }
}
