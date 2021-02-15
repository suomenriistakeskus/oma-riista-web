package fi.riista.feature.gamediary.harvest.specimen;

import fi.riista.feature.gamediary.HasGameSpeciesCode;
import fi.riista.feature.gamediary.harvest.HarvestSpecVersion;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import fi.riista.util.NumberUtils;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_FALLOW_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_MOOSE;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_ROE_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WHITE_TAILED_DEER;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_BOAR;
import static fi.riista.feature.gamediary.GameSpecies.OFFICIAL_CODE_WILD_FOREST_REINDEER;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * Encapsulates logic for species and specVersion dependent equality and
 * business field transferring between harvest specimen objects.
 */
public class HarvestSpecimenOps implements HasGameSpeciesCode {

    private final int gameSpeciesCode;
    private final HarvestSpecVersion specVersion;
    private final int huntingYear;

    public HarvestSpecimenOps(final int gameSpeciesCode,
                              @Nonnull final HarvestSpecVersion specVersion,
                              final int huntingYear) {

        checkArgument(gameSpeciesCode > 0, "gameSpeciesCode not set");

        this.gameSpeciesCode = gameSpeciesCode;
        this.specVersion = requireNonNull(specVersion);
        this.huntingYear = huntingYear;
    }

    @Override
    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public HarvestSpecVersion getVersion() {
        return specVersion;
    }

    public int getHuntingYear() {
        return huntingYear;
    }

    public boolean isPresenceOfDeerExtensionFieldsLegitimate() {
        return isDeerRequiringPermitForHunting() && specVersion.supportsExtendedFieldsForDeers();
    }

    public boolean isPresenceOfAloneLegitimate() {
        return isMoose() && specVersion.supportsSolitaryMooseCalves();
    }

    public boolean isPresenceOfAntlerFields2020Legitimate() {
        return huntingYear >= 2020 && isMooselike() && specVersion.supportsAntlerFields2020();
    }

    public boolean equalContent(@Nonnull final HarvestSpecimen entity, @Nonnull final HarvestSpecimenDTO dto) {
        requireNonNull(entity, "entity is null");
        requireNonNull(dto, "dto is null");

        switch (gameSpeciesCode) {
            case OFFICIAL_CODE_MOOSE:
                return hasEqualFieldsForMoose(entity, dto);
            case OFFICIAL_CODE_ROE_DEER:
                return hasEqualFieldsForRoeDeer(entity, dto);
            case OFFICIAL_CODE_WHITE_TAILED_DEER:
                return hasEqualFieldsForWhiteTailedDeer(entity, dto);
            case OFFICIAL_CODE_FALLOW_DEER:
            case OFFICIAL_CODE_WILD_FOREST_REINDEER:
                return hasEqualFieldsForOtherDeer(entity, dto);
            case OFFICIAL_CODE_WILD_BOAR:
                return hasEqualFieldsForWildBoar(entity, dto);
            default: // other species
                return hasEqualFieldsForOtherSpecies(entity, dto);
        }
    }

    private boolean hasEqualFieldsForMoose(@Nonnull final HarvestSpecimenBusinessFields first,
                                           @Nonnull final HarvestSpecimenBusinessFields second) {

        if (!specVersion.supportsSolitaryMooseCalves()) {
            return hasEqualFieldsForMooseOnSpecVersion3(first, second);
        }

        if (!specVersion.supportsAntlerFields2020() || huntingYear < 2020) {
            return hasEqualFieldsForMooseOnSpecVersion5(first, second);
        }

        return hasEqualFieldsForMooseOnSpecVersion8(first, second);
    }

    private boolean hasEqualFieldsForRoeDeer(@Nonnull final HarvestSpecimen entity,
                                             @Nonnull final HarvestSpecimenDTO dto) {

        if (!specVersion.supportsAntlerFields2020() || huntingYear < 2020) {
            return hasEqualFieldsForRoeDeerOrWildBoardOnSpecVersion7(entity, dto);
        }

        return hasEqualFieldsForRoeDeerOnSpecVersion8(entity, dto);
    }

