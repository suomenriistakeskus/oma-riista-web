package fi.riista.feature.huntingclub.permit.summary;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;

public class MooseHuntingSummaryCrudFeatureTest extends EmbeddedDatabaseTest {

    @Resource
    private MooseHuntingSummaryCrudFeature feature;

    private final BiConsumer<HarvestPermit, HuntingClub> FEATURE_CREATE = (permit, club) -> {
        final MooseHuntingSummaryDTO dto = new MooseHuntingSummaryDTO();
        dto.setClubId(club.getId());
        dto.setHarvestPermitId(permit.getId());
        dto.setTotalHuntingArea(1);
        feature.create(dto);
    };

    private final BiConsumer<HarvestPermit, HuntingClub> FEATURE_UPDATE = (permit, club) -> {
        final MooseHuntingSummaryDTO dto = feature.getMooseSummary(club.getId(), permit.getId());
        dto.setTotalHuntingArea(dto.getTotalHuntingArea() - 1);
        feature.update(dto);
    };

    private final BiConsumer<HarvestPermit, HuntingClub> FEATURE_DELETE = (permit, club) -> {
        final MooseHuntingSummaryDTO dto = feature.getMooseSummary(club.getId(), permit.getId());
        feature.delete(dto.getId());
    };

    // no moose data card or moose harvest report

    @Test
    public void testLocked_NoMooseDataCardOrMooseHarvestReport_member() {
        testNoMooseDataCardNoMooseHarvestReport(true, clubGroupUserFunctionsBuilder().createClubMember());
    }

    @Test
    public void testLocked_NoMooseDataCardOrMooseHarvestReport_contact() {
        testNoMooseDataCardNoMooseHarvestReport(false, clubGroupUserFunctionsBuilder().createClubContactPerson());
    }

    @Test
    public void testLocked_NoMooseDataCardOrMooseHarvestReport_leader() {
        testNoMooseDataCardNoMooseHarvestReport(false, clubGroupUserFunctionsBuilder().createGroupLeader());
    }

    @Test
    public void testLocked_NoMooseDataCardOrMooseHarvestReport_moderator() {
        testNoMooseDataCardNoMooseHarvestReport(false, clubGroupUserFunctionsBuilder().createModerator());
    }

    // group from moose data card

    @Test
    public void testLocked_MooseDataCard_member() {
        testMooseDataCard(true, clubGroupUserFunctionsBuilder().createClubMember());
    }

    @Test
    public void testLocked_MooseDataCard_contact() {
        testMooseDataCard(true, clubGroupUserFunctionsBuilder().createClubContactPerson());
    }

    @Test
    public void testLocked_MooseDataCard_leader() {
        testMooseDataCard(true, clubGroupUserFunctionsBuilder().createGroupLeader());
    }

    @Test
    public void testLocked_MooseDataCard_moderator() {
        testMooseDataCard(false, clubGroupUserFunctionsBuilder().createModerator());
    }

    // moose harvest report done

    @Test
    public void testLocked_MooseHarvestReportDone_member() {
        testMooseHarvestReportDone(true, clubGroupUserFunctionsBuilder().createClubMember());
    }

    @Test
    public void testLocked_MooseHarvestReportDone_contact() {
        testMooseHarvestReportDone(true, clubGroupUserFunctionsBuilder().createClubContactPerson());
    }

    @Test
    public void testLocked_MooseHarvestReportDone_leader() {
        testMooseHarvestReportDone(true, clubGroupUserFunctionsBuilder().createGroupLeader());
    }

    @Test
    public void testLocked_MooseHarvestReportDone_moderator() {
        testMooseHarvestReportDone(true, clubGroupUserFunctionsBuilder().createModerator());
    }

    private void testNoMooseDataCardNoMooseHarvestReport(final boolean expectedLocked,
                                                         final BiFunction<HuntingClub, HuntingClubGroup, SystemUser> userFn) {
        runLockedTest(expectedLocked, false, false, userFn);
    }

    private void testMooseDataCard(final boolean expectedLocked,
                                   final BiFunction<HuntingClub, HuntingClubGroup, SystemUser> userFn) {
        runLockedTest(expectedLocked, false, true, userFn);
    }

    private void testMooseHarvestReportDone(final boolean expectedLocked,
                                            final BiFunction<HuntingClub, HuntingClubGroup, SystemUser> userFn) {
        runLockedTest(expectedLocked, true, false, userFn);
    }

