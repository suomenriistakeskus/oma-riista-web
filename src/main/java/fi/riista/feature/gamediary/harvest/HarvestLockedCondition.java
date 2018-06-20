package fi.riista.feature.gamediary.harvest;

import fi.riista.feature.organization.person.Person;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Predicate;

public class HarvestLockedCondition {
    @Nonnull
    public static Predicate<Harvest> createContactPersonTester(final @Nullable Person activePerson) {
        return harvest -> activePerson != null
                && harvest != null
                && harvest.getHarvestPermit() != null
                && harvest.getHarvestPermit().hasContactPerson(activePerson);
    }

    // for web harvestSpecVersion == null
    // for mobile harvestSpecVersion != null
    // for moderator activePerson == null
    // isGroupHuntingLockedTester is given as parameter so that DTO can be canEdit=false but business logic can do correct checks.
    // isContactPersonTester is given as parameter to allow faster contact person resolution in DTO transformer.
    public static boolean canEdit(
            final Person activePerson,
            @Nonnull final Harvest harvest,
            final HarvestSpecVersion harvestSpecVersion,
            @Nonnull final Predicate<Harvest> isGroupHuntingLockedTester,
            @Nonnull final Predicate<Harvest> isContactPersonTester) {
        Objects.requireNonNull(harvest, "harvest is null");
        Objects.requireNonNull(isGroupHuntingLockedTester);
        Objects.requireNonNull(isContactPersonTester);

        if (harvest.isHarvestReportApproved() || harvest.isHarvestReportRejected()) {
            return false;
        }

        if (activePerson == null
                && harvest.getHarvestPermit() == null
                && harvest.getHarvestSeason() == null
                && harvest.getHuntingDayOfGroup() == null) {
            // moderator can not edit private diary entry unless mooselike proposed for hunting day
            return harvest.getSpecies() != null && harvest.getSpecies().isMooseOrDeerRequiringPermitForHunting();
        }

        // Check mobile specVersion support
        if (harvestSpecVersion != null) {
            // basic support for harvest permit is mandatory
            if (harvest.getHarvestPermit() != null && !harvestSpecVersion.supportsHarvestPermitState()) {
                return false;
            }

            // full support for all harvest report fields is mandatory
            if (harvest.getHarvestSeason() != null && !harvestSpecVersion.supportsHarvestReport()) {
                return false;
            }

            // no version currently has support for group hunting day
            if (harvest.getHuntingDayOfGroup() != null && !harvestSpecVersion.supportGroupHuntingDay()) {
                return false;
            }
        }

        if (harvest.getHuntingDayOfGroup() != null && isGroupHuntingLockedTester.test(harvest)) {
            return false;
        }

        if (harvest.getHarvestPermit() != null && harvest.isAcceptedToHarvestPermit()) {
            if (harvest.getHarvestPermit().isHarvestReportDone()) {
                // end of hunting done
                return false;
            }

            // contact person or moderator
            return activePerson == null || isContactPersonTester.test(harvest);
        }

        return true;
    }
}
