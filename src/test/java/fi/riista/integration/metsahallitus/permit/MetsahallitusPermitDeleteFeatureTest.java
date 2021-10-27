package fi.riista.integration.metsahallitus.permit;

import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import fi.riista.util.MockTimeProvider;
import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

public class MetsahallitusPermitDeleteFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private MetsahallitusPermitDeleteFeature feature;

    @Resource
    private MetsahallitusPermitRepository repository;

    @After
    public void tearDown() {
        MockTimeProvider.resetMock();
    }

    @Test
    public void testDeleteOldPermits() {
        final LocalDate today = DateUtil.today();
        final int huntingYear = DateUtil.huntingYear();
        final LocalDate pastDate = new LocalDate(huntingYear - 2, 7, 31);

        // Old permits with end date (to be deleted)
        IntStream.range(0, 6).forEach(i -> {
            final MetsahallitusPermit permit = model().newMetsahallitusPermit();
            permit.setEndDate(pastDate.minusDays(i));
        });

        // Old permits without end date and old modification date (to be deleted)
        IntStream.range(0, 7).forEach(i -> {
            final MetsahallitusPermit permit = new MetsahallitusPermit();
            final String identifier = "1000" + i;
            permit.setStatus("status");
            permit.setPermitIdentifier(identifier);
            permit.setHunterNumber("11111111");
            permit.setAreaNumber("1234");
            MockTimeProvider.mockTime(pastDate.minusDays(i).toDate().getTime());
            repository.save(permit);
        });

        MockTimeProvider.resetMock();

        // New permits with end date
        IntStream.range(0, 3).forEach(i -> {
            final MetsahallitusPermit permit = model().newMetsahallitusPermit();
            permit.setEndDate(today.plusDays(i));
        });

        // New permits without end date and modification date now (or in the future)
        IntStream.range(0, 2).forEach(i -> {
            final MetsahallitusPermit permit = new MetsahallitusPermit();
            final String identifier = "2000" + i;
            permit.setStatus("status");
            permit.setPermitIdentifier(identifier);
            permit.setHunterNumber("11111111");
            permit.setAreaNumber("1234");
            MockTimeProvider.mockTime(today.plusDays(i).toDate().getTime());
            repository.save(permit);
        });

        onSavedAndAuthenticated(createNewAdmin(), () -> {
            feature.deleteOldPermits();

            runInTransaction(() -> {
                final List<MetsahallitusPermit> permits = repository.findAll();
                assertThat(permits, hasSize(5));
            });
        });
    }

    @Test(expected = AccessDeniedException.class)
    public void testDeleteOldPermits_moderator() {
        final MetsahallitusPermit permit = model().newMetsahallitusPermit();
        permit.setEndDate(DateUtil.today());

        onSavedAndAuthenticated(createNewModerator(), () -> {
            feature.deleteOldPermits();
        });
    }
}
