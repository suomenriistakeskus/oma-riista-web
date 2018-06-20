package fi.riista.feature.organization.person;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.organization.occupation.OccupationRepository;
import fi.riista.feature.organization.occupation.OccupationType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PersonRepository extends BaseRepository<Person, Long> {

    Optional<Person> findBySsn(String ssn);

    @Query("select p from Person p" +
            " left join fetch p.mrAddress" +
            " where p.ssn IN (?1)")
    List<Person> findBySsnAndFetchMrAddress(List<String> list);

    // Faster fuzzy search using maximum distance 0.7
    @Query("select p from Person p" +
            " where TRUE = trgm_match(?1, p.firstName || ' ' || p.lastName)" +
            " order by trgm_dist(?1, p.firstName || ' ' || p.lastName)")
    List<Person> findByFuzzyFullNameMatch(String searchQuery, Pageable page);

    @Query("select p from Person p" +
            " where trgm_dist(?1, p.firstName || ' ' || p.lastName) < ?2" +
            " order by trgm_dist(?1, p.firstName || ' ' || p.lastName)")
    List<Person> findByFuzzyFullNameDistance(String searchQuery, double maxDistance, Pageable page);

    Optional<Person> findByHunterNumber(String hunterNumber);

    @Query("select distinct trim(lower(p.email)) from SystemUser u join u.person p" +
            " where u.active is TRUE " +
            " and p.email is not null" +
            " and u.role in :roles")
    Set<String> findEmailForActiveUserWithRole(@Param("roles") Collection<SystemUser.Role> roles);

    @Query("select distinct trim(lower(p.email)) from SystemUser u" +
            " join u.person p" +
            " join p.occupations as o" +
            " where u.active is TRUE " +
            " and p.email is not null" +
            " and o.occupationType = :occupationType" + OccupationRepository.AND_ACTIVE)
    Set<String> findEmailForActiveUserWithOccupationType(@Param("occupationType") OccupationType type);

    @Query("select p from SystemUser u JOIN u.person p where lower(u.username) = lower(:username)")
    List<Person> findByUsernameIgnoreCase(@Param("username") String username);
}