    private boolean hasEqualFieldsForWhiteTailedDeer(@Nonnull final HarvestSpecimen entity,
                                                     @Nonnull final HarvestSpecimenDTO dto) {

        if (!specVersion.supportsExtendedFieldsForDeers()) {
            return hasEqualFieldsForPermitBasedDeerOnSpecVersion3(entity, dto);
        }

        if (!specVersion.supportsAntlerFields2020() || huntingYear < 2020) {
            return hasEqualFieldsForPermitBasedDeerOnSpecVersion4(entity, dto);
        }

        return hasEqualFieldsForWhiteTailedDeerOnSpecVersion8(entity, dto);
    }

    // Fallow deer and wild forest reindeer
    private boolean hasEqualFieldsForOtherDeer(@Nonnull final HarvestSpecimen entity,
                                               @Nonnull final HarvestSpecimenDTO dto) {

        if (!specVersion.supportsExtendedFieldsForDeers()) {
            return hasEqualFieldsForPermitBasedDeerOnSpecVersion3(entity, dto);
        }

        if (!specVersion.supportsAntlerFields2020() || huntingYear < 2020) {
            return hasEqualFieldsForPermitBasedDeerOnSpecVersion4(entity, dto);
        }

        return hasEqualFieldsForOtherDeerOnSpecVersion8(entity, dto);
    }

    private boolean hasEqualFieldsForWildBoar(@Nonnull final HarvestSpecimen entity,
                                              @Nonnull final HarvestSpecimenDTO dto) {

        if (!specVersion.supportsExtendedWeightFieldsForRoeDeerAndWildBoar() || huntingYear < 2020) {
            return hasEqualFieldsForRoeDeerOrWildBoardOnSpecVersion7(entity, dto);
        }

        return hasEqualFieldsForWildBoarOnSpecVersion8(entity, dto);
    }

    private static boolean hasEqualFieldsForOtherSpecies(@Nonnull final HarvestSpecimenBusinessFields first,
                                                         @Nonnull final HarvestSpecimenBusinessFields second) {

        return hasEqualAgeAndGender(first, second) &&
                NumberUtils.equal(first.getWeight(), second.getWeight());
    }

    private static boolean hasEqualAgeAndGender(@Nonnull final HarvestSpecimenBusinessFields first,
                                                @Nonnull final HarvestSpecimenBusinessFields second) {

        return first.getAge() == second.getAge() && first.getGender() == second.getGender();
    }

    private static boolean hasEqualExtendedWeightFields(@Nonnull final HarvestSpecimenBusinessFields first,
                                                        @Nonnull final HarvestSpecimenBusinessFields second) {

        return NumberUtils.equal(first.getWeightEstimated(), second.getWeightEstimated()) &&
                NumberUtils.equal(first.getWeightMeasured(), second.getWeightMeasured());
    }

    // Moose, fallow deer, white-tailed deer and wild forest reindeer
    private static boolean hasEqualFieldsForPermitBasedMooselike(@Nonnull final HarvestSpecimenBusinessFields first,
                                                                 @Nonnull final HarvestSpecimenBusinessFields second) {
        return hasEqualAgeAndGender(first, second) &&
                hasEqualExtendedWeightFields(first, second) &&

                Objects.equals(first.getNotEdible(), second.getNotEdible()) &&
                Objects.equals(first.getAdditionalInfo(), second.getAdditionalInfo());
    }

    private static boolean hasEqualFieldsForMooseOnSpecVersion3(@Nonnull final HarvestSpecimenBusinessFields first,
                                                                @Nonnull final HarvestSpecimenBusinessFields second) {

        return hasEqualFieldsForPermitBasedMooselike(first, second) &&
                Objects.equals(first.getFitnessClass(), second.getFitnessClass()) &&
                Objects.equals(first.getAntlersType(), second.getAntlersType()) &&
                Objects.equals(first.getAntlersWidth(), second.getAntlersWidth()) &&
                Objects.equals(first.getAntlerPointsLeft(), second.getAntlerPointsLeft()) &&
                Objects.equals(first.getAntlerPointsRight(), second.getAntlerPointsRight());
    }

