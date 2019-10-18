package fi.riista.feature.huntingclub.permit.endofhunting.moosesummary;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitLockedByDateService;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.fixture.HuntingGroupFixtureMixin;
import fi.riista.feature.huntingclub.permit.endofhunting.AreaSizeAssertionHelper;
import fi.riista.feature.huntingclub.permit.endofhunting.InvalidHuntingEndDateException;
import fi.riista.test.EmbeddedDatabaseTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;

import javax.annotation.Resource;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

public class MooseHuntingSummaryCrudFeatureTest extends EmbeddedDatabaseTest implements HuntingGroupFixtureMixin {

    @Resource
    private MooseHuntingSummaryCrudFeature feature;

    @Resource
    private HarvestPermitLockedByDateService harvestPermitLockedByDateService;

    @Before
    public void disablePermitLockByDate() {
        harvestPermitLockedByDateService.disableLockingForTests();
    }

    @After
    public void enablePermitLockByDate() {
        harvestPermitLockedByDateService.normalLocking();
    }

    private final BiConsumer<HarvestPermit, HuntingClub> FEATURE_CREATE = (permit, club) -> {
        final MooseHuntingSummaryDTO dto = new MooseHuntingSummaryDTO();
        dto.setClubId(club.getId());
        dto.setHarvestPermitId(permit.getId());
        dto.setTotalHuntingArea(1);
        feature.create(dto);
    };

    private final Consumer<MooseHuntingSummary> FEATURE_UPDATE = (summary) -> {
        final MooseHuntingSummaryDTO dto = feature.read(summary.getId());
        dto.setTotalHuntingArea(dto.getTotalHuntingArea() - 1);
        feature.update(dto);
    };

    private final Consumer<MooseHuntingSummary> FEATURE_DELETE = (summary) -> {
        feature.delete(summary.getId());
    };

    // no moose data card or moose harvest report

    @Test
    public void testLocked_NoMooseDataCardAndPermitHolderNotFinishedHunting_member() {
        testNoMooseDataCardPermitHolderNotFinishedHunting(true, clubGroupUserFunctionsBuilder().createClubMember());
    }

    @Test
    public void testLocked_NoMooseDataCardAndPermitHolderNotFinishedHunting_contact() {
        testNoMooseDataCardPermitHolderNotFinishedHunting(false, clubGroupUserFunctionsBuilder().createClubContactPerson());
    }

    @Test
    public void testLocked_NoMooseDataCardAndPermitHolderNotFinishedHunting_leader() {
        testNoMooseDataCardPermitHolderNotFinishedHunting(false, clubGroupUserFunctionsBuilder().createGroupLeader());
    }

