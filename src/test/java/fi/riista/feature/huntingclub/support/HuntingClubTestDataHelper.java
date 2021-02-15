package fi.riista.feature.huntingclub.support;

import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.permit.HasHarvestCountsForPermit;
import fi.riista.feature.organization.person.Person;
import fi.riista.util.DateUtil;

import javax.annotation.Nonnull;
import java.util.List;

import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_FEMALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_MALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.YOUNG_FEMALE;
import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.YOUNG_MALE;
import static fi.riista.test.TestUtils.concatAndShuffle;
import static fi.riista.test.TestUtils.createList;
import static java.util.Objects.requireNonNull;

public abstract class HuntingClubTestDataHelper {

    protected abstract EntitySupplier model();

    public HasHarvestCountsForPermit generateHarvestCounts() {
        final int numAdultMales = 7;
        final int numAdultFemales = 11;
        final int numYoungMales = 13;
        final int numYoungFemales = 17;

        final int numNotEdibleAdults = model().nextIntBetween(2, numAdultMales + numAdultFemales);
        final int numNotEdibleYoungs = model().nextIntBetween(2, numYoungMales + numYoungFemales);

        return HasHarvestCountsForPermit.of(numAdultMales, numAdultFemales, numYoungMales, numYoungFemales,
                numNotEdibleAdults, numNotEdibleYoungs);
    }

    public void createHarvestsForHuntingGroup(@Nonnull final HuntingClubGroup group,
                                              @Nonnull final Person author,
                                              @Nonnull final HasHarvestCountsForPermit harvestCounts) {

        requireNonNull(group, "group is null");
        requireNonNull(author, "author is null");
        requireNonNull(harvestCounts, "harvestCounts is null");

        final GameSpecies species = group.getSpecies();
        final GroupHuntingDay huntingDay = model().newGroupHuntingDay(group, DateUtil.today());

        final List<HarvestSpecimen> adultMales = createList(
                harvestCounts.getNumberOfAdultMales(),
                () -> model().newHarvestSpecimen(model().newHarvest(species, author, huntingDay), ADULT_MALE));

        final List<HarvestSpecimen> adultFemales = createList(
                harvestCounts.getNumberOfAdultFemales(),
                () -> model().newHarvestSpecimen(model().newHarvest(species, author, huntingDay), ADULT_FEMALE));

        final List<HarvestSpecimen> youngMales = createList(
                harvestCounts.getNumberOfYoungMales(),
                () -> model().newHarvestSpecimen(model().newHarvest(species, author, huntingDay), YOUNG_MALE));

        final List<HarvestSpecimen> youngFemales = createList(
                harvestCounts.getNumberOfYoungFemales(),
                () -> model().newHarvestSpecimen(model().newHarvest(species, author, huntingDay), YOUNG_FEMALE));

        markEdibles(concatAndShuffle(adultMales, adultFemales), harvestCounts.getNumberOfNonEdibleAdults());
        markEdibles(concatAndShuffle(youngMales, youngFemales), harvestCounts.getNumberOfNonEdibleYoungs());
    }

    private static void markEdibles(final Iterable<HarvestSpecimen> specimens, final int numberOfNonEdibles) {
        int counter = 0;

        for (final HarvestSpecimen specimen : specimens) {
            final boolean notEdible = numberOfNonEdibles > counter++;
            specimen.setNotEdible(notEdible);
        }
    }
}
