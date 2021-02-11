package fi.riista.feature.huntingclub.members.notification;

import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.integration.common.entity.Integration;
import fi.riista.integration.common.repository.IntegrationRepository;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EmailToRhyCoordinatorFeatureRunnerTest extends EmbeddedDatabaseTest {

    @Resource
    private EmailToRhyCoordinatorFeatureRunner runner;

    @Resource
    private IntegrationRepository integrationRepository;

    private HuntingLeaderFinderService feature;
    private HuntingLeaderEmailSenderService mailer;
    private List<Occupation> occupations;
    private ArgumentCaptor<DateTime> beginCaptor;
    private ArgumentCaptor<DateTime> endCaptor;
    private ArgumentCaptor<Integer> huntingYearCaptor;

    @Before
    public void setup() {
        setupMocks();

        model().newIntegration(Integration.EMAIL_HUNTING_LEADERS_TO_RHY_COORDINATOR_ID);

        persistInNewTransaction();

        DateTimeUtils.setCurrentMillisFixed(System.currentTimeMillis());
    }

    private void setupMocks() {
        feature = mock(HuntingLeaderFinderService.class);
        mailer = mock(HuntingLeaderEmailSenderService.class);
        occupations = mock(List.class);

        when(feature.findChangedLeaders(any(), any(), anyInt())).thenReturn(occupations);

        beginCaptor = ArgumentCaptor.forClass(DateTime.class);
        endCaptor = ArgumentCaptor.forClass(DateTime.class);
        huntingYearCaptor = ArgumentCaptor.forClass(int.class);
    }

    @After
    public void tearDown() {
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test
    public void testFirstCallWhenLastRunIsNull() {
        onSavedAndAuthenticated(createNewAdmin(), () -> {
            doTest(DateTime.now().minusDays(1), DateTime.now());
        });
    }

    @Test
    public void testFistCallEndIsSecondCallStart() {
        onSavedAndAuthenticated(createNewAdmin(), () -> {
            final DateTime firstCallEnd = DateTime.now();
            doTest(DateTime.now().minusDays(1), firstCallEnd);
            setupMocks();
            tick();
            doTest(firstCallEnd, DateTime.now());
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testUnauthorized() {
        onSavedAndAuthenticated(createNewModerator(), () -> {
            doTest(DateTime.now().minusDays(1), DateTime.now());
        });
    }

    private static void tick() {
        DateTimeUtils.setCurrentMillisFixed(System.currentTimeMillis());
    }

    private void doTest(final DateTime expectedBegin, final DateTime expectedEnd) {
        runner.process(feature, mailer);

        verify(feature).findChangedLeaders(beginCaptor.capture(), endCaptor.capture(), huntingYearCaptor.capture());
        verify(mailer).sendMails(eq(occupations));

        assertEquals(expectedBegin, beginCaptor.getValue());
        assertEquals(expectedEnd, endCaptor.getValue());

        final int currentHuntingYear = DateUtil.huntingYear();
        assertEquals(Integer.valueOf(currentHuntingYear), huntingYearCaptor.getValue());

        runInTransaction(() -> assertEquals(DateTime.now(), getIntegration().getLastRun()));
    }


    private Integration getIntegration() {
        return integrationRepository.getOne(Integration.EMAIL_HUNTING_LEADERS_TO_RHY_COORDINATOR_ID);
    }
}
