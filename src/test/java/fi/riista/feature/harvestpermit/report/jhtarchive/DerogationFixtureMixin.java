package fi.riista.feature.harvestpermit.report.jhtarchive;

import fi.riista.feature.common.fixture.FixtureMixin;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitCategory;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.PermitTypeCode;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.application.HarvestPermitApplicationSpeciesAmount;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.species.PermitDecisionSpeciesAmount;

import java.util.function.Consumer;

import static java.util.Collections.singletonList;

public interface DerogationFixtureMixin extends FixtureMixin {

    default void withPermitCategory(final HarvestPermitCategory permitCategory,
                                    final Consumer<DerogationFixture> consumer) {
        withPermitCategory(getEntitySupplier().newGameSpecies(), permitCategory, consumer);
    }

    default void withPermitCategory(final GameSpecies species,
                                    final HarvestPermitCategory permitCategory,
                                    final Consumer<DerogationFixture> consumer) {
        final EntitySupplier es = getEntitySupplier();
        final RiistakeskuksenAlue rka = es.newRiistakeskuksenAlue();
        final Riistanhoitoyhdistys rhy = es.newRiistanhoitoyhdistys(rka);
        final HuntingClub club = es.newHuntingClub(rhy);
        consumer.accept(new DerogationFixture(es, rka, rhy, club, species, permitCategory));
    }


    class DerogationFixture {
        private final EntitySupplier es;

        public final RiistakeskuksenAlue rka;
        public final Riistanhoitoyhdistys rhy;
        public final HuntingClub club;
        public final HarvestPermitApplication application;
        public final HarvestPermitApplicationSpeciesAmount applicationAmount;
        public final PermitDecisionSpeciesAmount decisionAmount;
        public final HarvestPermitSpeciesAmount permitAmount;

        public final PermitDecision decision;
        public final HarvestPermit permit;
        public final GameSpecies species;

        public DerogationFixture(final EntitySupplier es,
                                 final RiistakeskuksenAlue rka,
                                 final Riistanhoitoyhdistys rhy,
                                 final HuntingClub club,
                                 final GameSpecies species,
                                 final HarvestPermitCategory permitCategory) {
            this.es = es;
            this.rka = rka;
            this.rhy = rhy;
            this.club = club;
            this.species = species;

            application = es.newHarvestPermitApplication(rhy, es.newHarvestPermitArea(), permitCategory);
            applicationAmount = es.newHarvestPermitApplicationSpeciesAmount(application, species, 0f, 1);
            application.setSpeciesAmounts(singletonList(applicationAmount)); // For BIRD, must be set before decision
            decision = es.newPermitDecision(application);
            decisionAmount = es.newPermitDecisionSpeciesAmount(decision, species, 1f);
            permit = es.newHarvestPermit(rhy);
            permit.setPermitDecision(decision);
            permit.setPermitTypeCode(PermitTypeCode.getPermitTypeCode(permitCategory, 1));
            permit.setPermitAreaSize(131313); // For MOOSELIKE
            permitAmount = es.newHarvestPermitSpeciesAmount(permit, species, 1f);
        }

    }
}
