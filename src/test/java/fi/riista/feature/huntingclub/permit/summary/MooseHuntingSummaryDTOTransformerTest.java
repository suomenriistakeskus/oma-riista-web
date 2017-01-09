package fi.riista.feature.huntingclub.permit.summary;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.util.jpa.HibernateStatisticsAssertions;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MooseHuntingSummaryDTOTransformerTest extends EmbeddedDatabaseTest {

    @Resource
    private MooseHuntingSummaryDTOTransformer transformer;

    @Resource
    private MooseHuntingSummaryRepository repository;

    @Test
    @HibernateStatisticsAssertions(queryCount = 4)
    public void testQueryCountWithOneSummary() {
        withMooseHuntingGroupFixture(fixture -> {
            persistInNewTransaction();
            createSummary(fixture, false);

            onSavedAndAuthenticated(createNewModerator(), () -> {
                final List<MooseHuntingSummaryDTO> result =
                        callInTransaction(() -> transformer.transform(repository.findAll()));
                assertEquals(1, result.size());
            });
        });
    }

    @Test
    @HibernateStatisticsAssertions(queryCount = 5)
    public void testQueryCountWithMultipleSummaries() {
        withMooseHuntingGroupFixture(fixture -> {
            persistInNewTransaction();
            createSummary(fixture, false);

            for (int i = 0; i < 10; i++) {
                withHuntingGroupFixture(fixture.rhy, fixture.species, fixture2 -> {
                    persistInNewTransaction();
                    createSummary(fixture2, true);
                });
            }

            onSavedAndAuthenticated(createNewModerator(), () -> {
                final List<MooseHuntingSummaryDTO> result =
                        callInTransaction(() -> transformer.transform(repository.findAll()));

                assertEquals(11, result.size());
            });
        });
    }

    private void createSummary(final HuntingGroupFixture fixture, final boolean groupFromMooseDataCard) {
        model().newMooseHuntingSummary(fixture.permit, fixture.club, true);
        fixture.group.setFromMooseDataCard(groupFromMooseDataCard);
    }
}
