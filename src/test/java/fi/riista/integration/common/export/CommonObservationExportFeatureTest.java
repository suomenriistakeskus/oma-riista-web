package fi.riista.integration.common.export;

import fi.riista.config.Constants;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.harvestpermit.HarvestPermitRepository;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.integration.common.export.observations.COBS_Observation;
import fi.riista.integration.common.export.observations.COBS_ObservationSpecimen;
import fi.riista.integration.common.export.observations.COBS_Observations;
import fi.riista.test.Asserts;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.List;

public class CommonObservationExportFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private CommonObservationExportFeature feature;

    @Resource
    private HarvestPermitRepository harvestPermitRepository;

    private SystemUser apiUser;
    private GameSpecies mooseSpecies;
    private RiistakeskuksenAlue rka;
    private Riistanhoitoyhdistys rhy;
    private Person observationperson;

    @Before
    public void setUp() {
        apiUser = createNewApiUser(SystemUserPrivilege.EXPORT_LUKE_COMMON);
        mooseSpecies = model().newGameSpeciesMoose();
        rka = model().newRiistakeskuksenAlue();
        rhy = model().newRiistanhoitoyhdistys(rka);
        observationperson = model().newPerson();


    }

    @Test(expected = AccessDeniedException.class)
    public void testAccessDenied() {
        onSavedAndAuthenticated(createNewAdmin(), () -> feature.exportObservations(2018, 1));
    }

    @Test
    public void testAccessGranted() {
        onSavedAndAuthenticated(
                apiUser,
                () -> Asserts.assertEmpty(feature.exportObservations(2018, 1).getObservation()));
    }

    @Test
    public void testFindsWithCorrectInterval() {
        createObservation(2017, 12, 31);
        createObservation(2018, 01, 01);
        createObservation(2018, 01, 31);
        createObservation(2018, 02, 01);

        onSavedAndAuthenticated(apiUser, () -> {
            final COBS_Observations result = feature.exportObservations(2018, 1);
            Assert.assertEquals(2, result.getObservation().size());
            result.getObservation()
                  .forEach(observation -> Assert.assertEquals(1, observation.getPointOfTime().monthOfYear().get()));
        });

    }

    @Test
    public void testExportsOnlyObservationsWithRhy() {
        final Observation observation = createObservation(2018, 02, 15);
        observation.setRhy(null);

        onSavedAndAuthenticated(
                apiUser,
                () -> Asserts.assertEmpty(feature.exportObservations(2018, 2).getObservation()));

    }

    @Test
    public void testFindsSpeciesForObservation() {
        createObservation(2018, 02, 15);

        onSavedAndAuthenticated(
                apiUser,
                () -> {
                    final COBS_Observations result = feature.exportObservations(2018, 2);
                    final List<COBS_Observation> observations = feature.exportObservations(2018, 2).getObservation();
                    final List<COBS_ObservationSpecimen> specimens =
                            feature.exportObservations(2018, 2).getObservationSpecimen();

                    Assert.assertEquals(1, observations.size());
                    Assert.assertEquals(1, specimens.size());
                    Assert.assertEquals(observations.iterator().next().getObservationId(),
                                        specimens.iterator().next().getObservationId());
                });

    }

    private Observation createObservation(final int year, final int month, final int day) {
        // Search criteria uses inclusive search parameter, so one millisecond is added
        final DateTime dateTime = new DateTime(year, month, day, 0, 0, 0, 1, Constants.DEFAULT_TIMEZONE);
        final Observation observation = model().newObservation(
                mooseSpecies, observationperson);
        observation.setPointOfTime(dateTime.toDate());
        observation.setRhy(rhy);
        model().newObservationSpecimen(observation);

        return observation;
    }
}
