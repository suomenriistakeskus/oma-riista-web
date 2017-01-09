package fi.riista.feature.huntingclub.members;

import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.RequireEntityService;
import fi.riista.feature.gamediary.GameDiaryService;
import fi.riista.feature.gamediary.GameSpecies;
import fi.riista.feature.harvestpermit.HarvestPermitAuthorization;
import fi.riista.feature.harvestpermit.HarvestPermit;
import fi.riista.feature.huntingclub.QHuntingClub;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.feature.organization.person.QPerson;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class HuntingClubContactFeature {

    @Resource
    private GameDiaryService gameDiaryService;

    @Resource
    private RequireEntityService requireEntityService;

    @Resource
    private JPQLQueryFactory queryFactory;

    @Transactional(readOnly = true)
    public List<HuntingClubContactDetailDTO> listLeaders(final long harvestPermitId,
                                                         final int huntingYear,
                                                         final int gameSpeciesCode) {
        final GameSpecies species = gameDiaryService.getGameSpeciesByOfficialCode(gameSpeciesCode);
        final HarvestPermit p = requireEntityService.requireHarvestPermit(harvestPermitId,
                HarvestPermitAuthorization.HarvestPermitPermission.LIST_LEADERS);

        final QOccupation occupation = QOccupation.occupation;
        final QPerson person = QPerson.person;

        final QHuntingClubGroup occupationGroup = new QHuntingClubGroup("occGroup");
        final QHuntingClub occupationClub = new QHuntingClub("occClub");

        return queryFactory.selectFrom(occupation)
                .join(occupation.person, person).fetchJoin()
                .join(occupation.organisation, occupationGroup._super).fetchJoin()
                .join(occupationGroup.parentOrganisation, occupationClub._super).fetchJoin()
                .where(
                        occupationGroup.harvestPermit.eq(p),
                        occupationGroup.huntingYear.eq(huntingYear),
                        occupationGroup.species.eq(species),
                        occupation.occupationType.eq(OccupationType.RYHMAN_METSASTYKSENJOHTAJA),
                        occupation.validAndNotDeleted())
                .orderBy(
                        occupationClub.nameFinnish.asc(),
                        occupationGroup.nameFinnish.asc(),
                        occupation.callOrder.asc().nullsLast())
                .fetch()
                .stream()
                .map(HuntingClubContactDetailDTO::createForGroup)
                .collect(toList());
    }
}
