package fi.riista.feature.harvestregistry;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQueryFactory;
import fi.riista.feature.common.repository.BaseRepositoryImpl;
import fi.riista.feature.organization.person.Person;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Repository
public class HarvestRegistryItemRepositoryImpl implements HarvestRegistryItemRepositoryCustom {

    @Resource
    private JPQLQueryFactory queryFactory;

    @Override
    @Transactional(readOnly = true)
    public Slice<HarvestRegistryItem> findByPerson(final Person person,
                                                   final Predicate predicate,
                                                   final Pageable pageRequest) {

        final QHarvestRegistryItem ITEM = QHarvestRegistryItem.harvestRegistryItem;

        return BaseRepositoryImpl.toSlice(queryFactory
                .selectFrom(ITEM)
                .where(ITEM.shooterHunterNumber.eq(person.getHunterNumber())
                        .and(predicate))
                .orderBy(ITEM.pointOfTime.desc())
                .offset(pageRequest.getOffset())
                .limit(pageRequest.getPageSize() + 1)
                .fetch(), pageRequest);

    }
}
