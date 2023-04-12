package fi.riista.integration.koulutusportaali;

import fi.riista.feature.common.training.TrainingType;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonLookupService;
import fi.riista.feature.organization.rhy.training.OccupationTraining;
import fi.riista.feature.organization.rhy.training.OccupationTrainingRepository;
import fi.riista.integration.koulutusportaali.other.OTH_Suoritus;
import fi.riista.integration.koulutusportaali.other.OTH_TehtavaTyyppi;
import fi.riista.util.F;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

import static fi.riista.feature.organization.rhy.training.OccupationTraining.FOREIGN_PERSON_ELIGIBLE_FOR_OCCUPATION_TRAINING;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

@Service
public class OtherTrainingImportService {

    @Resource
    private OccupationTrainingRepository trainingRepository;

    @Resource
    private PersonLookupService personLookupService;

    @Transactional
    public void importData(final List<OTH_Suoritus> batch) {

        // Searching by id is the primary way to identify person. Search all persons in
        // the batch, allow N+1 for other identification methods (hunter number, ssn)
        final Map<Long, Person> personsById = personLookupService.findByIdIn(
                F.mapNonNullsToList(batch, OTH_Suoritus::getOmaRiistaPersonId),
                FOREIGN_PERSON_ELIGIBLE_FOR_OCCUPATION_TRAINING);

        for (final OTH_Suoritus suoritus : batch) {
            final OccupationTraining training = new OccupationTraining();
            training.setExternalId(requireNonNull(suoritus.getId()));
            training.setTrainingType(TrainingType.SAHKOINEN);
            training.setPerson(requirePerson(suoritus, personsById));
            training.setOccupationType(transformJhtOccupationType(requireNonNull(suoritus.getTehtavaTyyppi())));
            training.setTrainingDate(requireNonNull(suoritus.getSuoritusPvm()));

            trainingRepository.save(training);
        }
    }

    private Person requirePerson(final OTH_Suoritus suoritus, final Map<Long, Person> personsById) {
        if (suoritus.getOmaRiistaPersonId() != null) {
            return ofNullable(personsById.get(suoritus.getOmaRiistaPersonId()))
                    .orElseThrow(() -> new IllegalArgumentException("no person found using personId"));
        } else if (suoritus.getHetu() != null) {
            return personLookupService
                    .findBySsnNoFallback(suoritus.getHetu())
                    .orElseThrow(() -> new IllegalArgumentException("no person found using ssn"));

        } else if (suoritus.getMetsastajaNumero() != null) {
            return personLookupService
                    .findByHunterNumber(suoritus.getMetsastajaNumero(), FOREIGN_PERSON_ELIGIBLE_FOR_OCCUPATION_TRAINING)
                    .orElseThrow(() -> new IllegalArgumentException("no person found using hunterNumber"));
        }

        throw new IllegalArgumentException("Missing person identifier");
    }

    private static OccupationType transformJhtOccupationType(final OTH_TehtavaTyyppi tehtavaTyyppi) {
        switch (tehtavaTyyppi) {
            case PETOYHDYSHENKILO:
                return OccupationType.PETOYHDYSHENKILO;
            default:
                break;
        }

        throw new IllegalArgumentException("Could not find mapping for " + tehtavaTyyppi);
    }
}
