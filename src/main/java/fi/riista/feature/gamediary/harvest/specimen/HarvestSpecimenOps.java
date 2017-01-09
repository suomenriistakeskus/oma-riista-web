package fi.riista.feature.gamediary.harvest.specimen;

import com.google.common.base.Preconditions;

import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import fi.riista.util.NumberUtils;
import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public class HarvestSpecimenOps {

    private final int gameSpeciesCode;
    private final HarvestSpecVersion specVersion;

    public HarvestSpecimenOps(@Nonnull final GameSpecies species, @Nonnull final HarvestSpecVersion specVersion) {
        this(Objects.requireNonNull(species, "species is null").getOfficialCode(), specVersion);
    }

    public HarvestSpecimenOps(final int gameSpeciesCode, @Nonnull final HarvestSpecVersion specVersion) {
        Preconditions.checkState(gameSpeciesCode > 0, "gameSpeciesCode not set");

        this.gameSpeciesCode = gameSpeciesCode;
        this.specVersion = Objects.requireNonNull(specVersion);
    }

    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public HarvestSpecVersion getVersion() {
        return specVersion;
    }

    public int getMinAmount() {
        return Harvest.MIN_AMOUNT;
    }

    public int getMaxAmount() {
        return Harvest.MAX_AMOUNT;
    }

    public boolean supportsExtendedMooselikeFields() {
        return specVersion.isExtendedMooselikeFieldsSupported(gameSpeciesCode);
    }

    public boolean supportsExtendedMooseFields() {
        return specVersion.isExtendedMooseFieldsSupported(gameSpeciesCode);
    }

    public boolean equalContent(@Nonnull final HarvestSpecimen entity, @Nonnull final HarvestSpecimenDTO dto) {
        Objects.requireNonNull(entity, "entity is null");
        Objects.requireNonNull(dto, "dto is null");

        if (entity.getGender() != dto.getGender() || entity.getAge() != dto.getAge()) {
            return false;
        }

        if (supportsExtendedMooselikeFields()) {
            return supportsExtendedMooseFields()
                    ? entity.hasEqualMooseFields(dto)
                    : entity.hasEqualMooselikeFields(dto);
        }

        final Double entityWeight = GameSpecies.isMooseOrDeerRequiringPermitForHunting(gameSpeciesCode)
                ? entity.getWeightEstimated()
                : entity.getWeight();

        return NumberUtils.equal(entityWeight, dto.getWeight());
    }

    public HarvestSpecimenDTO transform(@Nonnull final HarvestSpecimen entity) {
        Objects.requireNonNull(entity);
        final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();
        DtoUtil.copyBaseFields(entity, dto);
        copyContentToDTO(entity, dto);
        return dto;
    }

    public List<HarvestSpecimenDTO> transformList(@Nonnull final Collection<? extends HarvestSpecimen> specimens) {
        Objects.requireNonNull(specimens);
        return specimens.stream().map(this::transform).collect(toList());
    }

    public void copyContentToEntity(@Nonnull final HarvestSpecimenDTO dto, @Nonnull final HarvestSpecimen entity) {
        Objects.requireNonNull(dto, "dto is null");
        Objects.requireNonNull(entity, "entity is null");

        entity.setGender(dto.getGender());
        entity.setAge(dto.getAge());

        if (supportsExtendedMooselikeFields()) {
            dto.copyMooseFieldsTo(entity);
            entity.setWeight(null);

            if (!supportsExtendedMooseFields()) {
                entity.clearMooseOnlyFields();
            }
        } else if (GameSpecies.isMooseOrDeerRequiringPermitForHunting(gameSpeciesCode)) {
            // Customization for old mobile clients.
            entity.setWeightEstimated(dto.getWeight());
            entity.setWeight(null);
        } else {
            entity.setWeight(dto.getWeight());
            entity.clearMooseFields();
        }
    }

    public void copyContentToDTO(@Nonnull final HarvestSpecimen entity, @Nonnull final HarvestSpecimenDTO dto) {
        Objects.requireNonNull(entity, "entity is null");
        Objects.requireNonNull(dto, "dto is null");

        dto.setGender(entity.getGender());
        dto.setAge(entity.getAge());

        if (supportsExtendedMooselikeFields()) {
            entity.copyMooseFieldsTo(dto);
            dto.setWeight(null);

            if (!supportsExtendedMooseFields()) {
                dto.clearMooseOnlyFields();
            }
        } else {
            // Weight translation done for old mobile clients.
            dto.setWeight(GameSpecies.isMooseOrDeerRequiringPermitForHunting(gameSpeciesCode)
                    ? F.firstNonNull(entity.getWeightEstimated(), entity.getWeight()) 
                    : entity.getWeight());

            dto.clearMooseFields();
        }
    }

}
