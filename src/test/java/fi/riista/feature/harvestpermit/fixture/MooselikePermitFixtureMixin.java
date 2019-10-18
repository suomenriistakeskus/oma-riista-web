package fi.riista.feature.harvestpermit.fixture;

import fi.riista.feature.common.fixture.FixtureMixin;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.harvestpermit.HarvestPermitSpeciesAmount;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.PermitHolder;

import java.util.function.Consumer;

import static fi.riista.util.DateUtil.huntingYear;
import static java.util.Objects.requireNonNull;

public interface MooselikePermitFixtureMixin extends FixtureMixin {

    default void withMooselikePermitFixture(final Consumer<MooselikePermitFixture> consumer) {
        consumer.accept(new MooselikePermitFixture(getEntitySupplier()));
    }

    default void withMooselikePermitFixture(final int huntingYear, final Consumer<MooselikePermitFixture> consumer) {
        consumer.accept(new MooselikePermitFixture(getEntitySupplier(), huntingYear));
    }

    default void withMooselikePermitFixture(final GameSpecies species,
                                            final Consumer<MooselikePermitFixture> consumer) {

        consumer.accept(new MooselikePermitFixture(getEntitySupplier(), species));
    }

    default void withMooselikePermitFixture(final GameSpecies species,
                                            final int huntingYear,
                                            final Consumer<MooselikePermitFixture> consumer) {

        consumer.accept(new MooselikePermitFixture(getEntitySupplier(), species, huntingYear));
    }

    default void withMooselikePermitFixture(final Riistanhoitoyhdistys rhy,
                                            final GameSpecies species,
                                            final Consumer<MooselikePermitFixture> consumer) {

        consumer.accept(new MooselikePermitFixture(getEntitySupplier(), rhy, species));
    }

    default void withMooselikePermitFixture(final Riistanhoitoyhdistys rhy,
                                            final GameSpecies species,
                                            final int huntingYear,
                                            final Consumer<MooselikePermitFixture> consumer) {

        consumer.accept(new MooselikePermitFixture(getEntitySupplier(), rhy, species, huntingYear));
    }

    default void withMooselikePermitFixture(final HarvestPermitSpeciesAmount speciesAmount,
                                            final Consumer<MooselikePermitFixture> consumer) {

        consumer.accept(new MooselikePermitFixture(speciesAmount));
    }

    class MooselikePermitFixture {

        public final Riistanhoitoyhdistys rhy;
        public final GameSpecies species;

        public final int huntingYear;
        public final HarvestPermit permit;
        public final HarvestPermitSpeciesAmount speciesAmount;

        public final PermitHolder permitHolder;
        public final Person originalContactPerson;

        public MooselikePermitFixture(final EntitySupplier es) {
            this(es, huntingYear());
        }

        public MooselikePermitFixture(final EntitySupplier es, final int huntingYear) {
            this(es, es.newGameSpeciesMoose(), huntingYear);
        }

        public MooselikePermitFixture(final EntitySupplier es, final GameSpecies species) {
            this(es, es.newRiistanhoitoyhdistys(), species);
        }

        public MooselikePermitFixture(final EntitySupplier es, final GameSpecies species, final int huntingYear) {
            this(es, es.newRiistanhoitoyhdistys(), species, huntingYear);
        }

        public MooselikePermitFixture(final EntitySupplier es,
                                      final Riistanhoitoyhdistys rhy,
                                      final GameSpecies species) {

            this(es, rhy, species, huntingYear());
        }

        public MooselikePermitFixture(final EntitySupplier es,
                                      final Riistanhoitoyhdistys rhy,
                                      final GameSpecies species,
                                      final int huntingYear) {

            this(es.newHarvestPermitSpeciesAmount(es.newMooselikePermit(rhy, huntingYear), species));
        }

        public MooselikePermitFixture(final HarvestPermitSpeciesAmount speciesAmount) {
            this.speciesAmount = requireNonNull(speciesAmount);
            this.huntingYear = speciesAmount.resolveHuntingYear();

            species = requireNonNull(speciesAmount.getGameSpecies());
            permit = requireNonNull(speciesAmount.getHarvestPermit());
            rhy = requireNonNull(permit.getRhy());

            permitHolder = PermitHolder.create("club", "1234", PermitHolder.PermitHolderType.OTHER);
            permit.setPermitHolder(permitHolder);

            originalContactPerson = requireNonNull(permit.getOriginalContactPerson());
            requireNonNull(originalContactPerson.getMrAddress());
        }
    }
}
