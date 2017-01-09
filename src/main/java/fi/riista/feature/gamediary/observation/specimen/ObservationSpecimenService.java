package fi.riista.feature.gamediary.observation.specimen;

import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;

import fi.riista.feature.gamediary.AbstractSpecimenService;
import javaslang.Tuple2;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

@Component
public class ObservationSpecimenService
        extends AbstractSpecimenService<Observation, ObservationSpecimen, ObservationSpecimenDTO, ObservationSpecVersion> {

    private static final ObservationSpecVersion VERSION = ObservationSpecVersion.MOST_RECENT;

    @Resource
    private ObservationSpecimenRepository repository;

    public ObservationSpecimenService() {
        super(ObservationSpecimen_.observation);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<ObservationSpecimen> addSpecimens(
            @Nonnull final Observation observation,
            final int totalAmount,
            @Nonnull final List<ObservationSpecimenDTO> dtos) {

        return addSpecimens(observation, totalAmount, dtos, VERSION);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Tuple2<List<ObservationSpecimen>, Boolean> setSpecimens(
            @Nonnull final Observation observation,
            final int totalAmount,
            @Nonnull final List<ObservationSpecimenDTO> dtos) {

        return setSpecimens(observation, totalAmount, dtos, VERSION);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected ObservationSpecimenRepository getSpecimenRepository() {
        return repository;
    }

    @Override
    protected ObservationSpecimen createSpecimen(@Nonnull final Observation observation) {
        Objects.requireNonNull(observation);
        return new ObservationSpecimen(observation);
    }

    @Override
    protected boolean hasContent(final ObservationSpecimenDTO dto) {
        return !dto.allBusinessFieldsNull();
    }

    @Override
    protected BiConsumer<ObservationSpecimenDTO, ObservationSpecimen> getSpecimenFieldCopier(
            final Observation diaryEntry, final ObservationSpecVersion version) {

        return (dto, entity) -> {
            Objects.requireNonNull(dto, "dto is null");
            Objects.requireNonNull(entity, "entity is null");

            entity.setGender(dto.getGender());
            entity.setAge(dto.getAge());
            entity.setState(dto.getState());
            entity.setMarking(dto.getMarking());
        };
    }

    @Override
    protected void checkParameters(
            final Observation observation,
            final int totalAmount,
            final List<ObservationSpecimenDTO> dtos,
            final ObservationSpecVersion specVersion) {

        super.checkParameters(observation, totalAmount, dtos, specVersion);
        assertSpecimenAmountWithinBounds(totalAmount);
    }

    private static void assertSpecimenAmountWithinBounds(final int totalAmount) {
        if (totalAmount < Observation.MIN_AMOUNT || totalAmount > Observation.MAX_AMOUNT) {
            throw new IllegalArgumentException(String.format(
                    "Total amount of observation specimens must be between %d and %d",
                    Observation.MIN_AMOUNT, Observation.MAX_AMOUNT));
        }
    }

}