    private static boolean hasEqualFieldsForMooseOnSpecVersion5(@Nonnull final HarvestSpecimenBusinessFields first,
                                                                @Nonnull final HarvestSpecimenBusinessFields second) {

        return hasEqualFieldsForMooseOnSpecVersion3(first, second) &&
                Objects.equals(first.getAlone(), second.getAlone());
    }

    private static boolean hasEqualFieldsForMooseOnSpecVersion8(@Nonnull final HarvestSpecimenBusinessFields first,
                                                                @Nonnull final HarvestSpecimenBusinessFields second) {

        return hasEqualFieldsForMooseOnSpecVersion5(first, second) &&
                Objects.equals(first.getAntlersLost(), second.getAntlersLost()) &&
                Objects.equals(first.getAntlersGirth(), second.getAntlersGirth());
    }

    // fallow deer, white-tailed deer, wild forest reindeer
    private static boolean hasEqualFieldsForPermitBasedDeerOnSpecVersion3(@Nonnull final HarvestSpecimen entity,
                                                                          @Nonnull final HarvestSpecimenDTO dto) {
        return hasEqualAgeAndGender(entity, dto) &&
                NumberUtils.equal(F.firstNonNull(entity.getWeightEstimated(), entity.getWeight()), dto.getWeight());
    }

    private static boolean hasEqualFieldsForRoeDeerOrWildBoardOnSpecVersion7(@Nonnull final HarvestSpecimen entity,
                                                                             @Nonnull final HarvestSpecimenDTO dto) {
        return hasEqualFieldsForPermitBasedDeerOnSpecVersion3(entity, dto);
    }

    // Fallow deer, white-tailed deer and wild forest reindeer
    private static boolean hasEqualFieldsForPermitBasedDeerOnSpecVersion4(@Nonnull final HarvestSpecimenBusinessFields first,
                                                                          @Nonnull final HarvestSpecimenBusinessFields second) {

        return hasEqualFieldsForPermitBasedMooselike(first, second) &&
                Objects.equals(first.getAntlersWidth(), second.getAntlersWidth()) &&
                Objects.equals(first.getAntlerPointsLeft(), second.getAntlerPointsLeft()) &&
                Objects.equals(first.getAntlerPointsRight(), second.getAntlerPointsRight());
    }

    private static boolean hasEqualFieldsForRoeDeerOnSpecVersion8(@Nonnull final HarvestSpecimenBusinessFields first,
                                                                  @Nonnull final HarvestSpecimenBusinessFields second) {

        return hasEqualAgeAndGender(first, second) &&
                hasEqualExtendedWeightFields(first, second) &&
                Objects.equals(first.getAntlersLost(), second.getAntlersLost()) &&
                Objects.equals(first.getAntlerPointsLeft(), second.getAntlerPointsLeft()) &&
                Objects.equals(first.getAntlerPointsRight(), second.getAntlerPointsRight()) &&
                Objects.equals(first.getAntlersLength(), second.getAntlersLength()) &&
                Objects.equals(first.getAntlerShaftWidth(), second.getAntlerShaftWidth());
    }

    private static boolean hasEqualFieldsForWhiteTailedDeerOnSpecVersion8(@Nonnull final HarvestSpecimenBusinessFields first,
                                                                          @Nonnull final HarvestSpecimenBusinessFields second) {

        return hasEqualFieldsForPermitBasedMooselike(first, second) &&
                Objects.equals(first.getAntlersLost(), second.getAntlersLost()) &&
                Objects.equals(first.getAntlerPointsLeft(), second.getAntlerPointsLeft()) &&
                Objects.equals(first.getAntlerPointsRight(), second.getAntlerPointsRight()) &&
                Objects.equals(first.getAntlersGirth(), second.getAntlersGirth()) &&
                Objects.equals(first.getAntlersLength(), second.getAntlersLength()) &&
                Objects.equals(first.getAntlersInnerWidth(), second.getAntlersInnerWidth());
    }

