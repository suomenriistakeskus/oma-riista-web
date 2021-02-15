package fi.riista.feature.gamediary.observation;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.harvestpermit.HarvestPermitLockedByDateService;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.ClubHuntingStatusService;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.permit.endofhunting.HuntingFinishingService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import static java.util.Objects.requireNonNull;

@Component
public class ObservationLockChecker {

    @Resource
    private ClubHuntingStatusService clubHuntingStatusService;

    @Resource
    private HuntingFinishingService huntingFinishingService;

    @Resource
    private HarvestPermitLockedByDateService harvestPermitLockedByDateService;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = Exception.class)
    public ObservationLockInfo getObservationLockInfo(@Nonnull final Observation observation,
                                                      @Nonnull final ObservationSpecVersion specVersion,
                                                      @Nonnull final ObservationModifierInfo modifierInfo) {
        requireNonNull(observation);
        requireNonNull(specVersion);
        requireNonNull(modifierInfo);

        final boolean lockedByDeerHuntingConditions =
                isLockedByDeerHuntingConditions(observation, modifierInfo.isAuthorOrObserver(), specVersion);

        final boolean lockedByMooseHuntingConditions =
                isLockedByMooseHuntingConditions(observation, modifierInfo.getActiveUser());

        final boolean canChangeHuntingDay = !lockedByDeerHuntingConditions && !lockedByMooseHuntingConditions;

        final boolean lockedByLargeCarnivoreConditions =
                observation.isAnyLargeCarnivoreFieldPresent() && !modifierInfo.canUpdateCarnivoreFields();

        final boolean locked =
                lockedByLargeCarnivoreConditions || lockedByDeerHuntingConditions || lockedByMooseHuntingConditions;

        return new ObservationLockInfo(modifierInfo, locked, canChangeHuntingDay);
    }

    // Observations done within deer hunting are allowed to be modified only by ** author or observer. **
    //
    // Moderator or group leader can only reject an observation from the group or accept a rejected
    // observation to be as a part of group's observations.
    //
    // If checks for missing hunting leader or permit are added, it just prevents author or observer to modify
    // observation that was automatically linked to a hunting day.
    private boolean isLockedByDeerHuntingConditions(final Observation observation,
                                                    final boolean authorOrObserver,
                                                    final ObservationSpecVersion specVersion) {

        if (!observation.getObservationCategory().isWithinDeerHunting()) {
            return false;
        }

        if (!specVersion.supportsCategory() || !authorOrObserver) {
            return true;
        }

        final GroupHuntingDay huntingDay = observation.getHuntingDayOfGroup();

        if (huntingDay == null) {
            return false;
        }

        final HuntingClubGroup huntingGroup = huntingDay.getGroup();

        return huntingFinishingService.hasPermitPartnerFinishedHunting(huntingGroup)
                || harvestPermitLockedByDateService.isPermitLocked(huntingGroup)
                || !observation.isCreatedLessThanDayAgo();
    }

    private boolean isLockedByMooseHuntingConditions(final Observation observation, final SystemUser activeUser) {
        if (!observation.getObservationCategory().isWithinMooseHunting()) {
            return false;
        }

        final GroupHuntingDay huntingDay = observation.getHuntingDayOfGroup();

        return huntingDay != null
                && clubHuntingStatusService.isGroupHuntingDataLocked(huntingDay.getGroup(), activeUser);
    }

    /**
     * Checks whether observation is locked out of edits from personal diary.
     *
     * NOTE! Here are no checks on whether permit is locked or permit partner
     * has finished hunting.
     */
    public static boolean isLockedOutOfPersonalDiaryEdits(final @Nonnull Observation observation,
                                                          final boolean authorOrObserver,
                                                          final @Nonnull ObservationSpecVersion specVersion) {
        requireNonNull(observation);
        requireNonNull(specVersion);

        final boolean isLinkedWithHuntingDay = observation.getHuntingDayOfGroup() != null;

        if (observation.getObservationCategory().isWithinDeerHunting()) {
            // Observations done within deer hunting are ** locked ** when
            // ... requester does not support categories (i.e. within deer hunting)
            // ... OR  they're being modified by someone else than author or observer
            // ... OR hunting day is set AND observation is created longer than 24h ago

            return !authorOrObserver
                    || !specVersion.supportsCategory()
                    || isLinkedWithHuntingDay && !observation.isCreatedLessThanDayAgo();
        }

        // Observations NOT done within deer hunting are ** locked ** when
        // ... they're linked to hunting group
        // ... OR  they're being modified by someone else than author or observer
        return !authorOrObserver || isLinkedWithHuntingDay;
    }
}
