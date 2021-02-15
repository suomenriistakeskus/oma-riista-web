package fi.riista.feature.gamediary.observation.specimen;

import fi.riista.feature.gamediary.AbstractSpecimenServiceTest;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.gamediary.observation.Observation;
import fi.riista.feature.gamediary.observation.ObservationSpecVersion;
import fi.riista.feature.gamediary.observation.ObservationType;
import fi.riista.feature.gamediary.observation.metadata.ObservationMetadata;
import fi.riista.util.jpa.JpaSpecs;
import org.springframework.data.jpa.domain.JpaSort;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import static fi.riista.feature.gamediary.observation.ObservationCategory.NORMAL;

public class ObservationSpecimenServiceTest extends
        AbstractSpecimenServiceTest<Observation, ObservationSpecimen, ObservationSpecimenDTO, ObservationSpecVersion> {

    @Resource
    private ObservationSpecimenService service;

    @Resource
    private ObservationSpecimenRepository repository;

    @Override
    public List<ObservationSpecVersion> getTestExecutionVersions() {
        return new ArrayList<>(EnumSet.allOf(ObservationSpecVersion.class));
    }

    @Override
    protected ObservationSpecimenService getService() {
        return service;
    }

    @Override
    protected SpecimenTestOps<Observation, ObservationSpecimen, ObservationSpecimenDTO> getSpecimenTestOps(
            final Observation observation, final ObservationSpecVersion version) {

        return new CustomObservationSpecimenOps(observation.getSpecies(), version);
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
                JpaSpecs.equal(ObservationSpecimen_.observation, observation), JpaSort.of(ObservationSpecimen_.id));
    }

    private class CustomObservationSpecimenOps extends ObservationMetadata
            implements SpecimenTestOps<Observation, ObservationSpecimen, ObservationSpecimenDTO> {

        public CustomObservationSpecimenOps(@Nonnull final GameSpecies species,
                                            @Nonnull final ObservationSpecVersion specVersion) {

            super(model().newObservationBaseFields(species, specVersion),
                    model().newObservationContextSensitiveFields(species, NORMAL, ObservationType.NAKO, specVersion));
        }

        @Override
        public ObservationSpecimen createSpecimen(@Nullable final Observation observation) {
            return model().newObservationSpecimen(observation, getContextSensitiveFields());
        }

        @Override
        public ObservationSpecimenDTO createDTO() {
            return newObservationSpecimenDTO(false);
        }

        @Override
        public void mutateContent(@Nonnull final ObservationSpecimenDTO dto) {
            super.mutateContent(dto, false);
        }

        @Override
        public void clearContent(@Nonnull final ObservationSpecimenDTO dto) {
            Objects.requireNonNull(dto).clearBusinessFields();
        }
    }
}
