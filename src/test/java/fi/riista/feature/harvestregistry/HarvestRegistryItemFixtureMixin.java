package fi.riista.feature.harvestregistry;

import fi.riista.config.Constants;
import fi.riista.feature.common.fixture.FixtureMixin;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimen;
import fi.riista.feature.organization.RiistakeskuksenAlue;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;

import java.util.function.Consumer;

import static fi.riista.feature.gamediary.fixture.HarvestSpecimenType.ADULT_MALE;
import static fi.riista.util.DateUtil.today;

public interface HarvestRegistryItemFixtureMixin extends FixtureMixin {

    default void withItem(final GameSpecies species,
                          final Consumer<HarvestRegistryItemFixture> consumer) {
        consumer.accept(new HarvestRegistryItemFixture(getEntitySupplier(), species));
    }

    class HarvestRegistryItemFixture {

        public final RiistakeskuksenAlue rka;
        public final Riistanhoitoyhdistys rhy;
        public final Person shooter;
        public final Person author;
        public final Harvest harvest;
        public final HarvestRegistryItem item;
        public final HarvestSpecimen specimen;

        public HarvestRegistryItemFixture(final EntitySupplier es, final GameSpecies species) {
            rka = es.newRiistakeskuksenAlue();
            rhy = es.newRiistanhoitoyhdistys(rka);
            shooter = es.newPerson();
            author = es.newPerson();
            harvest = es.newHarvest(species, author, shooter);
            harvest.setPointOfTime(today().toDateTimeAtStartOfDay(Constants.DEFAULT_TIMEZONE));
            specimen = es.newHarvestSpecimen(harvest, ADULT_MALE);

            item = es.newHarvestRegistryItem(harvest, shooter, rka.getOfficialCode(), rhy.getOfficialCode());

        }

    }
}
