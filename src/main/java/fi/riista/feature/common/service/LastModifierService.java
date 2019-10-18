package fi.riista.feature.common.service;

import com.google.common.collect.Iterables;
import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.account.user.UserRepository;
import fi.riista.feature.common.dto.LastModifierDTO;
import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.organization.person.Person;
import fi.riista.feature.organization.person.PersonRepository;
import fi.riista.util.DateUtil;
import fi.riista.util.F;
import fi.riista.util.jpa.CriteriaUtils;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static fi.riista.util.Collect.mappingTo;
import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;

@Component
public class LastModifierService {

    @Resource
    private UserRepository userRepository;

    @Resource
    private PersonRepository personRepository;

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public LastModifierDTO getLastModifier(@Nonnull final LifecycleEntity<?> entity) {
        requireNonNull(entity);

        final Function<LifecycleEntity<?>, SystemUser> getUser = e -> userRepository.findOne(e.getModifiedByUserId());

        return getLastModifier(entity, getUser, SystemUser::getPerson);
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public <T extends LifecycleEntity<?>> Map<T, LastModifierDTO> getLastModifiers(@Nonnull final Iterable<T> entities) {
        requireNonNull(entities);

        if (Iterables.isEmpty(entities)) {
            return emptyMap();
        }

        final Map<Long, SystemUser> modifierUserIndex = F.indexById(findModifierUsers(entities));

        final Function<T, SystemUser> getUser = e -> modifierUserIndex.get(e.getModifiedByUserId());
        final Function<SystemUser, Person> getPerson = createUserToPersonMapping(modifierUserIndex.values());

        return F.stream(entities).collect(mappingTo(entity -> getLastModifier(entity, getUser, getPerson)));
    }

    private static <T extends LifecycleEntity<?>> LastModifierDTO getLastModifier(final T entity,
                                                                                  final Function<T, SystemUser> entityToUser,
                                                                                  final Function<SystemUser, Person> userToPerson) {

        final DateTime timestamp = DateUtil.toDateTimeNullSafe(entity.getModificationTime());
        final SystemUser user = entityToUser.apply(entity);

        // User might be null when entity is modified e.g. by a scheduled task.
        if (user == null) {
            return LastModifierDTO.createForAutomatedTask(timestamp);
        }

        final Person person = userToPerson.apply(user);

        if (person == null) {
            return LastModifierDTO.createForAdminOrModerator(user, timestamp);
        }

        return LastModifierDTO.createForPerson(person, timestamp);
    }

    private List<SystemUser> findModifierUsers(final Iterable<? extends LifecycleEntity<?>> entities) {
        final Set<Long> modifierIds = F.mapNonNullsToSet(entities, LifecycleEntity::getModifiedByUserId);

        return userRepository.findAll(modifierIds);
    }

    private Function<SystemUser, Person> createUserToPersonMapping(final Collection<SystemUser> users) {
        return CriteriaUtils.singleQueryFunction(users, SystemUser::getPerson, personRepository, false);
    }
}
