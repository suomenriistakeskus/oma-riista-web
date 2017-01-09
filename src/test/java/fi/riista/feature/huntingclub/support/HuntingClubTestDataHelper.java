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
import java.util.Objects;
import java.util.stream.Stream;

import static fi.riista.feature.gamediary.GameAge.ADULT;
import static fi.riista.feature.gamediary.GameAge.YOUNG;
import static fi.riista.feature.gamediary.GameGender.FEMALE;
import static fi.riista.feature.gamediary.GameGender.MALE;
import static fi.riista.util.TestUtils.createList;

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

    public void createHarvestsForHuntingGroup(
            @Nonnull final HuntingClubGroup group,
            @Nonnull final Person author,
            @Nonnull final HasHarvestCountsForPermit harvestCounts) {

        Objects.requireNonNull(group, "group is null");
        Objects.requireNonNull(author, "author is null");
        Objects.requireNonNull(harvestCounts, "harvestCounts is null");

        final GameSpecies species = group.getSpecies();
        final GroupHuntingDay huntingDay = model().newGroupHuntingDay(group, DateUtil.today());

        final List<HarvestSpecimen> adultMales = createList(
                harvestCounts.getNumberOfAdultMales(),
                () -> model().newHarvestSpecimen(model().newHarvest(species, author, huntingDay), ADULT, MALE));

        final List<HarvestSpecimen> adultFemales = createList(
                harvestCounts.getNumberOfAdultFemales(),
                () -> model().newHarvestSpecimen(model().newHarvest(species, author, huntingDay), ADULT, FEMALE));

        final List<HarvestSpecimen> youngMales = createList(
                harvestCounts.getNumberOfYoungMales(),
                () -> model().newHarvestSpecimen(model().newHarvest(species, author, huntingDay), YOUNG, MALE));

        final List<HarvestSpecimen> youngFemales = createList(
                harvestCounts.getNumberOfYoungFemales(),
                () -> model().newHarvestSpecimen(model().newHarvest(species, author, huntingDay), YOUNG, FEMALE));

        Stream.concat(adultMales.stream(), adultFemales.stream())
                .limit(harvestCounts.getNumberOfNonEdibleAdults())
                .forEach(adult -> adult.setNotEdible(true));

        Stream.concat(youngMales.stream(), youngFemales.stream())
                .limit(harvestCounts.getNumberOfNonEdibleYoungs())
                .forEach(young -> young.setNotEdible(true));
    }

}
