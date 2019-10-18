package fi.riista.feature.organization;

import fi.riista.feature.common.repository.BaseRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface OrganisationRepository extends BaseRepository<Organisation, Long> {

    @Query("select o from #{#entityName} o" +
            " left join fetch o.venues v" +
            " left join fetch v.address a" +
            " where o.id = ?1")
    Organisation getOneFetchingVenues(Long id);

    @Query("select o from #{#entityName} o" +
            " left join fetch o.address a" +
            " where o.organisationType IN (:orgType)")
    List<Organisation> findByOrganisationType(@Param("orgType") Set<OrganisationType> organisationType);

    @Query("select o from #{#entityName} o" +
            " left join fetch o.address a" +
            " where o.organisationType = :orgType")
    List<Organisation> findByOrganisationType(@Param("orgType") OrganisationType organisationType, Sort sort);

    @Query("SELECT o FROM #{#entityName} o" +
            " LEFT JOIN FETCH o.address a" +
            " WHERE o.organisationType = :orgType AND o.active = true")
    List<Organisation> findActiveByOrganisationType(@Param("orgType") OrganisationType organisationType, Sort sort);

    @Query("select distinct o from #{#entityName} o" +
            " where o.organisationType = ?1" +
            " and o.officialCode = ?2")
    Organisation findByTypeAndOfficialCode(OrganisationType organisationType, String officialcode);

    @Query("select o from #{#entityName} o" +
            " where o.organisationType in ?2 " +
            " and (trgm_dist(?1, o.nameFinnish) < ?3) order by trgm_dist(?1, o.nameFinnish)")
    List<Organisation> findByFuzzyFullNameMatch(String searchQuery, Set<OrganisationType> type,
                                                double maxDistance, Pageable page);

    @Query("select o from #{#entityName} o" +
            " where o.organisationType in ?2 " +
            " and (trgm_dist(?1, o.nameSwedish) < ?3) order by trgm_dist(?1, o.nameSwedish)")
    List<Organisation> findBySwedishFuzzyNameMatch(String searchQuery, Set<OrganisationType> type,
                                                   double maxDistance, Pageable page);

}
