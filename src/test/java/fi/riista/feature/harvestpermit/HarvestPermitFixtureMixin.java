package fi.riista.feature.harvestpermit;

import fi.riista.feature.common.fixture.FixtureMixin;
import fi.riista.feature.common.support.EntitySupplier;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.rhy.Riistanhoitoyhdistys;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import fi.riista.feature.permit.decision.PermitDecision;
import fi.riista.feature.permit.decision.revision.PermitDecisionRevision;
import fi.riista.feature.storage.metadata.PersistentFileMetadata;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.util.function.Consumer;

import static fi.riista.feature.permit.PermitTypeCode.getPermitTypeCode;
import static java.util.Objects.requireNonNull;

public interface HarvestPermitFixtureMixin extends FixtureMixin {

    TemporaryFolder getTemporaryFolder();

    default void withPermit(final GameSpecies species,
                            final int year,
                            final Consumer<HarvestPermitFixture> consumer) {
        consumer.accept(
                new HarvestPermitFixture(getEntitySupplier(), getTemporaryFolder(), year, species));
    }

    default void withPermit(final GameSpecies species, final Riistanhoitoyhdistys rhy, final int year,
                            final Consumer<HarvestPermitFixture> consumer) {
        consumer.accept(
                new HarvestPermitFixture(getEntitySupplier(), getTemporaryFolder(), rhy, year, species));
    }

    class HarvestPermitFixture {

        public final Riistanhoitoyhdistys rhy;
        public final GameSpecies species;

        public final int huntingYear;
        public final HarvestPermit permit;
        public final HarvestPermitSpeciesAmount speciesAmount;
        public final Person originalContactPerson;

        public final PermitDecision decision;

        public HarvestPermitFixture(final EntitySupplier es, final TemporaryFolder folder, final int year, final GameSpecies species) {
            this(es, folder, es.newRiistanhoitoyhdistys(), year, species);
        }

        public HarvestPermitFixture(final EntitySupplier es, final TemporaryFolder folder,
                                    final Riistanhoitoyhdistys rhy, final int year, final GameSpecies species) {

            this(es, folder, rhy, species, year, HarvestPermitCategory.MAMMAL);
        }

        public HarvestPermitFixture(final EntitySupplier es, final TemporaryFolder folder,
                                    final Riistanhoitoyhdistys rhy, final GameSpecies species,
                                    final int year, final HarvestPermitCategory category) {
            try {
                final HarvestPermitApplication application =
                        es.newHarvestPermitApplication(rhy, null, species, category);
                this.decision = es.newPermitDecision(application);
                this.decision.setDecisionYear(year);

                this.permit =
                        es.newHarvestPermit(
                                rhy, decision.createPermitNumber(), getPermitTypeCode(category, 1), decision);
                this.speciesAmount = es.newHarvestPermitSpeciesAmount(permit, species);

                this.huntingYear = speciesAmount.resolveHuntingYear();
                this.species = requireNonNull(species);
                this.rhy = requireNonNull(rhy);

                this.originalContactPerson = requireNonNull(permit.getOriginalContactPerson());
                requireNonNull(originalContactPerson.getMrAddress());

                final PermitDecisionRevision revision = es.newPermitDecisionRevision(decision);
                es.newPermitDecisionSpeciesAmount(decision, species, speciesAmount.getSpecimenAmount());

                final PersistentFileMetadata metadata = es.newPersistentFileMetadata();
                metadata.setResourceUrl(folder.newFile("meta-" + es.nextPositiveInt()).toURI().toURL());
                revision.setPublicPdfMetadata(metadata);

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
