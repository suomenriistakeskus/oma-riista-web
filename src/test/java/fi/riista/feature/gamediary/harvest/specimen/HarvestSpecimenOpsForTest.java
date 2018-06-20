package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.fixture.CanPopulateHarvestSpecimen;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.util.NumberGenerator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

import static fi.riista.feature.gamediary.GameSpecies.isMooseOrDeerRequiringPermitForHunting;

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

    public HarvestSpecimenDTO createDTO() {
        return createDTO(!isMooseOrDeerRequiringPermitForHunting(getGameSpeciesCode()));
    }

    public HarvestSpecimenDTO createDTO(final boolean allowUnknownAgeAndGender) {
        final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();
        mutateContent(dto, allowUnknownAgeAndGender);
        return dto;
    }

    public HarvestSpecimenDTO createDTO(@Nullable final GameAge age, @Nullable final GameGender gender) {
        final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();
        mutateContent(dto, age, gender);
        return dto;
    }

    public void mutateContent(@Nonnull final HarvestSpecimenBusinessFields obj) {
        mutateContent(obj, !isMooseOrDeerRequiringPermitForHunting(getGameSpeciesCode()));
    }
}
