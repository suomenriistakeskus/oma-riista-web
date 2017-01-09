package fi.riista.feature.gamediary.observation.specimen;

import static java.util.stream.Collectors.toList;

import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.gamediary.GameDiaryEntrySpecimenDTO;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.util.DtoUtil;
import fi.riista.util.F;

import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ObservationSpecimenDTO extends GameDiaryEntrySpecimenDTO {

    public static final BiFunction<ObservationSpecimen, ObservationSpecimenDTO, Boolean> EQUAL_TO_ENTITY =
            equalToEntity(ObservationSpecVersion.MOST_RECENT);

    public static final BiFunction<ObservationSpecimen, ObservationSpecimenDTO, Boolean> equalToEntity(
            @Nonnull final ObservationSpecVersion specVersion) {

        return (entity, dto) -> dto.isEqualTo(entity, specVersion);
    }

    @Nonnull
    public static ObservationSpecimenDTO from(@Nonnull final ObservationSpecimen entity) {
        Objects.requireNonNull(entity);

        final ObservationSpecimenDTO dto = new ObservationSpecimenDTO();
        DtoUtil.copyBaseFields(entity, dto);
        dto.setAge(entity.getAge());
        dto.setGender(entity.getGender());
        dto.setState(entity.getState());
        dto.setMarking(entity.getMarking());
        return dto;
    }

    @Nonnull
    public static List<ObservationSpecimenDTO> transformList(
            final Collection<? extends ObservationSpecimen> specimens) {

        Objects.requireNonNull(specimens);
        return specimens.stream().map(asFunction()).collect(toList());
    }

    @Nonnull
    public static Function<ObservationSpecimen, ObservationSpecimenDTO> asFunction() {
        return specimen -> specimen == null ? null : from(specimen);
    }

    private ObservedGameAge age;

    private ObservedGameState state;

    private GameMarking marking;

    public ObservationSpecimenDTO() {
        super();
    }

    public ObservationSpecimenDTO(final GameGender gender, final ObservedGameAge age) {
        super(gender);
        setAge(age);
    }

    public boolean isEqualTo(
            @Nonnull final ObservationSpecimen specimen, @Nonnull final ObservationSpecVersion version) {

        Objects.requireNonNull(specimen, "specimen must not be null");
        Objects.requireNonNull(version, "version must not be null");

        return getGender() == specimen.getGender() &&
                getAge() == specimen.getAge() &&
                getState() == specimen.getState() &&
                getMarking() == specimen.getMarking();
    }

    @Override
    public boolean allBusinessFieldsNull() {
        return super.allBusinessFieldsNull() && F.allNull(getAge(), getState(), getMarking());
    }

    @Override
    public void clearBusinessFields() {
        super.clearBusinessFields();
        setAge(null);
        setState(null);
        setMarking(null);
    }

    // Accessors -->

    public ObservedGameAge getAge() {
        return age;
    }

    public void setAge(final ObservedGameAge age) {
        this.age = age;
    }

    public ObservedGameState getState() {
        return state;
    }

    public void setState(final ObservedGameState state) {
        this.state = state;
    }

    public GameMarking getMarking() {
        return marking;
    }

    public void setMarking(final GameMarking marking) {
        this.marking = marking;
    }

}
