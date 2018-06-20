package fi.riista.feature.gamediary.observation.specimen;

import com.google.common.base.Preconditions;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;
import fi.riista.util.NumberUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static fi.riista.feature.gamediary.GameSpecies.isBear;
import static fi.riista.feature.gamediary.GameSpecies.isLargeCarnivore;
import static fi.riista.feature.gamediary.GameSpecies.isWolf;
import static java.util.stream.Collectors.toList;

// TODO: Introduce an enum class named GameSpeciesCode and move all the logic
// from here to there.
public class ObservationSpecimenOps {

    public static final double MIN_PAW_WIDTH_OF_LARGE_CARNIVORES = 4.0;
    public static final double MAX_PAW_WIDTH_OF_BEAR = 20.0;
    public static final double MAX_PAW_WIDTH_OF_OTHER_LARGE_CARNIVORES = 15.0;

    public static final double MIN_PAW_LENGTH_OF_LARGE_CARNIVORES = 4.0;
    public static final double MAX_PAW_LENGTH_OF_LARGE_CARNIVORES = 15.0;

    private final int gameSpeciesCode;
    private final ObservationSpecVersion specVersion;

    public ObservationSpecimenOps(@Nonnull final GameSpecies species,
                                  @Nonnull final ObservationSpecVersion specVersion) {

        this(Objects.requireNonNull(species, "species is null").getOfficialCode(), specVersion);
    }

    public ObservationSpecimenOps(final int gameSpeciesCode, @Nonnull final ObservationSpecVersion specVersion) {
        Preconditions.checkState(gameSpeciesCode > 0, "gameSpeciesCode not set");

        this.gameSpeciesCode = gameSpeciesCode;
        this.specVersion = Objects.requireNonNull(specVersion);
    }

    public static Double getMinWidthOfPaw(final int gameSpeciesCode) {
        return isLargeCarnivore(gameSpeciesCode) ? MIN_PAW_WIDTH_OF_LARGE_CARNIVORES : null;
    }

    public static Double getMaxWidthOfPaw(final int gameSpeciesCode) {
        if (isBear(gameSpeciesCode)) {
            return MAX_PAW_WIDTH_OF_BEAR;
        } else if (isLargeCarnivore(gameSpeciesCode)) {
            return MAX_PAW_WIDTH_OF_OTHER_LARGE_CARNIVORES;
        }
        return null;
    }

    public static Double getMinLengthOfPaw(final int gameSpeciesCode) {
        return isLargeCarnivore(gameSpeciesCode) ? MIN_PAW_LENGTH_OF_LARGE_CARNIVORES : null;
    }

    public static Double getMaxLengthOfPaw(final int gameSpeciesCode) {
        return isLargeCarnivore(gameSpeciesCode) ? MAX_PAW_LENGTH_OF_LARGE_CARNIVORES : null;
    }

    /**
     * "Lauma" in Finnish (relevant to wolf only)
     */
    public static Boolean isPack(final int gameSpeciesCode, final Integer amountOfSpecimens) {
        return !isWolf(gameSpeciesCode) ? null : F.coalesceAsInt(amountOfSpecimens, 0) >= 3;
    }

    /**
     * @return a boolean indicating whether specimens contains one adult and at
     *         least one young ("pentue" in Finnish).
     */
    public static Boolean isLitter(final int gameSpeciesCode,
                                   @Nullable final Iterable<? extends ObservationSpecimen> specimens) {

        if (!isLargeCarnivore(gameSpeciesCode) || isWolf(gameSpeciesCode)) {
            return null;
        }

        int adults = 0;
        int youngs = 0;

        if (specimens != null) {
            for (final ObservationSpecimen specimen : specimens) {
                final ObservedGameAge age = specimen.getAge();

                if (age != null) {
                    switch (age) {
                        case ADULT:
                            adults++;
                            break;
                        case LT1Y:
                            youngs++;
                            break;
                        default:
                            // rest ignored
                    }
                }
            }
        }

        return adults == 1 && youngs > 0;
    }

    public int getMinAmount() {
        return Observation.MIN_AMOUNT;
    }

    public int getMaxAmount() {
        return Observation.MAX_AMOUNT;
    }

    public boolean equalContent(@Nonnull final ObservationSpecimen entity, @Nonnull final ObservationSpecimenDTO dto) {
        Objects.requireNonNull(entity, "entity is null");
        Objects.requireNonNull(dto, "dto is null");

        final boolean commonFieldsEqual = entity.getGender() == dto.getGender() &&
                entity.getAge() == dto.getAge() &&
                entity.getState() == dto.getState() &&
                entity.getMarking() == dto.getMarking();

        if (!supportsLargeCarnivoreFields()) {
            return commonFieldsEqual;
        }

        return commonFieldsEqual &&
                NumberUtils.equal(entity.getWidthOfPaw(), dto.getWidthOfPaw()) &&
                NumberUtils.equal(entity.getLengthOfPaw(), dto.getLengthOfPaw());
    }

    public boolean supportsLargeCarnivoreFields() {
        return specVersion.supportsLargeCarnivoreFields();
    }

    public ObservationSpecimenDTO transform(@Nonnull final ObservationSpecimen entity) {
        Objects.requireNonNull(entity);
        final ObservationSpecimenDTO dto = new ObservationSpecimenDTO();
        DtoUtil.copyBaseFields(entity, dto);
        copyContentToDTO(entity, dto);
        return dto;
    }

    public List<ObservationSpecimenDTO> transformList(@Nonnull final Collection<? extends ObservationSpecimen> specimens) {
        Objects.requireNonNull(specimens);
        return specimens.stream().map(this::transform).collect(toList());
    }

    public void copyContentToEntity(@Nonnull final ObservationSpecimenDTO dto,
                                    @Nonnull final ObservationSpecimen entity) {

        Objects.requireNonNull(dto, "dto is null");
        Objects.requireNonNull(entity, "entity is null");

        entity.setGender(dto.getGender());
        entity.setAge(dto.getAge());
        entity.setState(dto.getState());
        entity.setMarking(dto.getMarking());

        if (supportsLargeCarnivoreFields()) {
            entity.setWidthOfPaw(dto.getWidthOfPaw());
            entity.setLengthOfPaw(dto.getLengthOfPaw());
        }
    }

    public void copyContentToDTO(@Nonnull final ObservationSpecimen entity, @Nonnull final ObservationSpecimenDTO dto) {
        Objects.requireNonNull(entity, "entity is null");
        Objects.requireNonNull(dto, "dto is null");

        dto.setGender(entity.getGender());
        dto.setAge(entity.getAge());
        dto.setState(entity.getState());
        dto.setMarking(entity.getMarking());

        if (supportsLargeCarnivoreFields()) {
            dto.setWidthOfPaw(entity.getWidthOfPaw());
            dto.setLengthOfPaw(entity.getLengthOfPaw());
        }
    }

    // Accessors -->

    public int getGameSpeciesCode() {
        return gameSpeciesCode;
    }

    public ObservationSpecVersion getSpecVersion() {
        return specVersion;
    }
}
