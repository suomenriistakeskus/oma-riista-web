package fi.riista.feature.gamediary.harvest.specimen;

import com.google.common.base.Preconditions;
import fi.riista.feature.gamediary.GameAge;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.harvest.Harvest;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import fi.riista.util.NumberUtils;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static fi.riista.feature.gamediary.GameSpecies.isMooseOrDeerRequiringPermitForHunting;
import static java.util.stream.Collectors.toList;

/**
 * Encapsulates logic for species and specVersion dependent equality and
 * business field transferring between harvest specimen objects.
 */
public class HarvestSpecimenOps {

    private final int gameSpeciesCode;
    private final HarvestSpecVersion specVersion;

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
        return specVersion.isPresenceOfMooselikeFieldsLegitimate(gameSpeciesCode);
    }

    public boolean supportsExtendedMooseFields() {
        return specVersion.isPresenceOfMooseFieldsLegitimate(gameSpeciesCode);
    }

    public boolean supportsSolitaryMooseCalves() {
        return specVersion.isPresenceOfAloneLegitimate(gameSpeciesCode);
    }

    public boolean equalContent(@Nonnull final HarvestSpecimen entity, @Nonnull final HarvestSpecimenDTO dto) {
        Objects.requireNonNull(entity, "entity is null");
        Objects.requireNonNull(dto, "dto is null");

        if (entity.getGender() != dto.getGender() || entity.getAge() != dto.getAge()) {
            return false;
        }

        if (supportsExtendedMooselikeFields()) {
            return supportsExtendedMooseFields()
                    ? hasEqualMooseFields(entity, dto)
                    : entity.hasEqualMooselikeFields(dto);
        }

        final Double entityWeight = isMooseOrDeerRequiringPermitForHunting(gameSpeciesCode)
                ? entity.getWeightEstimated()
                : entity.getWeight();

        return NumberUtils.equal(entityWeight, dto.getWeight());
    }

    public boolean hasEqualMooseFields(@Nonnull final HasMooseFields first, @Nonnull final HasMooseFields second) {
        Objects.requireNonNull(first, "first is null");

        return first.hasEqualMooselikeFields(second) &&
                first.getFitnessClass() == second.getFitnessClass() &&
                first.getAntlersType() == second.getAntlersType() &&
                (!supportsSolitaryMooseCalves() || Objects.equals(first.getAlone(), second.getAlone()));
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

        entity.setAge(dto.getAge());
        entity.setGender(dto.getGender());

        if (supportsExtendedMooselikeFields()) {
            entity.setWeight(null);

            if (supportsExtendedMooseFields()) {
                copyMooseFields(dto, entity);
            } else {
                copyMooselikeFields(dto, entity);
                entity.clearMooseOnlyFields();
            }
        } else if (isMooseOrDeerRequiringPermitForHunting(gameSpeciesCode)) {
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

        dto.setAge(entity.getAge());
        dto.setGender(entity.getGender());

        if (supportsExtendedMooselikeFields()) {
            dto.setWeight(null);

            if (supportsExtendedMooseFields()) {
                copyMooseFields(entity, dto);
            } else {
                copyMooselikeFields(entity, dto);
                dto.clearMooseOnlyFields();
            }
        } else {
            // Weight translation done for old mobile clients.
            dto.setWeight(isMooseOrDeerRequiringPermitForHunting(gameSpeciesCode)
                    ? F.firstNonNull(entity.getWeightEstimated(), entity.getWeight())
                    : entity.getWeight());

            dto.clearMooseFields();
        }
    }

    private static void copyMooselikeFields(final HarvestSpecimenBusinessFields src,
                                            final HarvestSpecimenBusinessFields dst) {

        dst.setWeightEstimated(src.getWeightEstimated());
        dst.setWeightMeasured(src.getWeightMeasured());
        dst.setNotEdible(src.getNotEdible());
        dst.setAdditionalInfo(src.getAdditionalInfo());

        if (src.getAge() == GameAge.ADULT && src.getGender() == GameGender.MALE) {
            dst.setAntlersWidth(src.getAntlersWidth());
            dst.setAntlerPointsLeft(src.getAntlerPointsLeft());
            dst.setAntlerPointsRight(src.getAntlerPointsRight());
        } else {
            dst.setAntlersWidth(null);
            dst.setAntlerPointsLeft(null);
            dst.setAntlerPointsRight(null);
        }
    }

    private void copyMooseFields(final HarvestSpecimenBusinessFields src, final HarvestSpecimenBusinessFields dst) {
        copyMooselikeFields(src, dst);

        dst.setFitnessClass(src.getFitnessClass());

        final GameAge age = src.getAge();

        if (age == GameAge.ADULT && src.getGender() == GameGender.MALE) {
            dst.setAntlersType(src.getAntlersType());
        } else {
            dst.setAntlersType(null);

            if (age == GameAge.YOUNG) {
                if (specVersion.supportsSolitaryMooseCalves()) {
                    dst.setAlone(src.getAlone());
                }
            } else {
                dst.setAlone(null);
            }
        }
    }
}