    private static boolean hasEqualFieldsForOtherDeerOnSpecVersion8(@Nonnull final HarvestSpecimenBusinessFields first,
                                                                    @Nonnull final HarvestSpecimenBusinessFields second) {

        return hasEqualFieldsForPermitBasedMooselike(first, second) &&
                Objects.equals(first.getAntlersLost(), second.getAntlersLost()) &&
                Objects.equals(first.getAntlersWidth(), second.getAntlersWidth()) &&
                Objects.equals(first.getAntlerPointsLeft(), second.getAntlerPointsLeft()) &&
                Objects.equals(first.getAntlerPointsRight(), second.getAntlerPointsRight());
    }

    private static boolean hasEqualFieldsForWildBoarOnSpecVersion8(@Nonnull final HarvestSpecimenBusinessFields first,
                                                                   @Nonnull final HarvestSpecimenBusinessFields second) {

        return hasEqualAgeAndGender(first, second) && hasEqualExtendedWeightFields(first, second);
    }

    public HarvestSpecimenDTO transform(@Nonnull final HarvestSpecimen entity) {
        requireNonNull(entity);

        final HarvestSpecimenDTO dto = new HarvestSpecimenDTO();
        DtoUtil.copyBaseFields(entity, dto);
        copyContentToDTO(entity, dto);

        return dto;
    }

    public List<HarvestSpecimenDTO> transformList(@Nonnull final Collection<? extends HarvestSpecimen> specimens) {
        requireNonNull(specimens);
        return specimens.stream().map(this::transform).collect(toList());
    }

    public void copyContentToEntity(@Nonnull final HarvestSpecimenDTO dto, @Nonnull final HarvestSpecimen entity) {
        requireNonNull(dto, "dto is null");
        requireNonNull(entity, "entity is null");

        switch (gameSpeciesCode) {
            case OFFICIAL_CODE_MOOSE:
                copyFieldsOfMooseToEntity(dto, entity);
                break;
            case OFFICIAL_CODE_ROE_DEER:
                copyFieldsOfRoeDeerToEntity(dto, entity);
                break;
            case OFFICIAL_CODE_WHITE_TAILED_DEER:
                copyFieldsOfWhiteTailedDeerToEntity(dto, entity);
                break;
            case OFFICIAL_CODE_FALLOW_DEER:
            case OFFICIAL_CODE_WILD_FOREST_REINDEER:
                copyFieldsOfOtherDeerToEntity(dto, entity);
                break;
            case OFFICIAL_CODE_WILD_BOAR:
                copyFieldsOfWildBoarToEntity(dto, entity);
                break;
            default: // other species
                copyFieldsOfOtherSpecies(dto, entity);
                break;
        }
    }

    private void copyFieldsOfOtherSpecies(@Nonnull final HarvestSpecimenBusinessFields src,
                                          @Nonnull final HarvestSpecimenBusinessFields dst) {

        dst.setAge(src.getAge());
        dst.setGender(src.getGender());
        dst.setWeight(src.getWeight());

        dst.clearExtensionFields();
    }

    private void copyFieldsOfMooseToEntity(@Nonnull final HarvestSpecimenDTO dto, @Nonnull final HarvestSpecimen entity) {
        entity.setAge(dto.getAge());
        entity.setGender(dto.getGender());
        entity.setWeight(null);

        copyExtensionFieldsOfMoose(dto, entity, true);
    }


