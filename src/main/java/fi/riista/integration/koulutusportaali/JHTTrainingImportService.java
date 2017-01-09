package fi.riista.integration.koulutusportaali;

import fi.riista.feature.organization.person.PersonLookupService;
import fi.riista.feature.organization.jht.training.JHTTraining;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.jht.training.JHTTrainingRepository;
import fi.riista.integration.koulutusportaali.jht.JHT_Suoritus;
import fi.riista.integration.koulutusportaali.jht.JHT_TehtavaTyyppi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Service
public class JHTTrainingImportService {

    @Resource
    private JHTTrainingRepository jhtTrainingRepository;

    @Resource
    private PersonLookupService personLookupService;

    @Transactional
    public void importData(final List<JHT_Suoritus> batch) {
        for (final JHT_Suoritus suoritus : batch) {
            final JHTTraining training = new JHTTraining();
            training.setExternalId(Objects.requireNonNull(suoritus.getId()));
            training.setTrainingType(JHTTraining.TrainingType.SAHKOINEN);
            training.setPerson(requirePerson(suoritus));
            training.setOccupationType(transformJhtOccupationType(Objects.requireNonNull(suoritus.getTehtavaTyyppi())));
            training.setTrainingDate(Objects.requireNonNull(suoritus.getSuoritusPvm()));

            jhtTrainingRepository.save(training);
        }
    }

    private Person requirePerson(final JHT_Suoritus suoritus) {
        if (suoritus.getOmaRiistaPersonId() != null) {
            return personLookupService.findById(suoritus.getOmaRiistaPersonId())
                    .orElseThrow(() -> new IllegalArgumentException("no person found using personId"));
        } else if (suoritus.getHetu() != null) {
            return personLookupService.findBySsnNoFallback(suoritus.getHetu())
                    .orElseThrow(() -> new IllegalArgumentException("no person found using ssn"));
        } else if (suoritus.getMetsastajaNumero() != null) {
            return personLookupService.findByHunterNumber(suoritus.getMetsastajaNumero())
                    .orElseThrow(() -> new IllegalArgumentException("no person found using hunterNumber"));
        }

        throw new IllegalArgumentException("Missing person identifier");
    }

    private static OccupationType transformJhtOccupationType(final JHT_TehtavaTyyppi jhtTehtavaTyyppi) {
        switch (jhtTehtavaTyyppi) {
            case AMPUMAKOKEEN_VASTAANOTTAJA:
                return OccupationType.AMPUMAKOKEEN_VASTAANOTTAJA;
            case METSASTAJATUTKINNON_VASTAANOTTAJA:
                return OccupationType.METSASTAJATUTKINNON_VASTAANOTTAJA;
            case RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA:
                return OccupationType.RHYN_EDUSTAJA_RIISTAVAHINKOJEN_MAASTOKATSELMUKSESSA;
            case METSASTYKSENVALVOJA:
                return OccupationType.METSASTYKSENVALVOJA;
            default:
                break;
        }

        throw new IllegalArgumentException("Could not find mapping for " + jhtTehtavaTyyppi);
    }
}
