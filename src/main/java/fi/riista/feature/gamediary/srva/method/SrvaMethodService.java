package fi.riista.feature.gamediary.srva.method;

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
public class SrvaMethodService {

    @Resource
    private SrvaMethodRepository srvaMethodRepository;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public void saveMethods(final SrvaEvent eventEntity, final SrvaEventDTOBase dto) {
        Objects.requireNonNull(eventEntity);
        Objects.requireNonNull(dto);
        requirePersistedEntity(eventEntity);
        save(eventEntity, dto);
    }

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public boolean updateMethods(final SrvaEvent eventEntity, final SrvaEventDTOBase dto) {
        Objects.requireNonNull(eventEntity);
        Objects.requireNonNull(dto);
        requirePersistedEntity(eventEntity);

        final List<SrvaMethod> oldMethods = srvaMethodRepository.findByEvent(eventEntity);
        srvaMethodRepository.deleteInBatch(oldMethods);

        final List<SrvaMethod> savedMethods = save(eventEntity, dto);

        //If any of methods has changed return true, otherwise return false
        if (savedMethods.size() != oldMethods.size()) {
            return true;
        }

        for (int i = 0; i < savedMethods.size(); i++) {
            if (!savedMethods.get(i).isEqualBusinessFields(oldMethods.get(i))) {
                return true;
            }
        }
        return false;
    }

    private List<SrvaMethod> save(final SrvaEvent eventEntity, final SrvaEventDTOBase dto) {
        // Create method entities to save.
        final List<SrvaMethod> methodEntities = dto.getMethods().stream().map(srvaMethodDTO -> {
            final SrvaMethod srvaMethod = new SrvaMethod(eventEntity);
            srvaMethod.setName(srvaMethodDTO.getName());
            srvaMethod.setChecked(srvaMethodDTO.isChecked());
            return srvaMethod;
        }).collect(Collectors.toList());

        final List<SrvaMethod> savedMethods = srvaMethodRepository.save(methodEntities);
        srvaMethodRepository.flush();
        return savedMethods;
    }

    private static void requirePersistedEntity(final LifecycleEntity<? extends Serializable> entity) {
        if (entity.isNew()) {
            throw new IllegalArgumentException(
                    String.format("%s object must be persisted before saving srva methods",
                            entity.getClass().getSimpleName()));
        }
    }
}
