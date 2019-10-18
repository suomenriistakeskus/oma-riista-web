package fi.riista.feature.huntingclub.permit.endofhunting.moosesummary;

import fi.riista.feature.harvestpermit.endofhunting.MooselikeHuntingFinishedException;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.Test;

import javax.annotation.Resource;

public class MooseHuntingSummaryFeatureTest extends EmbeddedDatabaseTest implements HuntingGroupFixtureMixin {

    @Resource
    private MooseHuntingSummaryFeature feature;

    @Test
    public void testMarkUnfinished_whenHarvestReportNotDone() {
        doTestMarkUnfinished(false);
    }

    @Test(expected = MooselikeHuntingFinishedException.class)
    public void testMarkUnfinished_whenHarvestReportDone() {
        doTestMarkUnfinished(true);
    }

    private void doTestMarkUnfinished(final boolean permitHolderFinishedHunting) {
        withMooseHuntingGroupFixture(f -> {
            f.speciesAmount.setMooselikeHuntingFinished(permitHolderFinishedHunting);

            persistInNewTransaction();
            final MooseHuntingSummary summary = model().newMooseHuntingSummary(f.permit, f.club, true);

            onSavedAndAuthenticated(createUser(f.clubContact), () -> feature.markUnfinished(summary.getId()));
        });
    }
}