    private void runLockedTest(final boolean expectedLocked,
                               final boolean mooseHarvestReportDone,
                               final boolean groupFromMooseDataCard,
                               final BiFunction<HuntingClub, HuntingClubGroup, SystemUser> userFn) {

        withMooseHuntingGroupFixture(f -> {
            f.group.setFromMooseDataCard(groupFromMooseDataCard);

            persistInNewTransaction();

            final MooseHuntingSummary summary = model().newMooseHuntingSummary(f.permit, f.club, true);

            if (mooseHarvestReportDone) {
                model().newMooseHarvestReport(f.speciesAmount);
            }

            onSavedAndAuthenticated(userFn.apply(f.club, f.group), tx(() -> {
                final MooseHuntingSummaryDTO dto = feature.dtoTransformer().apply(summary);
                assertEquals(f.permit.getId(), Long.valueOf(dto.getHarvestPermitId()));
                assertEquals(f.club.getId(), Long.valueOf(dto.getClubId()));
                assertEquals(expectedLocked, dto.isLocked());
            }));
        });
    }

    //
    // no moose data card
    //

    // member

    @Test(expected = AccessDeniedException.class)
    public void testCreate_NoMooseDataCard_member() {
        runTest(false, false, clubGroupUserFunctionsBuilder().createClubMember(), FEATURE_CREATE);
    }

    @Test(expected = AccessDeniedException.class)
    public void testUpdate_NoMooseDataCard_member() {
        runTest(false, true, clubGroupUserFunctionsBuilder().createClubMember(), FEATURE_UPDATE);
    }

    @Test(expected = AccessDeniedException.class)
    public void testDelete_NoMooseDataCard_member() {
        runTest(false, true, clubGroupUserFunctionsBuilder().createClubMember(), FEATURE_DELETE);
    }

    // contact

    @Test
    public void testCreate_NoMooseDataCard_contact() {
        runTest(false, false, clubGroupUserFunctionsBuilder().createClubContactPerson(), FEATURE_CREATE);
    }

    @Test
    public void testUpdate_NoMooseDataCard_contact() {
        runTest(false, true, clubGroupUserFunctionsBuilder().createClubContactPerson(), FEATURE_UPDATE);
    }

    @Test
    public void testDelete_NoMooseDataCard_contact() {
        runTest(false, true, clubGroupUserFunctionsBuilder().createClubContactPerson(), FEATURE_DELETE);
    }

    // leader

    @Test
    public void testCreate_NoMooseDataCard_leader() {
        runTest(false, false, clubGroupUserFunctionsBuilder().createGroupLeader(), FEATURE_CREATE);
    }

    @Test
    public void testUpdate_NoMooseDataCard_leader() {
        runTest(false, true, clubGroupUserFunctionsBuilder().createGroupLeader(), FEATURE_UPDATE);
    }

    @Test
    public void testDelete_NoMooseDataCard_leader() {
        runTest(false, true, clubGroupUserFunctionsBuilder().createGroupLeader(), FEATURE_DELETE);
    }

    // moderator

    @Test
    public void testCreate_NoMooseDataCard_moderator() {
        runTest(false, false, clubGroupUserFunctionsBuilder().createModerator(), FEATURE_CREATE);
    }

    @Test
    public void testUpdate_NoMooseDataCard_moderator() {
        runTest(false, true, clubGroupUserFunctionsBuilder().createModerator(), FEATURE_UPDATE);
    }

    @Test
    public void testDelete_NoMooseDataCard_moderator() {
        runTest(false, true, clubGroupUserFunctionsBuilder().createModerator(), FEATURE_DELETE);
    }

    //
    // with moose data card
    //

    // member

    @Test(expected = AccessDeniedException.class)
    public void testCreate_MooseDataCard_member() {
        runTest(true, false, clubGroupUserFunctionsBuilder().createClubMember(), FEATURE_CREATE);
    }

    @Test(expected = AccessDeniedException.class)
    public void testUpdate_MooseDataCard_member() {
        runTest(true, true, clubGroupUserFunctionsBuilder().createClubMember(), FEATURE_UPDATE);
    }

    @Test(expected = AccessDeniedException.class)
    public void testDelete_MooseDataCard_member() {
        runTest(true, true, clubGroupUserFunctionsBuilder().createClubMember(), FEATURE_DELETE);
    }

    // contact

