package fi.riista.feature.huntingclub.members.notification;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.HuntingClubGroupRepository;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.util.DateUtil;
import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HuntingLeaderFinderServiceTest extends EmbeddedDatabaseTest {

    @Resource
    private HuntingLeaderFinderService service;

    @Resource
    private OccupationRepository occupationRepository;

    @Resource
    private HuntingClubGroupRepository clubGroupRepository;

    private Riistanhoitoyhdistys rhy;

    @Before
    public void setUpRhy() {
        rhy = model().newRiistanhoitoyhdistys();
    }

    @Test
    public void testAnyChangesTooOld() {
        final DateTime when = now().minusDays(1);
        createOccupationBackInTime(when, OccupationType.RYHMAN_JASEN);
        createOccupationBackInTime(when, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        createDeletedOccupationBackInTime(when, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        persistBackInTime(when);

        findAndAssert(when.plusMillis(1));
    }

    @Test
    public void testChangesAreRecentEnough() {
        final DateTime when = now().minusDays(1);
        createOccupationBackInTime(when, OccupationType.RYHMAN_JASEN);
        final Occupation o1 = createOccupationBackInTime(when, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        final Occupation o2 = createDeletedOccupationBackInTime(when, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        persistBackInTime(when);

        findAndAssert(when, o1, o2);
    }

    @Test
    public void testGroupIsMooseDataCard() {
        final DateTime when = now().minusDays(1);
        createOccupationBackInTime(when, OccupationType.RYHMAN_JASEN);
        final Occupation o1 = createOccupationBackInTimeWithMooseDataCard(when, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        final Occupation o2 = createOccupationBackInTime(when, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        persistBackInTime(when);

        findAndAssert(when, o2);
    }

    @Test
    public void testGroupHasNoPermit() {
        final DateTime when = now().minusDays(1);
        createOccupationBackInTime(when, OccupationType.RYHMAN_JASEN);
        final Occupation o1 = createOccupationBackInTimeWithoutPermit(when, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        final Occupation o2 = createOccupationBackInTime(when, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        persistBackInTime(when);

        findAndAssert(when, o2);
    }

    @Test
    public void testGroupPermitIsSet() {
        final DateTime when = now().minusDays(1);
        createOccupationBackInTime(when, OccupationType.RYHMAN_JASEN);
        final Occupation o1 = createOccupationBackInTimeWithoutPermit(when, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        final Occupation o2 = createOccupationBackInTime(when, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        persistBackInTime(when);

        final DateTime now = now();
        runInTransaction(() -> {
            final HuntingClubGroup group = clubGroupRepository.getOne(o2.getOrganisation().getId());
            group.updateHarvestPermit(model().newHarvestPermit(this.rhy));
            persistInCurrentlyOpenTransaction();
        });

        findAndAssert(now, o2);
    }

    @Test
    public void testChangesAreTooNew() {
        final DateTime when = now().minusDays(1);
        createOccupationBackInTime(when, OccupationType.RYHMAN_JASEN);
        final Occupation o1 = createOccupationBackInTime(when, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        final Occupation o2 = createDeletedOccupationBackInTime(when, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        persistBackInTime(when);

        findAndAssert(when.minusSeconds(2), when.minusSeconds(1));
    }

    @Test
    public void testSkipGroupForPreviousHuntingYear() {
        final DateTime when = now().minusDays(1);
        createOccupationBackInTime(when, OccupationType.RYHMAN_JASEN);

        final Occupation occupation = createOccupationBackInTime(when, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        final int previousHuntingYear = DateUtil.getFirstCalendarYearOfCurrentHuntingYear() - 1;
        final HuntingClubGroup huntingClubGroup = HuntingClubGroup.class.cast(occupation.getOrganisation());
        huntingClubGroup.setHuntingYear(previousHuntingYear);

        persistBackInTime(when);

        // Not included
        findAndAssert(when);
    }

    @Test
    public void testNonPrimaryLeadersModified() {
        final DateTime when = now().minusDays(1);
        final Occupation o1 = createOccupationBackInTime(when, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        final Occupation o2 = createOccupationBackInTime(when, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        o1.setCallOrder(1);
        o2.setCallOrder(2);
        persistBackInTime(when);

        final DateTime now = now();
        runInTransaction(() -> {
            occupationRepository.getOne(o1.getId()).setCallOrder(2);
            occupationRepository.getOne(o2.getId()).setCallOrder(1);
        });

        findAndAssert(now);
    }

    @Test
    public void testPrimaryLeadersModified() {
        final DateTime when = now().minusDays(1);
        final Occupation o1 = createOccupationBackInTime(when, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        final Occupation o2 = createOccupationBackInTime(when, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        o1.setCallOrder(11);
        o2.setCallOrder(22);
        persistBackInTime(when);

        final DateTime now = now();
        runInTransaction(() -> occupationRepository.getOne(o2.getId()).setCallOrder(0));

        findAndAssert(now, o2);
    }

    @Test
    public void testModifiedRecently() {
        testOccupationFoundAfterModifyAction(occ -> occ.setAdditionalInfo(""));
    }

    @Test
    public void testDeletedRecently() {
        testOccupationFoundAfterModifyAction(LifecycleEntity::softDelete);
    }

    @Test
    public void testModifiedAndDeletedRecently() {
        testOccupationFoundAfterModifyAction((occ) -> {
            occ.setAdditionalInfo("");
            occ.softDelete();
        });
    }

    private void testOccupationFoundAfterModifyAction(Consumer<Occupation> modifyAction) {
        final DateTime when = now().minusDays(1);
        createOccupationBackInTime(when, OccupationType.RYHMAN_JASEN);
        createDeletedOccupationBackInTime(when, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        final Occupation o1 = createOccupationBackInTime(when, OccupationType.RYHMAN_METSASTYKSENJOHTAJA);
        o1.setCallOrder(0);

        persistBackInTime(when);
        final DateTime now = now();
        inTransactionWithOccupation(o1, modifyAction);

        findAndAssert(now, o1);
    }

    private void inTransactionWithOccupation(Occupation occ, Consumer<Occupation> consumer) {
        runInTransaction(() -> consumer.accept(occupationRepository.getOne(occ.getId())));
    }

    private Occupation createOccupationBackInTime(DateTime when, OccupationType occType) {
        return createOccupationBackInTime(when, occType, false, false, true);
    }

    private Occupation createDeletedOccupationBackInTime(DateTime when, OccupationType occType) {
        return createOccupationBackInTime(when, occType, true, false, true);
    }

    private Occupation createOccupationBackInTimeWithoutPermit(DateTime when, OccupationType occType) {
        return createOccupationBackInTime(when, occType, false, false, false);
    }

    private Occupation createOccupationBackInTimeWithMooseDataCard(DateTime when, OccupationType occType) {
        return createOccupationBackInTime(when, occType, false, true, true);
    }

    private Occupation createOccupationBackInTime(DateTime when, OccupationType occType, boolean isDeleted, Boolean fromMooseDataCard, boolean hasPermit) {
        return runBackInTime(when, () -> {
            HuntingClubGroup group = model().newHuntingClubGroup(model().newHuntingClub(this.rhy));
            group.setFromMooseDataCard(fromMooseDataCard);
            Occupation occ = model().newHuntingClubGroupMember(group, occType);
            if (isDeleted) {
                occ.softDelete();
            }
            if (hasPermit) {
                group.updateHarvestPermit(model().newHarvestPermit(this.rhy));
            }
            return occ;
        });
    }

    private void persistBackInTime(DateTime when) {
        runBackInTime(when, () -> {
            persistInNewTransaction();
            return null;
        });
    }

    private static <T> T runBackInTime(DateTime when, Supplier<T> cmd) {
        try {
            DateTimeUtils.setCurrentMillisFixed(when.getMillis());
            return cmd.get();
        } finally {
            DateTimeUtils.setCurrentMillisSystem();
        }
    }

    private static DateTime now() {
        return DateUtil.now();
    }

    private void findAndAssert(DateTime begin, Occupation... expecteds) {
        final DateTime end = now();
        findAndAssert(begin, end, expecteds);
    }

    private void findAndAssert(DateTime begin, DateTime end, Occupation... expecteds) {
        runInTransaction(() -> {
            final int currentHuntingYear = DateUtil.getFirstCalendarYearOfCurrentHuntingYear();
            assertLeaders(service.findChangedLeaders(begin.toDate(), end.toDate(), currentHuntingYear), expecteds);
        });
    }

    private static void assertLeaders(List<Occupation> huntingLeaders, Occupation... expecteds) {
        assertEquals(expecteds.length, huntingLeaders.size());
        for (Occupation expected : expecteds) {
            assertTrue(huntingLeaders.stream().anyMatch(o -> o.getId().equals(expected.getId())));
        }
    }
}