    private void copyCommonFieldsOfPermitBasedDeerToEntity(@Nonnull final HarvestSpecimenDTO dto,
                                                           @Nonnull final HarvestSpecimen entity) {
        entity.setAge(dto.getAge());
        entity.setGender(dto.getGender());
        entity.setWeight(null);

        if (!specVersion.supportsExtendedFieldsForDeers()) {
            // Done for old mobile clients.
            entity.setWeightEstimated(dto.getWeight());
        }
    }

    private void copyCommonFieldsOfRoeDeerAndWildBoarToEntity(@Nonnull final HarvestSpecimenDTO dto,
                                                              @Nonnull final HarvestSpecimen entity) {
        entity.setAge(dto.getAge());
        entity.setGender(dto.getGender());

        if (huntingYear < 2020) {
            entity.setWeight(dto.getWeight());
        } else {
            entity.setWeight(null);

            if (!specVersion.supportsExtendedWeightFieldsForRoeDeerAndWildBoar()) {
                entity.setWeightEstimated(dto.getWeight());
            }
        }
    }

    private void copyFieldsOfRoeDeerToEntity(@Nonnull final HarvestSpecimenDTO dto,
                                             @Nonnull final HarvestSpecimen entity) {

        copyCommonFieldsOfRoeDeerAndWildBoarToEntity(dto, entity);
        copyExtensionFieldsOfRoeDeer(dto, entity, true);
    }

    private void copyFieldsOfWhiteTailedDeerToEntity(@Nonnull final HarvestSpecimenDTO dto,
                                                     @Nonnull final HarvestSpecimen entity) {

        copyCommonFieldsOfPermitBasedDeerToEntity(dto, entity);
        copyExtensionFieldsOfWhiteTailedDeer(dto, entity, true);
    }

    // Fallow deer and wild forest reindeer
    private void copyFieldsOfOtherDeerToEntity(@Nonnull final HarvestSpecimenDTO dto,
                                               @Nonnull final HarvestSpecimen entity) {

        copyCommonFieldsOfPermitBasedDeerToEntity(dto, entity);
        copyExtensionFieldsOfOtherDeer(dto, entity, true);
    }

    private void copyFieldsOfWildBoarToEntity(@Nonnull final HarvestSpecimenDTO dto,
                                              @Nonnull final HarvestSpecimen entity) {

        copyCommonFieldsOfRoeDeerAndWildBoarToEntity(dto, entity);
        copyExtensionFieldsOfWildBoar(dto, entity);
    }

    public void copyContentToDTO(@Nonnull final HarvestSpecimen entity, @Nonnull final HarvestSpecimenDTO dto) {
        requireNonNull(entity, "entity is null");
        requireNonNull(dto, "dto is null");

        switch (gameSpeciesCode) {
            case OFFICIAL_CODE_MOOSE:
                copyFieldsOfMooseToDTO(entity, dto);
                break;
            case OFFICIAL_CODE_ROE_DEER:
                copyFieldsOfRoeDeerToDTO(entity, dto);
                break;
            case OFFICIAL_CODE_WHITE_TAILED_DEER:
                copyFieldsOfWhiteTailedDeerToDTO(entity, dto);
                break;
            case OFFICIAL_CODE_FALLOW_DEER:
            case OFFICIAL_CODE_WILD_FOREST_REINDEER:
                copyFieldsOfOtherDeerToDTO(entity, dto);
                break;
            case OFFICIAL_CODE_WILD_BOAR:
                copyFieldsOfWildBoarToDTO(entity, dto);
                break;
            default: // other species
                copyFieldsOfOtherSpecies(entity, dto);
                break;
        }
    }

    private void copyFieldsOfMooseToDTO(@Nonnull final HarvestSpecimen entity, @Nonnull final HarvestSpecimenDTO dto) {
        dto.setAge(entity.getAge());
        dto.setGender(entity.getGender());
        dto.setWeight(null);

        copyExtensionFieldsOfMoose(entity, dto, false);
    }

