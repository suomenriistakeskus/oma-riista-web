package fi.riista.feature.account.area.union;

import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.permit.area.QHarvestPermitArea;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Optional;

@Repository
public class PersonalAreaUnionRepositoryImpl implements PersonalAreaUnionRepositoryCustom {


    @Resource
    private JPQLQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public Optional<PersonalAreaUnion> findByExternalId(final String externalId) {
        final QPersonalAreaUnion UNION = QPersonalAreaUnion.personalAreaUnion;
        final QHarvestPermitArea HPA = QHarvestPermitArea.harvestPermitArea;
        return Optional.ofNullable(queryFactory.selectFrom(UNION)
                .innerJoin(UNION.harvestPermitArea, HPA)
                .where(HPA.externalId.eq(externalId))
                .fetchOne());
    }
}
