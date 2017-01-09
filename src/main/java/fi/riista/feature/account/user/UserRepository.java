package fi.riista.feature.account.user;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.organization.person.Person;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends BaseRepository<SystemUser, Long>, UserRepositoryCustom {
    SystemUser findByUsernameIgnoreCase(String username);

    SystemUser findByUsernameIgnoreCaseAndActive(String username, boolean active);

    List<SystemUser> findByPerson(Person person);

    @Query("select s from SystemUser s where s.role in :roles")
    Page<SystemUser> listHavingAnyOfRole(@Param("roles") List<SystemUser.Role> roles, Pageable page);

    @Query("select s from SystemUser s where s.role in :roles")
    List<SystemUser> listHavingAnyOfRole(@Param("roles") List<SystemUser.Role> roles);

    @Modifying
    @Query("update SystemUser user SET active = FALSE WHERE user.person IN (?1)")
    void deactivateAccount(List<Person> persons);
}