    private void copyCommonFieldsOfPermitBasedDeerToDTO(@Nonnull final HarvestSpecimen entity,
                                                        @Nonnull final HarvestSpecimenDTO dto) {
        dto.setAge(entity.getAge());
        dto.setGender(entity.getGender());

        // Weight translation done for old mobile clients.
        final Double weight = !specVersion.supportsExtendedFieldsForDeers()
                ? F.firstNonNull(entity.getWeightEstimated(), entity.getWeight())
                : null;

        dto.setWeight(weight);
    }

    private void copyCommonFieldsOfRoeDeerAndWildBoarToDTO(@Nonnull final HarvestSpecimen entity,
                                                           @Nonnull final HarvestSpecimenDTO dto) {
        dto.setAge(entity.getAge());
        dto.setGender(entity.getGender());

        final Double weight;

        if (huntingYear < 2020) {
            weight = entity.getWeight();
        } else {
            // Weight translation done for old mobile clients.
            weight = !specVersion.supportsExtendedWeightFieldsForRoeDeerAndWildBoar()
                    ? F.firstNonNull(entity.getWeightEstimated(), entity.getWeight())
                    : null;
        }

        dto.setWeight(weight);
    }

    private void copyFieldsOfRoeDeerToDTO(@Nonnull final HarvestSpecimen entity, @Nonnull final HarvestSpecimenDTO dto) {
        copyCommonFieldsOfRoeDeerAndWildBoarToDTO(entity, dto);
        copyExtensionFieldsOfRoeDeer(entity, dto, false);
    }

    private void copyFieldsOfWhiteTailedDeerToDTO(@Nonnull final HarvestSpecimen entity,
                                                  @Nonnull final HarvestSpecimenDTO dto) {

        copyCommonFieldsOfPermitBasedDeerToDTO(entity, dto);
        copyExtensionFieldsOfWhiteTailedDeer(entity, dto, false);
    }

    // Fallow deer and wild forest reindeer
    private void copyFieldsOfOtherDeerToDTO(@Nonnull final HarvestSpecimen entity, @Nonnull final HarvestSpecimenDTO dto) {
        copyCommonFieldsOfPermitBasedDeerToDTO(entity, dto);
        copyExtensionFieldsOfOtherDeer(entity, dto, false);
    }

    private void copyFieldsOfWildBoarToDTO(@Nonnull final HarvestSpecimen entity, @Nonnull final HarvestSpecimenDTO dto) {
        copyCommonFieldsOfRoeDeerAndWildBoarToDTO(entity, dto);
        copyExtensionFieldsOfWildBoar(entity, dto);
    }

    private static void copyExtendedWeightFields(final HarvestSpecimenBusinessFields src,
                                                 final HarvestSpecimenBusinessFields dst) {

        dst.setWeightEstimated(src.getWeightEstimated());
        dst.setWeightMeasured(src.getWeightMeasured());
    }

