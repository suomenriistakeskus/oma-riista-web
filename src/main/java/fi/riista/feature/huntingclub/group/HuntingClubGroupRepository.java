package fi.riista.feature.huntingclub.group;

import fi.riista.feature.common.repository.BaseRepository;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.HuntingClub;
import fi.riista.feature.huntingclub.area.HuntingClubArea;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface HuntingClubGroupRepository extends BaseRepository<HuntingClubGroup, Long>,
        HuntingClubGroupRepositoryCustom {

    List<HuntingClubGroup> findByParentOrganisation(HuntingClub club);

    long countByHuntingArea(HuntingClubArea huntingClubArea);

    @Query("select distinct o.huntingYear FROM #{#entityName} o WHERE o.parentOrganisation= ?1")
    List<Integer> listHuntingYears(HuntingClub club);

    @Query("select g from #{#entityName} g" +
            " join g.parentOrganisation c" +
            " join g.harvestPermit hp" +
            " where hp = :permit" +
            " and c in :clubs")
    List<HuntingClubGroup> findByPermitAndClubs(@Param("permit") HarvestPermit permit,
                                                @Param("clubs") Set<HuntingClub> clubs);

    default boolean isClubUsingMooseDataCardForPermit(HuntingClub club, final int huntingYear) {
        final QHuntingClubGroup group = QHuntingClubGroup.huntingClubGroup;
        return this.count(group.fromMooseDataCard.eq(true)
                .and(group.huntingYear.eq(huntingYear))
                .and(group.parentOrganisation.eq(club))) > 0;
    }
}
