package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.fixture.CanPopulateHarvestSpecimen;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.util.NumberGenerator;

import javax.annotation.Nonnull;

import static java.util.Objects.requireNonNull;

/**
 * Populates fields to HarvestSpecimenBusinessFields instances (JPA entity or DTO) within test execution.
 */
public class HarvestSpecimenPopulator implements CanPopulateHarvestSpecimen {

    private final HarvestSpecimenOps specimenOps;
    private final NumberGenerator numberGenerator;

    public HarvestSpecimenPopulator(final int gameSpeciesCode,
                                    @Nonnull final HarvestSpecVersion specVersion,
                                    final int huntingYear,
                                    @Nonnull final NumberGenerator numberGenerator) {

        this.specimenOps = new HarvestSpecimenOps(gameSpeciesCode, specVersion, huntingYear);
        this.numberGenerator = requireNonNull(numberGenerator);
    }

    @Override
    public NumberGenerator getNumberGenerator() {
        return numberGenerator;
    }

    @Override
    public HarvestSpecimenOps getSpecimenOps() {
        return specimenOps;
    }
}
