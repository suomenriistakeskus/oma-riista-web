package fi.riista.feature.permit.application.conflict;

import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.permit.application.HarvestPermitApplication;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

public class HarvestPermitApplicationConflictRepositoryImpl implements HarvestPermitApplicationConflictRepositoryCustom {

    @Resource
    private JPQLQueryFactory jpqlQueryFactory;

    @Override
    @Transactional(readOnly = true)
    public List<HarvestPermitApplication> listAllConflicting(final HarvestPermitApplication application) {
        final QHarvestPermitApplicationConflict CONFLICT = QHarvestPermitApplicationConflict.harvestPermitApplicationConflict;

        return jpqlQueryFactory
                .select(CONFLICT.secondApplication)
                .from(CONFLICT)
                .where(CONFLICT.firstApplication.eq(application))
                .fetch();
    }
}
