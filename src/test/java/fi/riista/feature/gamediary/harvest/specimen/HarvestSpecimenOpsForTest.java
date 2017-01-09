package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenDTO;
import fi.riista.feature.gamediary.fixture.CanPopulateHarvestSpecimen;
import fi.riista.feature.gamediary.harvest.specimen.HarvestSpecimenOps;
import fi.riista.util.NumberGenerator;

import javax.annotation.Nonnull;

import java.util.Objects;

public class HarvestSpecimenOpsForTest extends HarvestSpecimenOps implements CanPopulateHarvestSpecimen {

    private final NumberGenerator ng;

    public HarvestSpecimenOpsForTest(final int gameSpeciesCode,
                                     @Nonnull final HarvestSpecVersion specVersion,
                                     @Nonnull final NumberGenerator ng) {
        super(gameSpeciesCode, specVersion);
        this.ng = Objects.requireNonNull(ng);
    }

    @Override
    public NumberGenerator getNumberGenerator() {
        return ng;
    }

    @Override
    public HarvestSpecimenOps getSpecimenOps() {
        return this;
    }

    public HarvestSpecimenDTO newHarvestSpecimenDTO() {
        final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();
        mutateContent(dto);
        return dto;
    }

    public void mutateContent(@Nonnull final HarvestSpecimenDTO dto) {
        mutateContent(dto, !GameSpecies.isMoose(getGameSpeciesCode()));
    }

}
