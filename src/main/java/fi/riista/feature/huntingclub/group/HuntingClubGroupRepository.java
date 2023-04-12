package fi.riista.feature.huntingclub.group;

import fi.riista.feature.account.user.SystemUser;
import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import fi.riista.feature.organization.occupation.OccupationType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface HuntingClubGroupRepository extends BaseRepository<HuntingClubGroup, Long>,
        HuntingClubGroupRepositoryCustom {

    List<HuntingClubGroup> findByParentOrganisation(HuntingClub club);

    long countByHuntingArea(HuntingClubArea huntingClubArea);

    @Query("SELECT DISTINCT o.huntingYear FROM #{#entityName} o WHERE o.parentOrganisation= ?1")
    List<Integer> listHuntingYears(HuntingClub club);

    @Query("SELECT g FROM #{#entityName} g" +
            " JOIN g.parentOrganisation c" +
            " JOIN g.harvestPermit hp" +
            " WHERE hp = :permit" +
            " AND c IN :clubs")
    List<HuntingClubGroup> findByPermitAndClubs(@Param("permit") HarvestPermit permit,
                                                @Param("clubs") Set<HuntingClub> clubs);

    @Query("SELECT g FROM #{#entityName} g" +
            " INNER JOIN g.occupations occ" +
            " INNER JOIN occ.person person" +
            " INNER JOIN person.systemUsers user" +
            " WHERE user = :user" +
            " AND g.harvestPermit IS NOT NULL" +
            " AND g.huntingYear IN :years" +
            " AND occ.occupationType = :occupationType")
    List<HuntingClubGroup> findByUserAndHuntingYears(@Param("user") SystemUser user,
                                                     @Param("years") List<Integer> years,
                                                     @Param("occupationType") OccupationType occupationType);

    default boolean isClubUsingMooseDataCardForPermit(final HuntingClub club, final int huntingYear) {
        final QHuntingClubGroup group = QHuntingClubGroup.huntingClubGroup;
        return this.count(group.fromMooseDataCard.eq(true)
                .and(group.huntingYear.eq(huntingYear))
                .and(group.parentOrganisation.eq(club))) > 0;
    }
}
