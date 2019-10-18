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

    @Query("SELECT p FROM Person p" +
            " LEFT JOIN FETCH p.mrAddress" +
            " WHERE p.ssn IN (?1)")
    List<Person> findBySsnAndFetchMrAddress(List<String> list);

    // Faster fuzzy search using maximum distance 0.7
    @Query("SELECT p FROM Person p" +
            " WHERE p.ssn IS NOT NULL " +
            " AND TRUE = trgm_match(?1, p.firstName || ' ' || p.lastName)" +
            " ORDER BY trgm_dist(?1, p.firstName || ' ' || p.lastName)")
    List<Person> findFinnishPersonsByFuzzyFullNameMatch(String searchQuery, Pageable page);

    // Faster fuzzy search using maximum distance 0.7
    @Query("SELECT p FROM Person p" +
            " WHERE TRUE = trgm_match(?1, p.firstName || ' ' || p.lastName)" +
            " ORDER BY trgm_dist(?1, p.firstName || ' ' || p.lastName)")
    List<Person> findAllPersonsByFuzzyFullNameMatch(String searchQuery, Pageable page);

    @Query("SELECT p from Person p" +
            " WHERE trgm_dist(?1, p.firstName || ' ' || p.lastName) < ?2" +
            " ORDER BY trgm_dist(?1, p.firstName || ' ' || p.lastName)")
    List<Person> findByFuzzyFullNameDistance(String searchQuery, double maxDistance, Pageable page);

    @Query("SELECT p FROM Person p " +
            " WHERE p.ssn IS NOT NULL AND " +
            " p.hunterNumber = ?1")
    Optional<Person> findFinnishPersonByHunterNumber(String hunterNumber);

    @Query("SELECT p FROM Person p " +
            " WHERE p.hunterNumber = ?1")
    Optional<Person> findByHunterNumber(String hunterNumber);

    @Query("SELECT p FROM Person p " +
            " WHERE p.ssn IS NOT NULL AND " +
            " p.hunterNumber IN ?1")
    List<Person> findFinnishPersonsByHunterNumber(Collection<String> hunterNumbers);

    List<Person> findAllByHunterNumberIn(Collection<String> hunterNumbers);

    @Query("SELECT distinct trim(lower(p.email)) FROM SystemUser u JOIN u.person p" +
            " WHERE u.active IS TRUE " +
            " AND p.email IS not NULL" +
            " AND u.role in :roles")
    Set<String> findEmailForActiveUserWithRole(@Param("roles") Collection<SystemUser.Role> roles);

    @Query("SELECT distinct trim(lower(p.email)) FROM SystemUser u" +
            " JOIN u.person p" +
            " JOIN p.occupations as o" +
            " WHERE u.active IS TRUE " +
            " AND p.email IS NOT NULL" +
            " AND o.occupationType = :occupationType" + OccupationRepository.AND_ACTIVE)
    Set<String> findEmailForActiveUserWithOccupationType(@Param("occupationType") OccupationType type);

    @Query("SELECT p FROM SystemUser u JOIN u.person p WHERE lower(u.username) = lower(:username)")
    List<Person> findByUsernameIgnoreCase(@Param("username") String username);
}
