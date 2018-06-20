package fi.riista.feature.harvestpermit.report.autoapprove;

import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.HarvestChangeHistory;
import fi.riista.feature.gamediary.HarvestChangeHistoryRepository;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestRepository;
import fi.riista.feature.gamediary.harvest.QHarvest;
import fi.riista.feature.harvestpermit.report.HarvestReportState;
import fi.riista.test.EmbeddedDatabaseTest;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class HarvestReportAutomaticApprovalFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HarvestReportAutomaticApprovalFeature harvestReportAutomaticApprovalFeature;

    @Resource
    private HarvestRepository harvestRepository;

    @Resource
    private HarvestChangeHistoryRepository harvestChangeHistoryRepository;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Test
    public void wildBoarAutomaticallyApproved() {
        final int speciesCode = GameSpecies.OFFICIAL_CODE_WILD_BOAR;
        final HarvestReportState state = HarvestReportState.SENT_FOR_APPROVAL;
        final DateTime harvestPointOfTime = DateUtil.now();
        final DateTime harvestReportModified = DateUtil.now().minusDays(1);

        runTest(speciesCode, state, harvestPointOfTime, harvestReportModified, true);
    }

    @Test
    public void roeDeerAutomaticallyApproved() {
        final int speciesCode = GameSpecies.OFFICIAL_CODE_ROE_DEER;
        final HarvestReportState state = HarvestReportState.SENT_FOR_APPROVAL;
        final DateTime harvestPointOfTime = DateUtil.now();
        final DateTime harvestReportModified = DateUtil.now().minusDays(1);

        runTest(speciesCode, state, harvestPointOfTime, harvestReportModified, true);
    }

    @Test
    public void otherSpeciesAutomaticallyApproved() {
        final int speciesCode = 1;
        final HarvestReportState state = HarvestReportState.SENT_FOR_APPROVAL;
        final DateTime harvestPointOfTime = DateUtil.now();
        final DateTime harvestReportModified = DateUtil.now().minusDays(1);

        runTest(speciesCode, state, harvestPointOfTime, harvestReportModified, false);
    }

    @Test
    public void testWrongStateApproved() {
        testWrongState(HarvestReportState.APPROVED);
    }

    @Test
    public void testWrongStateRejected() {
        testWrongState(HarvestReportState.REJECTED);
    }

    private void testWrongState(HarvestReportState state) {
        final int speciesCode = GameSpecies.OFFICIAL_CODE_ROE_DEER;
        final DateTime harvestPointOfTime = DateUtil.now();
        final DateTime harvestReportModified = DateUtil.now().minusDays(1);

        runTest(speciesCode, state, harvestPointOfTime, harvestReportModified, false);
    }

    @Test
    public void roeDeerHarvestReportModifiedLessThan24HoursAgo() {
        final int speciesCode = GameSpecies.OFFICIAL_CODE_ROE_DEER;
        final HarvestReportState state = HarvestReportState.SENT_FOR_APPROVAL;
        final DateTime harvestPointOfTime = DateUtil.now();
        final DateTime harvestReportModified = DateUtil.now().minusDays(1).plusSeconds(1);

        runTest(speciesCode, state, harvestPointOfTime, harvestReportModified, false);
    }

    @Test
    public void roeDeerHarvestPointOfTimeTooEarly() {
        final int speciesCode = GameSpecies.OFFICIAL_CODE_ROE_DEER;
        final HarvestReportState state = HarvestReportState.SENT_FOR_APPROVAL;
        final DateTime harvestPointOfTime = new DateTime(2017, 7, 31, 23, 59);
        final DateTime harvestReportModified = DateUtil.now().minusDays(1);

        runTest(speciesCode, state, harvestPointOfTime, harvestReportModified, false);
    }

    private void runTest(final int speciesCode, final HarvestReportState state, final DateTime harvestPointOfTime,
                         final DateTime harvestReportModified, final boolean shouldBeApproved) {

        final GameSpecies species = model().newGameSpecies(speciesCode);
        runTest(species, state, harvestPointOfTime, harvestReportModified, shouldBeApproved);
    }

    private void runTest(final GameSpecies species, final HarvestReportState state, final DateTime harvestPointOfTime,
                         final DateTime harvestReportModified, final boolean shouldBeApproved) {

        final Harvest harvest = model().newHarvest(species);
        harvest.setPointOfTime(harvestPointOfTime.toDate());
        harvest.setHarvestReportState(state);
        harvest.setHarvestReportAuthor(harvest.getAuthor());
        harvest.setHarvestReportDate(harvestReportModified);

        persistInNewTransaction();

        runInTransaction(() -> {
            QHarvest HARVEST = QHarvest.harvest;
            jpqlQueryFactory.update(HARVEST)
                    .set(HARVEST.lifecycleFields.modificationTime, harvestReportModified.toDate())
                    .where(HARVEST.id.eq(harvest.getId()))
                    .execute();
        });

        authenticate(createNewAdmin());
        harvestReportAutomaticApprovalFeature.runAutoApprove();

        runInTransaction(() -> {
            final List<Harvest> allReports = harvestRepository.findAll();
            assertEquals(1, allReports.size());
            final Harvest hr = allReports.get(0);

            if (shouldBeApproved) {
                assertEquals(HarvestReportState.APPROVED, hr.getHarvestReportState());
                assertEquals(1, hr.getConsistencyVersion().intValue());

                final List<HarvestChangeHistory> stateHistory = harvestChangeHistoryRepository.findAll();
                assertEquals(1, stateHistory.size());

                final HarvestChangeHistory historyEvent = stateHistory.get(0);
                assertEquals(HarvestReportAutomaticApprovalFeature.AUTO_ACCEPT_REASON, historyEvent.getReasonForChange());

            } else {
                assertEquals(state, hr.getHarvestReportState());
                assertEquals(0, hr.getConsistencyVersion().intValue());
            }
        });
    }
}