    private void copyExtensionFieldsOfMoose(final HarvestSpecimenBusinessFields src,
                                            final HarvestSpecimenBusinessFields dst,
                                            final boolean toEntity) {
        copyExtendedWeightFields(src, dst);

        dst.setFitnessClass(src.getFitnessClass());
        dst.setNotEdible(src.isNotEdible());
        dst.setAdditionalInfo(src.getAdditionalInfo());

        final boolean isAdultMale = src.isAdultMale();
        final boolean isYoung = src.isYoung();

        if (isAdultMale) {
            if (specVersion.supportsAntlerFields2020() && huntingYear >= 2020) {
                copyAntlersLost(src, dst, toEntity);

                if (src.isAntlersLost()) {
                    dst.clearAntlerDetailFields();
                } else {
                    dst.setAntlersType(src.getAntlersType());
                    dst.setAntlersWidth(src.getAntlersWidth());
                    dst.setAntlerPointsLeft(src.getAntlerPointsLeft());
                    dst.setAntlerPointsRight(src.getAntlerPointsRight());
                    dst.setAntlersGirth(src.getAntlersGirth());
                }
            } else {
                if (huntingYear < 2020) {
                    dst.setAntlersLost(null);
                    dst.setAntlersGirth(null);
                }

                // Not copying antler fields if previously marked as antlersLost (with more recent version)
                if (!dst.isAntlersLost()) {
                    dst.setAntlersType(src.getAntlersType());
                    dst.setAntlersWidth(src.getAntlersWidth());
                    dst.setAntlerPointsLeft(src.getAntlerPointsLeft());
                    dst.setAntlerPointsRight(src.getAntlerPointsRight());
                }
            }

        } else if (isYoung) {
            if (specVersion.supportsSolitaryMooseCalves()) {
                dst.setAlone(Boolean.TRUE.equals(src.getAlone()));
            }
        }

        // Clear illegal fields.

        if (!isAdultMale) {
            dst.clearAllAntlerFields();
        } else {
            // undefined for moose
            dst.setAntlersLength(null);
            dst.setAntlersInnerWidth(null);
            dst.setAntlerShaftWidth(null);
        }

        if (!isYoung) {
            dst.setAlone(null);
        }
    }

    private void copyExtensionFieldsOfRoeDeer(final HarvestSpecimenBusinessFields src,
                                              final HarvestSpecimenBusinessFields dst,
                                              final boolean toEntity) {

        if (huntingYear < 2020) {
            dst.clearExtensionFields();

        } else {
            final boolean isAdultMale = src.isAdultMale();

            if (specVersion.supportsAntlerFields2020()) {
                copyExtendedWeightFields(src, dst);

                if (isAdultMale) {
                    copyAntlersLost(src, dst, toEntity);

                    if (src.isAntlersLost()) {
                        dst.clearAntlerDetailFields();
                    } else {
                        dst.setAntlerPointsLeft(src.getAntlerPointsLeft());
                        dst.setAntlerPointsRight(src.getAntlerPointsRight());
                        dst.setAntlersLength(src.getAntlersLength());
                        dst.setAntlerShaftWidth(src.getAntlerShaftWidth());
                    }
                }
            }

            // Clear illegal fields.

            dst.setFitnessClass(null);
            dst.setNotEdible(null);
            dst.setAdditionalInfo(null);
            dst.setAlone(null);

            if (!isAdultMale) {
                dst.clearAllAntlerFields();
            } else {
                // undefined for roe deer
                dst.setAntlersType(null);
                dst.setAntlersWidth(null);
                dst.setAntlersGirth(null);
                dst.setAntlersInnerWidth(null);
            }
        }
    }

    private void copyExtensionFieldsOfWhiteTailedDeer(final HarvestSpecimenBusinessFields src,
                                                      final HarvestSpecimenBusinessFields dst,
                                                      final boolean toEntity) {

        final boolean isAdultMale = src.isAdultMale();

        if (specVersion.supportsExtendedFieldsForDeers()) {
            copyExtendedWeightFields(src, dst);

            dst.setNotEdible(src.isNotEdible());
            dst.setAdditionalInfo(src.getAdditionalInfo());

            if (isAdultMale) {
                if (specVersion.supportsAntlerFields2020() && huntingYear >= 2020) {
                    copyAntlersLost(src, dst, toEntity);

                    if (src.isAntlersLost()) {
                        dst.clearAntlerDetailFields();
                    } else {
                        dst.setAntlerPointsLeft(src.getAntlerPointsLeft());
                        dst.setAntlerPointsRight(src.getAntlerPointsRight());
                        dst.setAntlersGirth(src.getAntlersGirth());
                        dst.setAntlersLength(src.getAntlersLength());
                        dst.setAntlersInnerWidth(src.getAntlersInnerWidth());

                        // `antlersWidth` can be updated by old versions.
                        //dst.setAntlersWidth(null);
                    }
                } else {
                    if (huntingYear < 2020) {
                        dst.setAntlersLost(null);
                        dst.setAntlersGirth(null);
                        dst.setAntlersLength(null);
                        dst.setAntlersInnerWidth(null);
                    }

                    // Not copying antler fields if previously marked as antlersLost (with more recent version)
                    if (!dst.isAntlersLost()) {
                        dst.setAntlersWidth(src.getAntlersWidth());
                        dst.setAntlerPointsLeft(src.getAntlerPointsLeft());
                        dst.setAntlerPointsRight(src.getAntlerPointsRight());
                    }
                }
            }
        }

        // Clear illegal fields.

        dst.setFitnessClass(null);
        dst.setAlone(null);

        if (!isAdultMale) {
            dst.clearAllAntlerFields();
        } else {
            // undefined for white-tailed deer
            dst.setAntlersType(null);
            dst.setAntlerShaftWidth(null);
        }
    }

