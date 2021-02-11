package fi.riista.feature.gamediary.harvest;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.season.HarvestSeason;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.organization.person.Person;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

public class HarvestLockedCondition {

    @Nonnull
    public static Predicate<Harvest> createContactPersonTester(final @Nullable Person activePerson) {
        return harvest -> activePerson != null
                && harvest != null
                && harvest.getHarvestPermit() != null
                && harvest.getHarvestPermit().hasContactPerson(activePerson);
    }

    public static boolean canEditFromWeb(@Nullable final Person activePerson,
                                         @Nonnull final Harvest harvest,
                                         @Nonnull final Predicate<Harvest> isGroupHuntingLockedTester,
                                         @Nonnull final Predicate<Harvest> isContactPersonTester) {

        requireNonNull(harvest);
        requireNonNull(isGroupHuntingLockedTester);
        requireNonNull(isContactPersonTester);

        return canEdit(activePerson, harvest, null, isGroupHuntingLockedTester, isContactPersonTester);
    }

    public static boolean canEditFromMobile(@Nonnull final Person activePerson,
                                            @Nonnull final Harvest harvest,
                                            @Nonnull final HarvestSpecVersion harvestSpecVersion,
                                            @Nonnull final Predicate<Harvest> isGroupHuntingLockedTester,
                                            @Nonnull final Predicate<Harvest> isContactPersonTester) {

        requireNonNull(activePerson);
        requireNonNull(harvest);
        requireNonNull(harvestSpecVersion);
        requireNonNull(isGroupHuntingLockedTester);
        requireNonNull(isContactPersonTester);

        return canEdit(activePerson, harvest, harvestSpecVersion, isGroupHuntingLockedTester, isContactPersonTester);
    }

    // for web harvestSpecVersion == null
    // for mobile harvestSpecVersion != null
    // for moderator activePerson == null
    // isGroupHuntingLockedTester is given as parameter so that DTO can be canEdit=false but business logic can do correct checks.
    // isContactPersonTester is given as parameter to allow faster contact person resolution in DTO transformer.
    private static boolean canEdit(@Nullable final Person activePerson,
                                   @Nonnull final Harvest harvest,
                                   @Nullable final HarvestSpecVersion harvestSpecVersion,
                                   @Nonnull final Predicate<Harvest> isGroupHuntingLockedTester,
                                   @Nonnull final Predicate<Harvest> isContactPersonTester) {

        if (harvest.isHarvestReportApproved() || harvest.isHarvestReportRejected()) {
            if (activePerson != null) {
                // only moderator can edit harvest reports
                return false;
            }
        }

        final HarvestPermit permit = harvest.getHarvestPermit();
        final HarvestSeason season = harvest.getHarvestSeason();
        final GroupHuntingDay huntingGroup = harvest.getHuntingDayOfGroup();

        if (activePerson == null && permit == null && season == null && huntingGroup == null) {
            // moderator can not edit private diary entry unless mooselike proposed for hunting day
            final GameSpecies species = harvest.getSpecies();
            return species != null && species.isMooseOrDeerRequiringPermitForHunting();
        }

        // Check mobile specVersion support
        if (harvestSpecVersion != null) {
            // full support for all harvest report fields is mandatory
            if (season != null && !harvestSpecVersion.supportsHarvestReport()) {
                return false;
            }

            // no version currently has support for group hunting day
            if (huntingGroup != null) {
                return false;
            }
        }

        if (huntingGroup != null && isGroupHuntingLockedTester.test(harvest)) {
            return false;
        }

        if (permit != null && harvest.isAcceptedToHarvestPermit()) {
            if (permit.isHarvestReportApproved() || permit.isHarvestReportRejected()) {
                return false;
            }

            if (activePerson == null) {
                return true;
            }

            return !permit.isHarvestReportDone() && isContactPersonTester.test(harvest);
        }

        return true;
    }
}
