package fi.riista.feature.huntingclub.members.notification;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import fi.riista.feature.huntingclub.group.QHuntingClubGroup;
import fi.riista.feature.organization.occupation.Occupation;
import fi.riista.feature.organization.occupation.OccupationType;
import fi.riista.feature.organization.occupation.QOccupation;
import fi.riista.feature.organization.occupation.OccupationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;

@Service
public class HuntingLeaderFinderService {

    @Resource
    private OccupationRepository occupationRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(propagation = Propagation.MANDATORY, noRollbackFor = RuntimeException.class)
    public List<Occupation> findChangedLeaders(final Date begin, final Date end, final int huntingYear) {
        final QHuntingClubGroup group = QHuntingClubGroup.huntingClubGroup;
        final QOccupation occupation = QOccupation.occupation;

        final BooleanExpression isDeleted = occupation.lifecycleFields.deletionTime.isNotNull();
        final BooleanExpression notDeleted = occupation.lifecycleFields.deletionTime.isNull();

        final BooleanExpression created = occupation.lifecycleFields.creationTime.between(begin, end)
                .and(notDeleted);

        final BooleanExpression modified = occupation.lifecycleFields.modificationTime.between(begin, end)
                .and(notDeleted)
                .and(occupation.callOrder.eq(0));

        final BooleanExpression deleted = isDeleted.and(occupation.lifecycleFields.deletionTime.between(begin, end));
        final BooleanExpression huntingLeader = occupation.occupationType.eq(OccupationType.RYHMAN_METSASTYKSENJOHTAJA);

        final BooleanExpression groupHuntingYearNotOld = group.huntingYear.goe(huntingYear);
        final BooleanExpression groupNotMooseDataCard = group.fromMooseDataCard.isNull().or(group.fromMooseDataCard.isFalse());
        final BooleanExpression groupHasPermit = group.harvestPermit.isNotNull();
        final BooleanExpression groupPermitModified = group.harvestPermitModificationTime.isNotNull()
                .and(group.harvestPermitModificationTime.between(begin, end));

        return new JPAQuery<>(entityManager).from(occupation)
                .innerJoin(occupation.organisation, group._super)
                .where(groupNotMooseDataCard
                        .and(groupHuntingYearNotOld)
                        .and(groupHasPermit)
                        .and(huntingLeader)
                        .and(created.or(modified).or(deleted).or(groupPermitModified)))
                .select(occupation).fetch();
    }
}
