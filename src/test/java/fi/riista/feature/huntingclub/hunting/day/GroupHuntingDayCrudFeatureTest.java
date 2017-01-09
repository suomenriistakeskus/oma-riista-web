package fi.riista.feature.huntingclub.hunting.day;

import fi.riista.feature.EmbeddedDatabaseTest;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.ClubHuntingFinishedException;
import fi.riista.feature.account.user.UserAuthorizationHelper;
import org.joda.time.LocalTime;
import org.junit.Test;

import org.springframework.security.access.AccessDeniedException;
import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static fi.riista.util.DateUtil.today;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class GroupHuntingDayCrudFeatureTest extends EmbeddedDatabaseTest {

    private enum PermissionTestScheme {

        NORMAL_GROUP(false, false),
        NORMAL_GROUP_HUNTING_FINISHED(false, true),
        IMPORT_GROUP(true, false),
        IMPORT_GROUP_HUNTING_FINISHED(true, true);

        private final boolean fromMooseDataCard;
        private final boolean huntingFinished;

        PermissionTestScheme(final boolean fromMooseDataCard, final boolean huntingFinished) {
            this.fromMooseDataCard = fromMooseDataCard;
            this.huntingFinished = huntingFinished;
        }
    }

    @Resource
    private GroupHuntingDayCrudFeature crudFeature;

    @Resource
    private GroupHuntingDayRepository huntingDayRepo;

    @Resource
    private UserAuthorizationHelper userAuthHelper;

    @Test
    public void testCreateHuntingDay_withHounds_thenNumberOfHoundsIsSet() {
        withMooseHuntingGroupFixture(f -> onSavedAndAuthenticated(createUser(f.groupLeader), () -> {
            final GroupHuntingDayDTO saved =
                    crudFeature.create(createHuntingDayDTO(f.group, true).withNumberOfHounds(2));

            runInTransaction(() -> assertEquals(2, (int) huntingDayRepo.getOne(saved.getId()).getNumberOfHounds()));
        }));
    }

    @Test(expected = NumberOfHoundsMissingException.class)
    public void testCreateHuntingDay_withHounds_thenNumberOfHoundsMustBeGiven() {
        withMooseHuntingGroupFixture(f -> onSavedAndAuthenticated(createUser(f.groupLeader), () -> {
            crudFeature.create(createHuntingDayDTO(f.group, true).withNumberOfHounds(null));
        }));
    }

    @Test
    public void testCreateHuntingDay_withoutHounds_thenNumberOfHoundsIsSetToNull() {
        withMooseHuntingGroupFixture(f -> onSavedAndAuthenticated(createUser(f.groupLeader), () -> {
            final GroupHuntingDayDTO saved =
                    crudFeature.create(createHuntingDayDTO(f.group, false).withNumberOfHounds(2));

            runInTransaction(() -> assertNull(huntingDayRepo.getOne(saved.getId()).getNumberOfHounds()));
        }));
    }

    private final Consumer<HuntingClubGroup> FEATURE_CREATE = group -> {
        crudFeature.create(createHuntingDayDTO(group, false));
    };

    private final Consumer<HuntingClubGroup> FEATURE_UPDATE = group -> {
        final List<GroupHuntingDay> list = huntingDayRepo.findByGroup(group);
        final GroupHuntingDayDTO dto = crudFeature.read(list.get(0).getId());
        dto.setSnowDepth(50);
        crudFeature.update(dto);
    };

    private final Consumer<HuntingClubGroup> FEATURE_DELETE = group -> {
        crudFeature.delete(huntingDayRepo.findByGroup(group).get(0).getId());
    };

    @Test
    public void testCreatePermissions() {
        Stream.of(PermissionTestScheme.values()).forEach(groupScheme -> {
            testEditPermissions("Create hunting day", groupScheme, FEATURE_CREATE);
        });
    }

    @Test
    public void testUpdatePermissions() {
        Stream.of(PermissionTestScheme.values()).forEach(groupScheme -> {
            testEditPermissions("Update hunting day", groupScheme, FEATURE_UPDATE);
        });
    }

    @Test
    public void testDeletePermissions() {
        Stream.of(PermissionTestScheme.values()).forEach(groupScheme -> {
            testEditPermissions("Delete hunting day", groupScheme, FEATURE_DELETE);
        });
    }

    private static GroupHuntingDayDTO createHuntingDayDTO(final HuntingClubGroup group, final boolean withHounds) {
        final GroupHuntingDayDTO dto = new GroupHuntingDayDTO();
        dto.setHuntingGroupId(group.getId());

        dto.setStartDate(today());
        dto.setEndDate(today());

        dto.setStartTime(LocalTime.now());
        dto.setEndTime(LocalTime.now().plusHours(1));

        dto.setBreakDurationInMinutes(10);
        dto.setNumberOfHunters(1);

        dto.setHuntingMethod(getAnyHuntingMethodByHounds(withHounds));
        return dto;
    }

    private static GroupHuntingMethod getAnyHuntingMethodByHounds(final boolean hounds) {
        return Stream.of(GroupHuntingMethod.values())
                .filter(ghm -> ghm.isWithHound() == hounds)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Something went wrong, GroupHuntingMethod not found"));
    }

    private void testEditPermissions(
            final String testName, final PermissionTestScheme groupScheme, final Consumer<HuntingClubGroup> testFn) {

        clubGroupUserFunctionsBuilder().withAdminAndModerator(true).build().forEach(userFn -> {
            withMooseHuntingGroupFixture(f -> {
                f.group.setFromMooseDataCard(groupScheme.fromMooseDataCard);

                // Create one hunting day for update/delete test.
                model().newGroupHuntingDay(f.group, today().minusDays(1));

                if (groupScheme.huntingFinished) {
                    // Need to flush before creating MooseHuntingSummary in order to have
                    // harvest_permit_partners table populated.
                    persistInNewTransaction();
                    model().newMooseHuntingSummary(f.permit, f.club, true);
                }

                onSavedAndAuthenticated(userFn.apply(f.club, f.group), user -> {
                    try {
                        testFn.accept(f.group);

                        if (groupScheme.huntingFinished) {
                            fail(String.format("%s by %s should have failed when hunting is finished.",
                                    testName, user.getUsername()));
                        }

                        if (groupScheme.fromMooseDataCard && !user.isModeratorOrAdmin()) {
                            fail(String.format(
                                    "%s by %s should have failed when group is created from moose data card and user is not moderator.",
                                    testName, user.getUsername()));
                        }

                        Optional.ofNullable(user.getPerson()).ifPresent(p -> {
                            if (!userAuthHelper.isClubContact(f.club, p) && !userAuthHelper.isGroupLeader(f.group, p)) {
                                fail(String.format(
                                        "%s by %s should have failed because user is not club contact nor group leader.",
                                        testName, user.getUsername()));
                            }
                        });
                    } catch (final AccessDeniedException e) {
                        if (!groupScheme.huntingFinished) {
                            if (user.isModeratorOrAdmin()) {
                                fail(String.format(
                                        "%s should have succeeded for moderator when hunting is not finished.",
                                        testName, user.getUsername()));
                            } else if (!groupScheme.fromMooseDataCard) {
                                Optional.ofNullable(user.getPerson()).ifPresent(p -> {
                                    if (userAuthHelper.isClubContact(f.club, p) || userAuthHelper.isGroupLeader(f.group, p)) {
                                        fail(String.format(
                                                "%s by %s should have succeeded for user who is club contact or group leader when group not created from moose data card.",
                                                testName, user.getUsername()));
                                    }
                                });
                            }
                        }
                    } catch (final ClubHuntingFinishedException e) {
                        if (!groupScheme.huntingFinished) {
                            fail(String.format("%s by %s raised %s unexpectedly while hunting is not finished.",
                                    testName, user.getUsername(), ClubHuntingFinishedException.class.getSimpleName()));
                        }
                    }
                });
            });

            reset();
        });
    }

}
