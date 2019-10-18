package fi.riista.integration.common.export;

import fi.riista.config.Constants;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.SystemUserPrivilege;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.gamediary.srva.method.SrvaMethodEnum;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.integration.common.export.srva.CEV_SRVAEvent;
import fi.riista.integration.common.export.srva.CEV_SrvaEvents;
import fi.riista.test.Asserts;
import fi.riista.test.EmbeddedDatabaseTest;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

public class CommonSrvaEventExportFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private CommonSrvaEventExportFeature feature;

    private SystemUser apiUser;
    private GameSpecies mooseSpecies;
    private RiistakeskuksenAlue rka;
    private Riistanhoitoyhdistys rhy;

    @Before
    public void setUp() {
        apiUser = createNewApiUser(SystemUserPrivilege.EXPORT_LUKE_COMMON);
        mooseSpecies = model().newGameSpeciesMoose();
        rka = model().newRiistakeskuksenAlue();
        rhy = model().newRiistanhoitoyhdistys(rka);
    }

    @Test(expected = AccessDeniedException.class)
    public void testAccessDenied() {
        onSavedAndAuthenticated(createNewAdmin(), () -> feature.exportSrvaEvents(2018, 1));
    }

    @Test
    public void testAccessGranted() {
        onSavedAndAuthenticated(
                apiUser,
                () -> Asserts.assertEmpty(feature.exportSrvaEvents(2018, 1).getSrvaEvent()));
    }

    @Test
    public void testFindsWithCorrectInterval() {
        createEvent(2017, 12, 31);
        createEvent(2018, 01, 01);
        createEvent(2018, 01, 31);
        createEvent(2018, 02, 01);

        onSavedAndAuthenticated(apiUser, () -> {
            final CEV_SrvaEvents result = feature.exportSrvaEvents(2018, 1);
            Assert.assertEquals(2, result.getSrvaEvent().size());
            result.getSrvaEvent()
                  .forEach(event -> Assert.assertEquals(1, event.getPointOfTime().monthOfYear().get()));
        });

    }

    @Test
    public void testFindsSpecies() {
        createEvent(2018, 03, 03);
        createEvent(2018, 03, 05);

        onSavedAndAuthenticated(apiUser, () -> {
            final CEV_SrvaEvents result = feature.exportSrvaEvents(2018, 3);

            Assert.assertEquals(2, result.getSrvaEvent().size());
            Assert.assertEquals(2, result.getSrvaSpecimen().size());

            final Set<Long> ids =
                    result.getSrvaEvent().stream().map(e -> e.getSrvaEventId()).collect(Collectors.toSet());

            result.getSrvaSpecimen()
                  .forEach(specimen -> assertTrue(ids.contains(specimen.getSRVAEventId())));
        });
    }

    @Test
    public void testMethodsMarkedCorrectly() {
        final SrvaEvent event = createEvent(2018, 03, 03);
        final SrvaMethodEnum checkedMethod = SrvaMethodEnum.TRACED_WITH_DOG;
        model().newSrvaMethod(event, checkedMethod, true);
        model().newSrvaMethod(event, SrvaMethodEnum.TRACED_WITHOUT_DOG, false);

        onSavedAndAuthenticated(apiUser, () -> {
            final CEV_SrvaEvents result = feature.exportSrvaEvents(2018, 3);

            Assert.assertEquals(1, result.getSrvaEvent().size());

            final CEV_SRVAEvent resultEvent =
                    result.getSrvaEvent().iterator().next();
            Assert.assertEquals(1, resultEvent.getMethod().size());
            Assert.assertEquals(checkedMethod.name(), resultEvent.getMethod().iterator().next().name());
        });
    }

    private SrvaEvent createEvent(final int year, final int month, final int day) {
        // Search criteria uses inclusive search parameter, so one millisecond is added
        final DateTime pointOfTime = new DateTime(year, month, day, 0, 0, 0, 1, Constants.DEFAULT_TIMEZONE);

        final SrvaEvent event = model().newSrvaEvent();
        event.setPointOfTime(pointOfTime.toDate());
        event.setRhy(rhy);
        event.setSpecies(mooseSpecies);

        model().newSrvaSpecimen(event);

        return event;
    }

}
