package fi.riista.feature.gamediary.observation.specimen;

import fi.riista.feature.gamediary.AbstractSpecimenService;
import fi.riista.feature.gamediary.OutOfBoundsSpecimenAmountException;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import io.vavr.Tuple2;
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
    public List<ObservationSpecimen> addSpecimens(@Nonnull final Observation observation,
                                                  final int totalAmount,
                                                  @Nonnull final List<ObservationSpecimenDTO> dtos) {

        return addSpecimens(observation, totalAmount, dtos, VERSION);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public Tuple2<List<ObservationSpecimen>, Boolean> setSpecimens(@Nonnull final Observation observation,
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

    protected ObservationSpecimenOps getSpecimenOps(@Nonnull final Observation observation,
                                                    @Nonnull final ObservationSpecVersion version) {

        Objects.requireNonNull(observation, "observation is null");
        return new ObservationSpecimenOps(observation.getSpecies(), version);
    }

    @Override
    protected BiConsumer<ObservationSpecimenDTO, ObservationSpecimen> getSpecimenFieldCopier(
            @Nonnull final Observation observation, @Nonnull final ObservationSpecVersion version) {

        return getSpecimenOps(observation, version)::copyContentToEntity;
    }

    @Override
    protected void checkParameters(final Observation observation,
                                   final int totalAmount,
                                   final List<ObservationSpecimenDTO> dtos,
                                   final ObservationSpecVersion specVersion) {

        super.checkParameters(observation, totalAmount, dtos, specVersion);
        OutOfBoundsSpecimenAmountException.assertObservationSpecimenAmountWithinBounds(totalAmount);
    }
}
