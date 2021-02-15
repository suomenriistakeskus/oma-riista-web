package fi.riista.feature.account.pilot;

import com.google.common.collect.ImmutableList;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.harvestpermit.QHarvestPermit;
import fi.riista.feature.huntingclub.group.HuntingClubGroup;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.feature.organization.person.Person;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

@Transactional
@Repository
public class DeerPilotRepositoryImpl implements DeerPilotRepositoryCustom {
    private static final QDeerPilot PILOT = QDeerPilot.deerPilot;
    private static final QHuntingClubGroup GROUP = QHuntingClubGroup.huntingClubGroup;
    private static final QOccupation OCC = QOccupation.occupation;
    private static final QHarvestPermit PERMIT = QHarvestPermit.harvestPermit;

    @Resource
    private JPQLQueryFactory queryFactory;


    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    @Override
    public boolean isPersonInPilotGroup(final Person person) {
        return isPersonInPilotGroup(person.getId());
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    @Override
    public boolean isPersonInPilotGroup(final long personId) {

        return queryFactory.selectDistinct(OCC.person.id)
                .from(PILOT)
                .innerJoin(PILOT.harvestPermit, PERMIT)
                .innerJoin(PERMIT.permitGroups, GROUP)
                .innerJoin(GROUP._super.occupations, OCC)
                .where(GROUP._super.active.isTrue(),
                        OCC.person.id.eq(personId),
                        OCC.endDate.isNull())
                .fetchFirst() != null;
    }

    @Transactional(readOnly = true, propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    @Override
    public boolean isPilotGroup(final long groupId) {

        return queryFactory.select(PILOT.id)
                .from(PILOT)
                .innerJoin(PILOT.harvestPermit, PERMIT)
                .innerJoin(PERMIT.permitGroups, GROUP)
                .where(GROUP._super.active.isTrue(),
                        GROUP.id.eq(groupId))
                .fetchFirst() != null;
    }

    @Override
    public List<HuntingClubGroup> filterGroupsInPilot(final Collection<HuntingClubGroup> groups) {
        if (groups.isEmpty()){
            return ImmutableList.of();
        }

        return queryFactory
                .select(GROUP)
                .from(PILOT)
                .innerJoin(PILOT.harvestPermit, PERMIT)
                .innerJoin(PERMIT.permitGroups, GROUP)
                .where(GROUP.in(groups),
                        GROUP._super.active.isTrue())
                .fetch();
    }

}
