package fi.riista.feature.organization.occupation;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.organization.Organisation;
import fi.riista.feature.organization.OrganisationType;
import fi.riista.feature.organization.Organisation_;
import fi.riista.feature.organization.person.Person;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static fi.riista.util.jpa.JpaSpecs.inIdCollection;
import static fi.riista.util.jpa.JpaSpecs.overlapsInterval;
import static java.util.Collections.singleton;

public interface OccupationRepository extends BaseRepository<Occupation, Long>, OccupationRepositoryCustom {

    String AND_NOT_DELETED = " AND o.lifecycleFields.deletionTime IS NULL";
    String AND_ACTIVE = AND_NOT_DELETED + " AND CURRENT_DATE BETWEEN COALESCE(o.beginDate, CURRENT_DATE) AND COALESCE(o.endDate, CURRENT_DATE)";

    @Query("SELECT o FROM #{#entityName} o WHERE o.organisation = :org")
    List<Occupation> findByOrganisation(@Param("org") Organisation organisation);

    @Query("SELECT o FROM #{#entityName} o INNER JOIN FETCH o.organisation WHERE o.person = :person" + AND_ACTIVE)
    List<Occupation> findActiveByPerson(@Param("person") Person person);

    @Query("SELECT o FROM #{#entityName} o INNER JOIN FETCH o.organisation WHERE o.person = :person" + AND_ACTIVE
            + " AND o.organisation.active = TRUE"
            + " AND (o.occupationType <> 'RYHMAN_METSASTYKSENJOHTAJA' OR o.organisation.huntingYear = :huntingYear)")
    List<Occupation> findOccupationsForRoleMapping(@Param("person") Person person, @Param("huntingYear") int huntingYear);

    @Query("SELECT o FROM #{#entityName} o WHERE o.organisation = :org" + AND_NOT_DELETED)
    List<Occupation> findNotDeletedByOrganisation(@Param("org") Organisation organisation);

    @Query("SELECT o FROM #{#entityName} o " +
            " INNER JOIN FETCH o.organisation org" +
            " INNER JOIN FETCH o.person person" +
            " WHERE o.organisation = :org" +
            " AND o.occupationType = :type" +
            AND_ACTIVE)
    List<Occupation> findActiveByOrganisationAndOccupationType(@Param("org") Organisation organisation,
                                                               @Param("type") OccupationType occupationType);

    @Query("SELECT o FROM #{#entityName} o WHERE o.person = :person AND o.organisation = :org" + AND_ACTIVE)
    List<Occupation> findActiveByOrganisationAndPerson(@Param("org") Organisation organisation,
                                                       @Param("person") Person person);

    @Query("SELECT o FROM #{#entityName} o" +
            " INNER JOIN FETCH o.organisation org" +
            " WHERE o.person = :person" +
            " AND org.organisationType IN :orgTypes" +
            AND_ACTIVE)
    List<Occupation> findActiveByPersonAndOrganisationTypes(@Param("person") Person person,
                                                            @Param("orgTypes") Set<OrganisationType> type);

    @Query("SELECT o FROM #{#entityName} o" +
            " WHERE o.person = :person" +
            " AND o.organisation = :org" +
            AND_NOT_DELETED +
            " ORDER BY o.beginDate ASC, o.endDate ASC")
    List<Occupation> findNotDeletedByOrganisationAndPerson(@Param("org") Organisation organisation,
                                                           @Param("person") Person person);

    @Query("SELECT o FROM #{#entityName} o" +
            " INNER JOIN FETCH o.organisation org" +
            " WHERE o.person = :person" +
            " AND org.parentOrganisation = :parent" +
            AND_NOT_DELETED +
            " ORDER BY o.beginDate ASC, o.endDate ASC")
    List<Occupation> findNotDeletedByParentOrganisationAndPerson(@Param("parent") Organisation organisation,
                                                                 @Param("person") Person person);

    @Query("SELECT o from #{#entityName} o" +
            " INNER JOIN FETCH o.person p" +
            " LEFT JOIN FETCH p.mrAddress" +
            " LEFT JOIN FETCH p.otherAddress" +
            " WHERE o.occupationType IN :occupationTypes" +
            AND_NOT_DELETED)
    List<Occupation> listNotDeletedFilteredByTypesWhileFetchingRelatedPersons(
            @Param("occupationTypes") Set<OccupationType> occupationTypes);

    @Query("SELECT o FROM #{#entityName} o WHERE o.organisation.id in ?1 AND o.occupationType IN ?2" + AND_ACTIVE)
    List<Occupation> findActiveByOrganisationsAndTypes(Collection<Long> organisationIds, Set<OccupationType> roles);

    @Query("SELECT COUNT(o.id) FROM #{#entityName} o WHERE o.organisation.id in ?1" +
            " AND o.person = ?2" +
            " AND o.occupationType IN ?3" +
            AND_ACTIVE)
    long countActiveByTypeAndPersonAndOrganizationIn(Collection<Long> organisationIds,
                                                     Person person,
                                                     Set<OccupationType> roles);

    @Query("SELECT COUNT(o.id) FROM #{#entityName} o " +
            " WHERE o.person = ?1" +
            " AND o.occupationType IN ?2" +
            " AND o.lifecycleFields.deletionTime IS NULL " +
            " AND ?3 BETWEEN COALESCE(o.beginDate, ?3) AND COALESCE(o.endDate, ?3)")
    long countActiveByPersonAndTypesAndValidOn(Person person, Set<OccupationType> roles, LocalDate validOn);

    @Query("SELECT COUNT(o.id) FROM #{#entityName} o " +
            " WHERE o.organisation = ?1" +
            " AND o.person = ?2" +
            " AND o.occupationType IN ?3" +
            " AND o.lifecycleFields.deletionTime IS NULL " +
            " AND ?4 BETWEEN COALESCE(o.beginDate, ?4) AND COALESCE(o.endDate, ?4)")
    long countActiveByTypeAndPersonAndOrganizationValidOn(Organisation organisation,
                                                          Person person,
                                                          Set<OccupationType> roles,
                                                          LocalDate validOn);

    @Query("SELECT COUNT(o.id) FROM #{#entityName} o WHERE o.person = ?1 AND o.occupationType IN ?2" + AND_ACTIVE)
    long countActiveByTypeAndPerson(Person p, Set<OccupationType> type);

    @Query("SELECT COUNT(o.id) FROM #{#entityName} o WHERE o.organisation.id = ?1 AND o.occupationType = ?2" +
            AND_NOT_DELETED)
    int countNotDeletedByTypeAndOrganisation(Long organisationId, OccupationType type);

    default List<Occupation> findActiveByOrganisation(final Organisation organisation,
                                                      final Interval activityInterval) {

        return findActiveByOrganisations(singleton(organisation.getId()), activityInterval);
    }

    default List<Occupation> findActiveByOrganisations(final Collection<Long> organisationIds,
                                                       final Interval activityInterval) {

        final DateTime beginTime = activityInterval.getStart();
        final DateTime endTime = activityInterval.getEnd();

        final Specification<Occupation> constraint = Specifications
                .where(overlapsInterval(Occupation_.beginDate, Occupation_.endDate, beginTime, endTime))
                .and(inIdCollection(Occupation_.organisation, Organisation_.id, organisationIds));

        return findAll(constraint);
    }
}
