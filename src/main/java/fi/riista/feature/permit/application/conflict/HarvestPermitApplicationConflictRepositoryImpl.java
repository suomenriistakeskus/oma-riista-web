package fi.riista.feature.permit.application.conflict;

import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class HarvestPermitApplicationConflictRepositoryImpl implements HarvestPermitApplicationConflictRepositoryCustom {

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Override
    @Transactional(readOnly = true)
    public List<HarvestPermitApplication> listAllConflicting(final long batchId,
                                                             final HarvestPermitApplication application) {
        final QHarvestPermitApplicationConflict CONFLICT = QHarvestPermitApplicationConflict.harvestPermitApplicationConflict;

        final List<HarvestPermitApplication> firstSet = jpqlQueryFactory
                .select(CONFLICT.secondApplication)
                .from(CONFLICT)
                .where(CONFLICT.batchId.eq(batchId))
                .where(CONFLICT.firstApplication.eq(application))
                .fetch();

        final List<HarvestPermitApplication> secondSet = jpqlQueryFactory
                .select(CONFLICT.firstApplication)
                .from(CONFLICT)
                .where(CONFLICT.batchId.eq(batchId))
                .where(CONFLICT.secondApplication.eq(application))
                .fetch();

        final HashSet<HarvestPermitApplication> uniqueSet = new HashSet<>();
        uniqueSet.addAll(firstSet);
        uniqueSet.addAll(secondSet);

        return new LinkedList<>(uniqueSet);
    }
}