    @Test(expected = AccessDeniedException.class)
    public void testCreate_MooseDataCard_contact() {
        runTest(true, false, clubGroupUserFunctionsBuilder().createClubContactPerson(), FEATURE_CREATE);
    }

    @Test(expected = AccessDeniedException.class)
    public void testUpdate_MooseDataCard_contact() {
        runTest(true, true, clubGroupUserFunctionsBuilder().createClubContactPerson(), FEATURE_UPDATE);
    }

    @Test(expected = AccessDeniedException.class)
    public void testDelete_MooseDataCard_contact() {
        runTest(true, true, clubGroupUserFunctionsBuilder().createClubContactPerson(), FEATURE_DELETE);
    }

    // leader

    @Test(expected = AccessDeniedException.class)
    public void testCreate_MooseDataCard_leader() {
        runTest(true, false, clubGroupUserFunctionsBuilder().createGroupLeader(), FEATURE_CREATE);
    }

    @Test(expected = AccessDeniedException.class)
    public void testUpdate_MooseDataCard_leader() {
        runTest(true, true, clubGroupUserFunctionsBuilder().createGroupLeader(), FEATURE_UPDATE);
    }

    @Test(expected = AccessDeniedException.class)
    public void testDelete_MooseDataCard_leader() {
        runTest(true, true, clubGroupUserFunctionsBuilder().createGroupLeader(), FEATURE_DELETE);
    }

    // moderator

    @Test
    public void testCreate_MooseDataCard_moderator() {
        runTest(true, false, clubGroupUserFunctionsBuilder().createModerator(), FEATURE_CREATE);
    }

    @Test
    public void testUpdate_MooseDataCard_moderator() {
        runTest(true, true, clubGroupUserFunctionsBuilder().createModerator(), FEATURE_UPDATE);
    }

    @Test
    public void testDelete_MooseDataCard_moderator() {
        runTest(true, true, clubGroupUserFunctionsBuilder().createModerator(), FEATURE_DELETE);
    }

    // given area sizes must be at most permit area size

    @Test(expected = AreaSizeAssertionHelper.TotalAreaSizeTooBigException.class)
    public void testUpdate_totalAreaSizeTooBig() {
        runTest(true, true, clubGroupUserFunctionsBuilder().createModerator(), (permit, club) -> {
            final MooseHuntingSummaryDTO dto = feature.getMooseSummary(club.getId(), permit.getId());
            dto.setTotalHuntingArea(permit.getPermitAreaSize() + 1);
            feature.update(dto);
        });
    }

    @Test(expected = AreaSizeAssertionHelper.EffectiveAreaSizeTooBigException.class)
    public void testUpdate_effectiveAreaSizeTooBig() {
        runTest(true, true, clubGroupUserFunctionsBuilder().createModerator(), (permit, club) -> {
            final MooseHuntingSummaryDTO dto = feature.getMooseSummary(club.getId(), permit.getId());
            dto.setTotalHuntingArea(null);
            dto.setEffectiveHuntingArea(permit.getPermitAreaSize() + 1);
            feature.update(dto);
        });
    }

    private void runTest(final boolean groupFromMooseDataCard,
                         final boolean createSummary,
                         final BiFunction<HuntingClub, HuntingClubGroup, SystemUser> userFn,
                         final BiConsumer<HarvestPermit, HuntingClub> testFn) {

        withMooseHuntingGroupFixture(f -> {
            f.group.setFromMooseDataCard(groupFromMooseDataCard);

            persistInNewTransaction();

            if (createSummary) {
                model().newMooseHuntingSummary(f.permit, f.club, true);
            }

            onSavedAndAuthenticated(userFn.apply(f.club, f.group), () -> testFn.accept(f.permit, f.club));
        });
    }

    @Test(expected = InvalidHuntingEndDateException.class)
    public void testHuntingEndDateNotWithinPermittedDates() {
        withMooseHuntingGroupFixture(f -> {
            persistInNewTransaction();
            final MooseHuntingSummary summary = model().newMooseHuntingSummary(f.permit, f.club, true);
            onSavedAndAuthenticated(createNewModerator(), () -> {
                final MooseHuntingSummaryDTO dto = feature.getMooseSummary(f.club.getId(), f.permit.getId());
                dto.setHuntingEndDate(f.speciesAmount.getBeginDate().minusDays(1));
                feature.update(dto);
            });
        });
    }
}
