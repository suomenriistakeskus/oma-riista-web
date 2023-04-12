package fi.riista.feature.account.user;

import fi.riista.feature.common.entity.LifecycleEntity;
import fi.riista.feature.organization.person.Person;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserRepositoryCustom {
    void deactivateAccountsForDeceased();

    boolean isModeratorOrAdmin(long userId);

    Map<Long, String> getModeratorFullNames(Iterable<? extends LifecycleEntity<Long>> entities);

    Optional<SystemUser> findActiveByPerson(Person person);

    Map<Long, SystemUser> findActiveByPersonIn(List<Person> persons);
}
