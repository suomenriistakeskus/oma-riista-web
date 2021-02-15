package fi.riista.feature.gamediary.srva.specimen;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.gamediary.srva.SrvaEvent;
import fi.riista.feature.gamediary.srva.SrvaEventDTOBase;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class SrvaSpecimenService {

    @Resource
    private SrvaSpecimenRepository srvaSpecimenRepository;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void saveSpecimens(final SrvaEvent entity, final SrvaEventDTOBase dto) {
        Objects.requireNonNull(entity);
        Objects.requireNonNull(dto);
        requirePersistedEntity(entity);

        save(entity, dto);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean updateSpecimens(final SrvaEvent entity, final SrvaEventDTOBase dto) {
        Objects.requireNonNull(entity);
        Objects.requireNonNull(dto);
        requirePersistedEntity(entity);

        final List<SrvaSpecimen> oldSpecimens = srvaSpecimenRepository.findByEventOrderById(entity);
        srvaSpecimenRepository.deleteInBatch(oldSpecimens);

        final List<SrvaSpecimen> savedSpecimens = save(entity, dto);

        //If any of specimens has changed return true, otherwise return false
        if (savedSpecimens.size() != oldSpecimens.size()) {
            return true;
        }

        for (int i = 0; i < savedSpecimens.size(); i++) {
            if (!savedSpecimens.get(i).isEqualBusinessFields(oldSpecimens.get(i))) {
                return true;
            }
        }
        return false;
    }

    private List<SrvaSpecimen> save(final SrvaEvent eventEntity, final SrvaEventDTOBase dto) {
        final List<SrvaSpecimen> specimenEntities = dto.getSpecimens().stream()
                .filter(srvaSpecimenDTO -> srvaSpecimenDTO.getAge() != null || srvaSpecimenDTO.getGender() != null)
                .map(srvaSpecimenDTO -> {
                    final SrvaSpecimen srvaSpecimen = new SrvaSpecimen(eventEntity);
                    srvaSpecimen.setAge(srvaSpecimenDTO.getAge());
                    srvaSpecimen.setGender(srvaSpecimenDTO.getGender());
                    return srvaSpecimen;
                }).collect(Collectors.toList());

        final List<SrvaSpecimen> savedSpecimens = srvaSpecimenRepository.saveAll(specimenEntities);
        srvaSpecimenRepository.flush();

        return savedSpecimens;
    }

    private static void requirePersistedEntity(final LifecycleEntity<? extends Serializable> entity) {
        if (entity.isNew()) {
            throw new IllegalArgumentException(
                    String.format("%s object must be persisted before saving srva methods",
                            entity.getClass().getSimpleName()));
        }
    }
}