    // Fallow deer and wild forest reindeer
    private void copyExtensionFieldsOfOtherDeer(final HarvestSpecimenBusinessFields src,
                                                final HarvestSpecimenBusinessFields dst,
                                                final boolean toEntity) {

        final boolean isAdultMale = src.isAdultMale();

        if (specVersion.supportsExtendedFieldsForDeers()) {
            copyExtendedWeightFields(src, dst);

            dst.setNotEdible(src.isNotEdible());
            dst.setAdditionalInfo(src.getAdditionalInfo());

            if (isAdultMale) {
                if (specVersion.supportsAntlerFields2020() && huntingYear >= 2020) {
                    copyAntlersLost(src, dst, toEntity);

                    if (src.isAntlersLost()) {
                        dst.clearAntlerDetailFields();
                    } else {
                        dst.setAntlersWidth(src.getAntlersWidth());
                        dst.setAntlerPointsLeft(src.getAntlerPointsLeft());
                        dst.setAntlerPointsRight(src.getAntlerPointsRight());
                    }
                } else {
                    if (huntingYear < 2020) {
                        dst.setAntlersLost(null);
                    }

                    // Not copying antler fields if previously marked as antlersLost (with more recent version)
                    if (!dst.isAntlersLost()) {
                        dst.setAntlersWidth(src.getAntlersWidth());
                        dst.setAntlerPointsLeft(src.getAntlerPointsLeft());
                        dst.setAntlerPointsRight(src.getAntlerPointsRight());
                    }
                }
            }
        }

        // Clear illegal fields.

        dst.setFitnessClass(null);
        dst.setAlone(null);

        if (!isAdultMale) {
            dst.clearAllAntlerFields();
        } else {
            // undefined for fallow deer and wild forest reindeer
            dst.setAntlersType(null);
            dst.setAntlersGirth(null);
            dst.setAntlersLength(null);
            dst.setAntlersInnerWidth(null);
            dst.setAntlerShaftWidth(null);
        }
    }

    private void copyExtensionFieldsOfWildBoar(final HarvestSpecimenBusinessFields src,
                                               final HarvestSpecimenBusinessFields dst) {
        if (huntingYear < 2020) {
            dst.clearExtensionFields();
        } else {

            if (specVersion.supportsAntlerFields2020()) {
                copyExtendedWeightFields(src, dst);
            }

            // Clear illegal fields.

            dst.setFitnessClass(null);
            dst.setNotEdible(null);
            dst.setAdditionalInfo(null);
            dst.setAlone(null);

            dst.clearAllAntlerFields();
        }
    }

    private static void copyAntlersLost(final HarvestSpecimenBusinessFields src,
                                        final HarvestSpecimenBusinessFields dst,
                                        final boolean toEntity) {
        if (toEntity) {
            // Force `antlersLost` to non-null value (it is a client bug if it is sent as null).
            dst.setAntlersLost(src.isAntlersLost());
        } else {
            // Let `antlersLost` be null in DTO if it is not present in entity.
            dst.setAntlersLost(src.getAntlersLost());
        }
    }
}
