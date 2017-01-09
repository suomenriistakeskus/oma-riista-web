package fi.riista.feature.gamediary.observation.specimen;

import fi.riista.feature.gamediary.AbstractSpecimenServiceTest;
import fi.riista.feature.gamediary.GameGender;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.util.jpa.JpaSpecs;

import org.springframework.data.jpa.domain.JpaSort;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ObservationSpecimenServiceTest extends
        AbstractSpecimenServiceTest<Observation, ObservationSpecimen, ObservationSpecimenDTO, ObservationSpecVersion> {

    private static final ObservationSpecVersion VERSION = ObservationSpecVersion.MOST_RECENT;

    @Resource
    private ObservationSpecimenService service;

    @Resource
    private ObservationSpecimenRepository repository;

    @Override
    public List<ObservationSpecVersion> getTestExecutionVersions() {
        return Collections.singletonList(VERSION);
    }

    @Override
    protected ObservationSpecimenService getService() {
        return service;
    }

    @Override
    protected SpecimenTestOps<Observation, ObservationSpecimen, ObservationSpecimenDTO> getSpecimenTestOps(
            final GameSpecies species, final ObservationSpecVersion version) {

        return new SpecimenTestOps<Observation, ObservationSpecimen, ObservationSpecimenDTO>() {

            @Override
            public int getMinAmount() {
                return Observation.MIN_AMOUNT;
            }

            @Override
            public int getMaxAmount() {
                return Observation.MAX_AMOUNT;
            }

            @Override
            public ObservationSpecimen createSpecimen(@Nullable final Observation observation) {
                return model().newObservationSpecimen(observation);
            }

            @Override
            public ObservationSpecimenDTO createDTO() {
                final ObservationSpecimenDTO dto = new ObservationSpecimenDTO();
                dto.setAge(some(ObservedGameAge.class));
                dto.setGender(some(GameGender.class));
                dto.setState(some(ObservedGameState.class));
                dto.setMarking(some(GameMarking.class));
                return dto;
            }

            @Override
            public ObservationSpecimenDTO transform(@Nonnull final ObservationSpecimen entity) {
                return ObservationSpecimenDTO.from(entity);
            }

            @Override
            public void mutateContent(@Nonnull final ObservationSpecimenDTO dto) {
                Objects.requireNonNull(dto);
                dto.setAge(someOtherThan(dto.getAge(), ObservedGameAge.class));
                dto.setGender(someOtherThan(dto.getGender(), GameGender.class));
                dto.setState(someOtherThan(dto.getState(), ObservedGameState.class));
                dto.setMarking(someOtherThan(dto.getMarking(), GameMarking.class));
            }

            @Override
            public void clearContent(@Nonnull final ObservationSpecimenDTO dto) {
                Objects.requireNonNull(dto);
                dto.clearBusinessFields();
            }

            @Override
            public boolean equalContent(
                    @Nonnull final ObservationSpecimen entity, @Nonnull final ObservationSpecimenDTO dto) {

                Objects.requireNonNull(dto, "dto is null");
                return dto.isEqualTo(entity, VERSION);
            }
        };
    }

    @Override
    protected Observation newParent() {
        final Observation observation = model().newObservation();
        observation.setAmount(1);

        // We need to set observationType that is applicable for all species
        // and suitable for adding specimens.
        observation.setObservationType(ObservationType.NAKO);

        return observation;
    }

    @Override
    protected List<ObservationSpecimen> findSpecimensInInsertionOrder(final Observation observation) {
        return repository.findAll(
                JpaSpecs.equal(ObservationSpecimen_.observation, observation), new JpaSort(ObservationSpecimen_.id));
    }

}
