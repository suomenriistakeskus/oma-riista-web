package fi.riista.integration.koulutusportaali;

import fi.riista.feature.common.training.TrainingType;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.training.OccupationTraining;
import fi.riista.feature.organization.rhy.training.OccupationTrainingRepository;
import fi.riista.integration.koulutusportaali.other.OTH_Suoritus;
import fi.riista.integration.koulutusportaali.other.OTH_TehtavaTyyppi;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.DataIntegrityViolationException;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.test.Asserts.assertThat;
import static fi.riista.util.DateUtil.today;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class OtherTrainingImportServiceTest extends EmbeddedDatabaseTest {

    private Person person;

    @Resource
    private OtherTrainingImportService service;

    @Resource
    private OccupationTrainingRepository repository;

    @Before
    public void setup() {
        person = model().newPerson();
        persistInNewTransaction();
    }

    @Test
    public void testImportPetoyhdyshenkiloTranining_hunterNumber() {
        final String id = "id-for-occupation-training-test";

        final OTH_Suoritus suoritus = new OTH_Suoritus()
                .withId(id)
                .withMetsastajaNumero(person.getHunterNumber())
                .withTehtavaTyyppi(OTH_TehtavaTyyppi.PETOYHDYSHENKILO)
                .withSuoritusPvm(today());

        service.importData(singletonList(suoritus));

        runInTransaction(() -> {
            final List<OccupationTraining> trainings = repository.findAll();
            assertThat(trainings, hasSize(1));

            final OccupationTraining training = trainings.get(0);

            assertThat(training.getExternalId(), equalTo(id));
            assertThat(training.getPerson(), equalTo(person));
            assertThat(training.getOccupationType(), equalTo(OccupationType.PETOYHDYSHENKILO));
            assertThat(training.getTrainingDate(), equalTo(today()));
            assertThat(training.getTrainingType(), equalTo(TrainingType.SAHKOINEN));
        });
    }

    @Test
    public void testImportPetoyhdyshenkiloTranining_ssn() {
        final String id = "id-for-occupation-training-test";

        final OTH_Suoritus suoritus = new OTH_Suoritus()
                .withId(id)
                .withHetu(person.getSsn())
                .withTehtavaTyyppi(OTH_TehtavaTyyppi.PETOYHDYSHENKILO)
                .withSuoritusPvm(today());

        service.importData(singletonList(suoritus));

        runInTransaction(() -> {
            final List<OccupationTraining> trainings = repository.findAll();
            assertThat(trainings, hasSize(1));

            final OccupationTraining training = trainings.get(0);

            assertThat(training.getExternalId(), equalTo(id));
            assertThat(training.getPerson(), equalTo(person));
            assertThat(training.getOccupationType(), equalTo(OccupationType.PETOYHDYSHENKILO));
            assertThat(training.getTrainingDate(), equalTo(today()));
            assertThat(training.getTrainingType(), equalTo(TrainingType.SAHKOINEN));
        });
    }

    @Test
    public void testImportPetoyhdyshenkiloTranining_personId() {
        final String id = "id-for-occupation-training-test";

        final OTH_Suoritus suoritus = new OTH_Suoritus()
                .withId(id)
                .withOmaRiistaPersonId(person.getId())
                .withTehtavaTyyppi(OTH_TehtavaTyyppi.PETOYHDYSHENKILO)
                .withSuoritusPvm(today());

        service.importData(singletonList(suoritus));

        runInTransaction(() -> {
            final List<OccupationTraining> trainings = repository.findAll();
            assertThat(trainings, hasSize(1));

            final OccupationTraining training = trainings.get(0);

            assertThat(training.getExternalId(), equalTo(id));
            assertThat(training.getPerson(), equalTo(person));
            assertThat(training.getOccupationType(), equalTo(OccupationType.PETOYHDYSHENKILO));
            assertThat(training.getTrainingDate(), equalTo(today()));
            assertThat(training.getTrainingType(), equalTo(TrainingType.SAHKOINEN));
        });
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void testImportPetoyhdyshenkiloTranining_personId_duplicateExternalIdsNotAllowed() {
        final String id = "id-for-occupation-training-test";

        final OTH_Suoritus suoritus = new OTH_Suoritus()
                .withId(id)
                .withOmaRiistaPersonId(person.getId())
                .withTehtavaTyyppi(OTH_TehtavaTyyppi.PETOYHDYSHENKILO)
                .withSuoritusPvm(today());

        service.importData(asList(suoritus, suoritus));
    }
}
