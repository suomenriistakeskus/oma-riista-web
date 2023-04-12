package fi.riista.feature.harvestpermit.report.jhtarchive;

import fi.riista.feature.common.fixture.FixtureMixin;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameCategory;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.hunting.day.GroupHuntingDay;
import fi.riista.feature.huntingclub.permit.HasHarvestCountsForPermit;
import fi.riista.feature.huntingclub.permit.endofhunting.AreaSizeAndRemainingPopulation;
import fi.riista.feature.huntingclub.permit.endofhunting.basicsummary.BasicClubHuntingSummary;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmount;
import fi.riista.util.DateUtil;

import java.util.List;
import java.util.function.Consumer;

import static fi.riista.util.DateUtil.currentYear;

public interface MooselikeFixtureMixin extends FixtureMixin {

    default void withMooselikeHarvestFixture(final Consumer<MooselikeFixture> consumer) {
        consumer.accept(new MooselikeFixture(getEntitySupplier()));
    }

    class MooselikeFixture {
        private final EntitySupplier es;

        public final RiistakeskuksenAlue rka;
        public final Riistanhoitoyhdistys rhy;
        public final HuntingClub club;
        public final HarvestPermitApplication application;
        public final PermitDecision decision;
        public final HarvestPermit permit;

        public final GameSpecies moose;
        public final HuntingClubGroup mooseGroup;
        public final GroupHuntingDay mooseHuntingDay;
        public final HarvestPermitApplicationSpeciesAmount mooseApplicationAmounts;
        public final PermitDecisionSpeciesAmount mooseDecisionAmounts;
        public final HarvestPermitSpeciesAmount moosePermitAmounts;

        public final GameSpecies deer;
        public final HuntingClubGroup deerGroup;
        public final GroupHuntingDay deerHuntingDay;
        public final HarvestPermitApplicationSpeciesAmount deerApplicationAmounts;
        public final PermitDecisionSpeciesAmount deerDecisionAmounts;
        public final HarvestPermitSpeciesAmount deerPermitAmounts;

        public MooselikeFixture(final EntitySupplier es) {
            this.es = es;
            rka = es.newRiistakeskuksenAlue();
            rhy = es.newRiistanhoitoyhdistys(rka);
            club = es.newHuntingClub(rhy);
            application = es.newHarvestPermitApplication(rhy, es.newHarvestPermitArea(), HarvestPermitCategory.MOOSELIKE);
            decision = es.newPermitDecision(application);
            permit = es.newMooselikePermit(rhy, currentYear());
            permit.setPermitDecision(decision);

            moose = es.newGameSpecies(GameSpecies.OFFICIAL_CODE_MOOSE, GameCategory.GAME_MAMMAL, "hirvi", "hirvi", "hirvi");
            mooseGroup = es.newHuntingClubGroup(club, moose);
            mooseGroup.updateHarvestPermit(permit);
            mooseHuntingDay = es.newGroupHuntingDay(mooseGroup, DateUtil.today());
            mooseApplicationAmounts = es.newHarvestPermitApplicationSpeciesAmount(application, moose, 1f);
            mooseDecisionAmounts = es.newPermitDecisionSpeciesAmount(decision, moose, 1f);
            moosePermitAmounts = es.newHarvestPermitSpeciesAmount(permit, moose, 1f);

            deer = es.newGameSpecies(GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER, GameCategory.GAME_MAMMAL, "peura", "peura", "peura");
            deerGroup = es.newHuntingClubGroup(club, deer);
            deerGroup.updateHarvestPermit(permit);
            deerHuntingDay = es.newGroupHuntingDay(deerGroup, DateUtil.today());
            deerApplicationAmounts = es.newHarvestPermitApplicationSpeciesAmount(application, deer, 1f);
            deerDecisionAmounts = es.newPermitDecisionSpeciesAmount(decision, deer, 1f);
            deerPermitAmounts = es.newHarvestPermitSpeciesAmount(permit, deer, 1f);
        }

        public void setMooseSpeciesAmounts(final float applicationAmount,
                                           final float decisionAmount,
                                           final float permitAmount) {
            mooseApplicationAmounts.setSpecimenAmount(applicationAmount);
            mooseDecisionAmounts.setSpecimenAmount(decisionAmount);
            moosePermitAmounts.setSpecimenAmount(permitAmount);
        }

        public void setDeerSpeciesAmounts(final float applicationAmount,
                                          final float decisionAmount,
                                          final float permitAmount) {
            deerApplicationAmounts.setSpecimenAmount(applicationAmount);
            deerDecisionAmounts.setSpecimenAmount(decisionAmount);
            deerPermitAmounts.setSpecimenAmount(permitAmount);
        }

        public void addMooseHarvests(final List<GameAge> ages) {
            ages.forEach(age -> createMooselikeHarvest(moose, mooseHuntingDay, age));
        }

        public void addDeerHarvests(final List<GameAge> ages) {
            ages.forEach(age -> createMooselikeHarvest(deer, deerHuntingDay, age));
        }

        private void createMooselikeHarvest(final GameSpecies species,
                                           final GroupHuntingDay huntingDay,
                                           final GameAge age) {
            final Harvest harvest = es.newHarvest(species);
            harvest.setAmount(1);
            harvest.updateHuntingDayOfGroup(huntingDay, es.newPerson());
            final HarvestSpecimen specimen = es.newHarvestSpecimen(harvest);
            specimen.setAge(age);
            if (age == GameAge.YOUNG) {
                specimen.setAntlersGirth(null);
                specimen.setAntlersInnerWidth(null);
                specimen.setAntlersLength(null);
                specimen.setAntlersLost(null);
                specimen.setAntlerPointsLeft(null);
                specimen.setAntlerPointsRight(null);
                specimen.setAntlerShaftWidth(null);
                specimen.setAntlersType(null);
                specimen.setAntlersWidth(null);
            } else {
                specimen.setAlone(null);
            }
        }

        public void addMooseModeratorOverrideValues(final int numberOfAdultMales, final int numberOfAdultFemales,
                                                    final int numberOfYoungMales, final int numberOfYoungFemales,
                                                    final int numberOfNonEdibleAdults, final int numberOfNonEdibleYoungs ) {

            final BasicClubHuntingSummary summary = es.newBasicHuntingSummary(moosePermitAmounts, club, true);
            summary.doModeratorOverride(
                    moosePermitAmounts.getLastDate(),
                    new AreaSizeAndRemainingPopulation()
                            .withTotalHuntingArea(23456)
                            .withEffectiveHuntingArea(12345)
                            .withRemainingPopulationInTotalArea(234)
                            .withRemainingPopulationInEffectiveArea(123),
                    // NOTE: harvests must be bigger than non edibles.
                    HasHarvestCountsForPermit.of(numberOfAdultMales, numberOfAdultFemales,
                                                 numberOfYoungMales, numberOfYoungFemales,
                                                 numberOfNonEdibleAdults, numberOfNonEdibleYoungs));

        }
    }
}
