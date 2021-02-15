package fi.riista.feature.harvestpermit.report.autoapprove;

import com.google.common.collect.ImmutableSet;
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
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import java.util.List;

import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_ROE_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_BOAR;
import static fi.riista.feature.harvestpermit.report.HarvestReportState.APPROVED;
import static fi.riista.feature.harvestpermit.report.HarvestReportState.SENT_FOR_APPROVAL;
import static fi.riista.test.Asserts.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@RunWith(Theories.class)
public class HarvestReportAutomaticApprovalFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private HarvestReportAutomaticApprovalFeature feature;

    @Resource
    private HarvestRepository harvestRepository;

    @Resource
    private HarvestChangeHistoryRepository harvestChangeHistoryRepository;

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @DataPoints
    public static final ImmutableSet<Integer> BIRDS_REQUIRING_HARVEST_REPORT_AFTER_2020 =
            ImmutableSet.<Integer>builder()
                    .add(GameSpecies.OFFICIAL_CODE_WIGEON)
                    .add(GameSpecies.OFFICIAL_CODE_PINTAIL)
                    .add(GameSpecies.OFFICIAL_CODE_GARGANEY)
                    .add(GameSpecies.OFFICIAL_CODE_SHOVELER)
                    .add(GameSpecies.OFFICIAL_CODE_POCHARD)
                    .add(GameSpecies.OFFICIAL_CODE_TUFTED_DUCK)
                    .add(GameSpecies.OFFICIAL_CODE_COMMON_EIDER)
                    .add(GameSpecies.OFFICIAL_CODE_LONG_TAILED_DUCK)
                    .add(GameSpecies.OFFICIAL_CODE_RED_BREASTED_MERGANSER)
                    .add(GameSpecies.OFFICIAL_CODE_GOOSANDER)
                    .add(GameSpecies.OFFICIAL_CODE_COOT).build();

    @Test
    public void wildBoarAutomaticallyApproved() {
        final DateTime harvestPointOfTime = DateUtil.now();
        final DateTime harvestReportModified = DateUtil.now().minusDays(1);

        runTest(OFFICIAL_CODE_WILD_BOAR, SENT_FOR_APPROVAL, harvestPointOfTime, harvestReportModified, true);
    }

    @Test
    public void roeDeerAutomaticallyApproved() {
        final DateTime harvestPointOfTime = DateUtil.now();
        final DateTime harvestReportModified = DateUtil.now().minusDays(1);

        runTest(OFFICIAL_CODE_ROE_DEER, SENT_FOR_APPROVAL, harvestPointOfTime, harvestReportModified, true);
    }

    @Theory
    public void waterBirdsAutomaticallyApproved(final int gameSpeciesCode) {
        final DateTime harvestPointOfTime = DateUtil.now();
        final DateTime harvestReportModified = DateUtil.now().minusDays(1);

        runTest(gameSpeciesCode, SENT_FOR_APPROVAL, harvestPointOfTime, harvestReportModified, true);
    }

    @Test
    public void otherSpeciesNotAutomaticallyApproved() {
        final int speciesCode = 1;
        final DateTime harvestPointOfTime = DateUtil.now();
        final DateTime harvestReportModified = DateUtil.now().minusDays(1);

        runTest(speciesCode, SENT_FOR_APPROVAL, harvestPointOfTime, harvestReportModified, false);
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
        final DateTime harvestPointOfTime = DateUtil.now();
        final DateTime harvestReportModified = DateUtil.now().minusDays(1);

        runTest(OFFICIAL_CODE_ROE_DEER, state, harvestPointOfTime, harvestReportModified, false);
    }

    @Test
    public void roeDeerHarvestReportModifiedLessThan24HoursAgo() {
        final DateTime harvestPointOfTime = DateUtil.now();
        final DateTime harvestReportModified = DateUtil.now().minusDays(1).plusSeconds(1);

        runTest(OFFICIAL_CODE_ROE_DEER, SENT_FOR_APPROVAL, harvestPointOfTime, harvestReportModified, false);
    }

    @Theory
    public void waterBirdsHarvestReportModifiedLessThan24HoursAgo(final int gameSpeciesCode) {
        final DateTime harvestPointOfTime = DateUtil.now();
        final DateTime harvestReportModified = DateUtil.now().minusDays(1).plusSeconds(1);

        runTest(gameSpeciesCode, SENT_FOR_APPROVAL, harvestPointOfTime, harvestReportModified, false);
    }

    @Test
    public void roeDeerHarvestPointOfTimeTooEarly() {
        final DateTime harvestPointOfTime = new DateTime(2017, 7, 31, 23, 59);
        final DateTime harvestReportModified = DateUtil.now().minusDays(1);

        runTest(OFFICIAL_CODE_ROE_DEER, SENT_FOR_APPROVAL, harvestPointOfTime, harvestReportModified, false);
    }

    @Theory
    public void waterBirdsHarvestPointOfTimeTooEarly(final int gameSpeciesCode) {
        final DateTime harvestPointOfTime = new DateTime(2020, 7, 31, 23, 59);
        final DateTime harvestReportModified = DateUtil.now().minusDays(1);

        runTest(gameSpeciesCode, SENT_FOR_APPROVAL, harvestPointOfTime, harvestReportModified, false);
    }

    private void runTest(final int speciesCode, final HarvestReportState state, final DateTime harvestPointOfTime,
                         final DateTime harvestReportModified, final boolean shouldBeApproved) {

        final GameSpecies species = model().newGameSpecies(speciesCode);
        runTest(species, state, harvestPointOfTime, harvestReportModified, shouldBeApproved);
    }

    private void runTest(final GameSpecies species, final HarvestReportState state, final DateTime harvestPointOfTime,
                         final DateTime harvestReportModified, final boolean shouldBeApproved) {

        final Harvest harvest = model().newHarvest(species);
        harvest.setPointOfTime(harvestPointOfTime);
        harvest.setHarvestReportState(state);
        harvest.setHarvestReportAuthor(harvest.getAuthor());
        harvest.setHarvestReportDate(harvestReportModified);

        persistInNewTransaction();

        runInTransaction(() -> {
            QHarvest HARVEST = QHarvest.harvest;
            jpqlQueryFactory.update(HARVEST)
                    .set(HARVEST.lifecycleFields.modificationTime, harvestReportModified)
                    .where(HARVEST.id.eq(harvest.getId()))
                    .execute();
        });

        authenticate(createNewAdmin());
        feature.runAutoApprove();

        runInTransaction(() -> {
            final List<Harvest> allReports = harvestRepository.findAll();
            assertThat(allReports, hasSize(1));
            final Harvest hr = allReports.get(0);

            if (shouldBeApproved) {
                assertThat(hr.getHarvestReportState(), equalTo(APPROVED));
                assertThat(hr.getConsistencyVersion(), equalTo(1));

                final List<HarvestChangeHistory> stateHistory = harvestChangeHistoryRepository.findAll();
                assertThat(stateHistory, hasSize(1));

                final HarvestChangeHistory historyEvent = stateHistory.get(0);
                assertThat(historyEvent.getReasonForChange(),
                        equalTo(HarvestReportAutomaticApprovalFeature.AUTO_ACCEPT_REASON));

            } else {
                assertThat(hr.getHarvestReportState(), equalTo(state));
                assertThat(hr.getConsistencyVersion(), equalTo(0));
            }
        });
    }
}