    @Test
    public void testLocked_NoMooseDataCardAndPermitHolderNotFinishedHunting_moderator() {
        testNoMooseDataCardPermitHolderNotFinishedHunting(false, clubGroupUserFunctionsBuilder().createModerator());
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
    public void testLocked_PermitHolderFinishedHunting_member() {
        testPermitHolderFinishedHunting(true, clubGroupUserFunctionsBuilder().createClubMember());
    }

    @Test
    public void testLocked_PermitHolderFinishedHunting_contact() {
        testPermitHolderFinishedHunting(true, clubGroupUserFunctionsBuilder().createClubContactPerson());
    }

    @Test
    public void testLocked_PermitHolderFinishedHunting_leader() {
        testPermitHolderFinishedHunting(true, clubGroupUserFunctionsBuilder().createGroupLeader());
    }

    @Test
    public void testLocked_PermitHolderFinishedHunting_moderator() {
        testPermitHolderFinishedHunting(true, clubGroupUserFunctionsBuilder().createModerator());
    }

    private void testNoMooseDataCardPermitHolderNotFinishedHunting(final boolean expectedLocked,
                                                                   final BiFunction<HuntingClub, HuntingClubGroup, SystemUser> userFn) {
        runLockedTest(expectedLocked, false, false, userFn);
    }

    private void testMooseDataCard(final boolean expectedLocked,
                                   final BiFunction<HuntingClub, HuntingClubGroup, SystemUser> userFn) {
        runLockedTest(expectedLocked, false, true, userFn);
    }

    private void testPermitHolderFinishedHunting(final boolean expectedLocked,
                                                 final BiFunction<HuntingClub, HuntingClubGroup, SystemUser> userFn) {
        runLockedTest(expectedLocked, true, false, userFn);
    }

    private void runLockedTest(final boolean expectedLocked,
                               final boolean permitHolderFinishedHunting,
                               final boolean groupFromMooseDataCard,
                               final BiFunction<HuntingClub, HuntingClubGroup, SystemUser> userFn) {

        withMooseHuntingGroupFixture(f -> {
            f.group.setFromMooseDataCard(groupFromMooseDataCard);
            f.speciesAmount.setMooselikeHuntingFinished(permitHolderFinishedHunting);

            persistInNewTransaction();

            final MooseHuntingSummary summary = model().newMooseHuntingSummary(f.permit, f.club, true);

            onSavedAndAuthenticated(userFn.apply(f.club, f.group), tx(() -> {
                final MooseHuntingSummaryDTO dto = feature.toDTO(summary);
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
        runTest(false, clubGroupUserFunctionsBuilder().createClubMember(), FEATURE_UPDATE);
    }

    @Test(expected = AccessDeniedException.class)
    public void testDelete_NoMooseDataCard_member() {
        runTest(false, clubGroupUserFunctionsBuilder().createClubMember(), FEATURE_DELETE);
    }

    // contact

    @Test
    public void testCreate_NoMooseDataCard_contact() {
        runTest(false, false, clubGroupUserFunctionsBuilder().createClubContactPerson(), FEATURE_CREATE);
    }

    @Test
    public void testUpdate_NoMooseDataCard_contact() {
        runTest(false, clubGroupUserFunctionsBuilder().createClubContactPerson(), FEATURE_UPDATE);
    }

    @Test
    public void testDelete_NoMooseDataCard_contact() {
        runTest(false, clubGroupUserFunctionsBuilder().createClubContactPerson(), FEATURE_DELETE);
    }

    // leader

    @Test
    public void testCreate_NoMooseDataCard_leader() {
        runTest(false, false, clubGroupUserFunctionsBuilder().createGroupLeader(), FEATURE_CREATE);
    }

    @Test
    public void testUpdate_NoMooseDataCard_leader() {
        runTest(false, clubGroupUserFunctionsBuilder().createGroupLeader(), FEATURE_UPDATE);
    }

    @Test
    public void testDelete_NoMooseDataCard_leader() {
        runTest(false, clubGroupUserFunctionsBuilder().createGroupLeader(), FEATURE_DELETE);
    }

    // moderator

    @Test
    public void testCreate_NoMooseDataCard_moderator() {
        runTest(false, false, clubGroupUserFunctionsBuilder().createModerator(), FEATURE_CREATE);
    }

    @Test
    public void testUpdate_NoMooseDataCard_moderator() {
        runTest(false, clubGroupUserFunctionsBuilder().createModerator(), FEATURE_UPDATE);
    }

    @Test
    public void testDelete_NoMooseDataCard_moderator() {
        runTest(false, clubGroupUserFunctionsBuilder().createModerator(), FEATURE_DELETE);
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
        runTest(true, clubGroupUserFunctionsBuilder().createClubMember(), FEATURE_UPDATE);
    }

    @Test(expected = AccessDeniedException.class)
    public void testDelete_MooseDataCard_member() {
        runTest(true, clubGroupUserFunctionsBuilder().createClubMember(), FEATURE_DELETE);
    }

    // contact

    @Test(expected = AccessDeniedException.class)
    public void testCreate_MooseDataCard_contact() {
        runTest(true, false, clubGroupUserFunctionsBuilder().createClubContactPerson(), FEATURE_CREATE);
    }

    @Test(expected = AccessDeniedException.class)
    public void testUpdate_MooseDataCard_contact() {
        runTest(true, clubGroupUserFunctionsBuilder().createClubContactPerson(), FEATURE_UPDATE);
    }

    @Test(expected = AccessDeniedException.class)
    public void testDelete_MooseDataCard_contact() {
        runTest(true, clubGroupUserFunctionsBuilder().createClubContactPerson(), FEATURE_DELETE);
    }

    // leader

    @Test(expected = AccessDeniedException.class)
    public void testCreate_MooseDataCard_leader() {
        runTest(true, false, clubGroupUserFunctionsBuilder().createGroupLeader(), FEATURE_CREATE);
    }

    @Test(expected = AccessDeniedException.class)
    public void testUpdate_MooseDataCard_leader() {
        runTest(true, clubGroupUserFunctionsBuilder().createGroupLeader(), FEATURE_UPDATE);
    }

    @Test(expected = AccessDeniedException.class)
    public void testDelete_MooseDataCard_leader() {
        runTest(true, clubGroupUserFunctionsBuilder().createGroupLeader(), FEATURE_DELETE);
    }

    // moderator

    @Test
    public void testCreate_MooseDataCard_moderator() {
        runTest(true, false, clubGroupUserFunctionsBuilder().createModerator(), FEATURE_CREATE);
    }

    @Test
    public void testUpdate_MooseDataCard_moderator() {
        runTest(true, clubGroupUserFunctionsBuilder().createModerator(), FEATURE_UPDATE);
    }

    @Test
    public void testDelete_MooseDataCard_moderator() {
        runTest(true, clubGroupUserFunctionsBuilder().createModerator(), FEATURE_DELETE);
    }

    // given area sizes must be at most permit area size

    @Test(expected = AreaSizeAssertionHelper.TotalAreaSizeTooBigException.class)
    public void testUpdate_totalAreaSizeTooBig() {
        runTest(true, clubGroupUserFunctionsBuilder().createModerator(), (summary) -> {
            final MooseHuntingSummaryDTO dto = feature.read(summary.getId());
            dto.setTotalHuntingArea(summary.getHarvestPermit().getPermitAreaSize() + 1);
            feature.update(dto);
        });
    }

    @Test(expected = AreaSizeAssertionHelper.EffectiveAreaSizeTooBigException.class)
    public void testUpdate_effectiveAreaSizeTooBig() {
        runTest(true, clubGroupUserFunctionsBuilder().createModerator(), (summary) -> {
            final MooseHuntingSummaryDTO dto = feature.read(summary.getId());
            dto.setTotalHuntingArea(null);
            dto.setEffectiveHuntingArea(summary.getHarvestPermit().getPermitAreaSize() + 1);
            feature.update(dto);
        });
    }

    private void runTest(final boolean groupFromMooseDataCard,
                         final boolean createSummary,
                         final BiFunction<HuntingClub, HuntingClubGroup, SystemUser> userFn,
                         final BiConsumer<HarvestPermit, HuntingClub> testFn) {

        withMooseHuntingGroupFixture(f -> {
            f.group.setFromMooseDataCard(groupFromMooseDataCard);

            if (createSummary) {
                persistInNewTransaction();
                model().newMooseHuntingSummary(f.permit, f.club, true);
            }

            onSavedAndAuthenticated(userFn.apply(f.club, f.group), () -> testFn.accept(f.permit, f.club));
        });
    }

    private void runTest(final boolean groupFromMooseDataCard,
                         final BiFunction<HuntingClub, HuntingClubGroup, SystemUser> userFn,
                         final Consumer<MooseHuntingSummary> testFn) {

        withMooseHuntingGroupFixture(f -> {
            f.group.setFromMooseDataCard(groupFromMooseDataCard);

            persistInNewTransaction();

            final MooseHuntingSummary summary = model().newMooseHuntingSummary(f.permit, f.club, true);

            onSavedAndAuthenticated(userFn.apply(f.club, f.group), () -> testFn.accept(summary));
        });
    }

    @Test(expected = InvalidHuntingEndDateException.class)
    public void testHuntingEndDateNotWithinPermittedDates() {
        withMooseHuntingGroupFixture(f -> {
            persistInNewTransaction();
            final MooseHuntingSummary summary = model().newMooseHuntingSummary(f.permit, f.club, true);

            onSavedAndAuthenticated(createNewModerator(), () -> {
                final MooseHuntingSummaryDTO dto = feature.read(summary.getId());
                dto.setHuntingEndDate(f.speciesAmount.getBeginDate().minusDays(1));
                feature.update(dto);
            });
        });
    }
}
